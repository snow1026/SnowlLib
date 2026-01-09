package io.github.snow1026.snowlib.utils.reflect;

import io.github.snow1026.snowlib.utils.VersionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Java 리플렉션(Reflection)을 쉽고 안전하며 강력하게 다루기 위한 유틸리티 클래스입니다.
 * <p>
 * 이 클래스는 마인크래프트(Bukkit/Spigot) 플러그인 개발 환경에 특화되어 설계되었습니다.
 * 복잡한 예외 처리를 내부적으로 갈무리하고, 캐싱을 통해 성능 오버헤드를 최소화합니다.
 * </p>
 *
 * <h3>주요 기능</h3>
 * <ul>
 * <li><b>버전 독립적 경로 처리:</b> {@code {nms}}, {@code {obc}}, {@code {version}} 플레이스홀더를 사용하여 버전별 패키지 경로를 자동으로 해결합니다.</li>
 * <li><b>스마트 타입 매칭:</b> Primitive 타입(int, double 등)과 Wrapper 클래스(Integer, Double 등) 간의 자동 변환을 지원하여 메서드/생성자 검색 실패를 줄입니다.</li>
 * <li><b>고성능 캐싱:</b> 한 번 검색된 클래스, 메서드, 필드는 {@link ConcurrentHashMap}에 캐시되어 반복 호출 시 성능 저하를 방지합니다.</li>
 * <li><b>Fluent API:</b> {@link #on(Object)} 등을 통해 직관적인 메서드 체이닝 방식으로 리플렉션을 사용할 수 있습니다.</li>
 * <li><b>Java 모듈 시스템 대응:</b> Java 17+ 환경에서도 {@code setAccessible} 및 final 필드 수정을 위한 방어 로직이 포함되어 있습니다.</li>
 * </ul>
 *
 * @author Snow1026
 * @see java.lang.reflect.Method
 * @see java.lang.reflect.Field
 */
@SuppressWarnings({"unchecked", "unused"})
public final class Reflection {
    /** 현재 서버의 버전 문자열 (예: v1_20_R1) */
    private static final String VERSION_STRING;
    /** NMS 패키지 접두사 (net.minecraft 또는 버전 포함 경로) */
    private static final String MINECRAFT_PREFIX;
    /** OBC 패키지 접두사 (org.bukkit.craftbukkit.버전) */
    private static final String CRAFTBUKKIT_PREFIX;
    /** 클래스 이름 내 변수 패턴 ({variable}) */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");

    // 성능 향상을 위한 리플렉션 객체 캐시
    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, FieldAccessor<?>> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, MethodInvoker> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ConstructorInvoker> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    // Primitive 타입과 Wrapper 타입 간의 매핑 (호환성 검사 용도)
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = new HashMap<>();
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = new HashMap<>();

    // 정적 초기화 블록
    static {
        // Primitive 매핑 초기화
        PRIMITIVE_TO_WRAPPER.put(boolean.class, Boolean.class);
        PRIMITIVE_TO_WRAPPER.put(byte.class, Byte.class);
        PRIMITIVE_TO_WRAPPER.put(short.class, Short.class);
        PRIMITIVE_TO_WRAPPER.put(char.class, Character.class);
        PRIMITIVE_TO_WRAPPER.put(int.class, Integer.class);
        PRIMITIVE_TO_WRAPPER.put(long.class, Long.class);
        PRIMITIVE_TO_WRAPPER.put(float.class, Float.class);
        PRIMITIVE_TO_WRAPPER.put(double.class, Double.class);
        PRIMITIVE_TO_WRAPPER.put(void.class, Void.class);

        PRIMITIVE_TO_WRAPPER.forEach((k, v) -> WRAPPER_TO_PRIMITIVE.put(v, k));

        // 버전 감지 로직 (실패 시 안전장치 포함)
        String obcPath;
        String version;
        try {
            VersionUtil.MappingsVersion nmsVersion = VersionUtil.getNmsVersion();
            version = nmsVersion.name();
            switch (nmsVersion) {
                // 최신 버전의 경우 패키지 구조가 변경되었을 수 있음을 고려
                case v1_20_R4, v1_21_R1, v1_21_R2, v1_21_R3, v1_21_R4, v1_21_R5, v1_21_R6, v1_21_R7 ->
                        obcPath = "org.bukkit.craftbukkit";
                default ->
                        obcPath = "org.bukkit.craftbukkit" + (version.isEmpty() ? "" : "." + version);
            }
        } catch (Throwable t) {
            // VersionUtil이 없거나 실패해도 플러그인이 죽지 않도록 처리
            System.err.println("[Reflection] Failed to detect version via VersionUtil. NMS/OBC placeholders might fail.");
            throw new RuntimeException(t);
        }

        VERSION_STRING = version;
        CRAFTBUKKIT_PREFIX = obcPath;
        MINECRAFT_PREFIX = "net.minecraft";
    }

    /**
     * 유틸리티 클래스는 인스턴스화할 수 없습니다.
     */
    private Reflection() {
        throw new UnsupportedOperationException("Reflection utility class cannot be instantiated.");
    }

    // =================================================================================
    // Interfaces
    // =================================================================================

    /**
     * 생성자를 호출하기 위한 함수형 인터페이스입니다.
     */
    @FunctionalInterface
    public interface ConstructorInvoker {
        /**
         * 생성자를 호출하여 새 인스턴스를 반환합니다.
         * @param arguments 생성자 인자
         * @return 생성된 객체
         */
        Object invoke(Object... arguments);
    }

    /**
     * 메서드를 호출하기 위한 함수형 인터페이스입니다.
     */
    @FunctionalInterface
    public interface MethodInvoker {
        /**
         * 메서드를 호출합니다.
         * @param target 메서드를 호출할 대상 객체 (static 메서드인 경우 null)
         * @param arguments 메서드 인자
         * @return 메서드 반환값
         */
        Object invoke(@Nullable Object target, Object... arguments);
    }

    /**
     * 필드에 접근(Get/Set)하기 위한 인터페이스입니다.
     * @param <T> 필드의 값 타입
     */
    public interface FieldAccessor<T> {
        /**
         * 필드 값을 가져옵니다.
         * @param target 필드를 소유한 객체 (static 필드인 경우 null 허용이나, 내부 구현에 따라 무시될 수 있음)
         * @return 필드 값
         */
        T get(@Nullable Object target);

        /**
         * 필드 값을 설정합니다.
         * @param target 필드를 소유한 객체
         * @param value 설정할 값
         */
        void set(@Nullable Object target, @Nullable T value);

        /**
         * 해당 객체가 이 필드를 가지고 있는지 확인합니다.
         * @param target 대상 객체
         * @return 필드 소유 여부
         */
        boolean hasField(Object target);
    }

    // =================================================================================
    // Class Loading & Version Info
    // =================================================================================

    /**
     * 현재 서버의 NMS 버전 문자열을 반환합니다. (예: "v1_20_R1")
     * @return 버전 문자열
     */
    public static String getVersion() {
        return VERSION_STRING;
    }

    /**
     * 클래스 이름으로 Class 객체를 가져옵니다.
     * <p>
     * <b>플레이스홀더 지원:</b>
     * <ul>
     * <li>{@code {nms}} -> net.minecraft (또는 버전 경로)</li>
     * <li>{@code {obc}} -> org.bukkit.craftbukkit.v1_xx_Rx</li>
     * <li>{@code {version}} -> v1_xx_Rx</li>
     * </ul>
     * 예: {@code getClass("{nms}.world.entity.Entity")}
     * </p>
     *
     * @param name 클래스 이름 (패키지 포함)
     * @return 해당 클래스 객체
     * @throws IllegalArgumentException 클래스를 찾을 수 없는 경우
     */
    public static Class<?> getClass(@NotNull String name) {
        return CLASS_CACHE.computeIfAbsent(expandVariables(name), Reflection::getCanonicalClass);
    }

    /**
     * 내부 클래스(Inner Class)를 가져옵니다.
     * @param parentClass 부모 클래스
     * @param innerClassName 내부 클래스 이름 (Simple Name)
     * @return 내부 클래스 객체
     */
    public static Class<?> getInnerClass(@NotNull Class<?> parentClass, @NotNull String innerClassName) {
        String name = parentClass.getName() + "$" + innerClassName;
        return getClass(name);
    }

    /**
     * 예외를 발생시키지 않고 안전하게 클래스를 가져옵니다.
     * @param name 클래스 이름
     * @return 클래스가 존재하면 Optional에 담아 반환, 없으면 Empty 반환
     */
    public static Optional<Class<?>> getClassSafe(@NotNull String name) {
        try {
            return Optional.of(getClass(name));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 여러 클래스 이름 중 하나라도 존재하는 클래스를 찾아 반환합니다.
     * 버전별로 클래스 이름이 변경된 경우 유용합니다.
     *
     * @param name 첫 번째로 시도할 클래스 이름
     * @param aliases 대체 가능한 클래스 이름 목록
     * @return 발견된 첫 번째 클래스
     * @throws IllegalArgumentException 모든 이름에 대해 클래스를 찾지 못한 경우
     */
    public static Class<?> getClass(@NotNull String name, @NotNull String... aliases) {
        try {
            return getClass(name);
        } catch (Exception e) {
            for (String alias : aliases) {
                try {
                    return getClass(alias);
                } catch (Exception ignored) {}
            }
            throw new IllegalArgumentException("Could not find class: " + name + " or its aliases " + Arrays.toString(aliases));
        }
    }

    /**
     * NMS (net.minecraft) 패키지 하위의 클래스를 가져옵니다.
     * @param name NMS 패키지 내부 경로 (예: "world.entity.Entity")
     * @return NMS 클래스
     */
    public static Class<?> getMinecraftClass(@NotNull String name) {
        return getClass("{nms}." + name);
    }

    /**
     * OBC (org.bukkit.craftbukkit) 패키지 하위의 클래스를 가져옵니다.
     * @param name OBC 패키지 내부 경로 (예: "entity.CraftPlayer")
     * @return OBC 클래스
     */
    public static Class<?> getCraftBukkitClass(@NotNull String name) {
        return getClass("{obc}." + name);
    }

    // =================================================================================
    // Constructors (생성자)
    // =================================================================================

    /**
     * 주어진 인자를 사용하여 클래스의 새 인스턴스를 생성합니다.
     * <p>
     * 이 메서드는 먼저 정확한 타입의 생성자를 찾고, 실패할 경우 호환 가능한(Assignable) 생성자를 검색합니다.
     * </p>
     * @param clazz 대상 클래스
     * @param args 생성자 인자
     * @return 생성된 인스턴스
     * @throws RuntimeException 생성자 호출 실패 시
     */
    public static Object newInstance(@NotNull Class<?> clazz, Object... args) {
        try {
            // 1. 정확한 타입으로 먼저 시도
            Class<?>[] types = toClassArray(args);
            return getConstructor(clazz, types).invoke(args);
        } catch (RuntimeException e) {
            // 2. 실패 시, 호환성(assignable) 검사를 통한 느슨한 검색 시도
            return findAndInvokeConstructor(clazz, args);
        }
    }

    /**
     * 특정 파라미터 타입을 가진 생성자 호출자(Invoker)를 가져옵니다.
     * @param clazz 대상 클래스
     * @param params 생성자 파라미터 타입 목록
     * @return 생성자 호출자
     */
    public static ConstructorInvoker getConstructor(@NotNull Class<?> clazz, Class<?>... params) {
        String cacheKey = clazz.getName() + ":<init>:" + Arrays.toString(params);
        return CONSTRUCTOR_CACHE.computeIfAbsent(cacheKey, k -> {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor(params);
                constructor.setAccessible(true);
                return args -> {
                    try {
                        return constructor.newInstance(args);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to invoke constructor of " + clazz.getName(), e);
                    }
                };
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Constructor not found: " + clazz.getName() + Arrays.toString(params));
            }
        });
    }

    private static Object findAndInvokeConstructor(Class<?> clazz, Object... args) {
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            if (isParametersCompatible(c.getParameterTypes(), args)) {
                c.setAccessible(true);
                try {
                    return c.newInstance(args);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke compatible constructor in " + clazz.getName(), e);
                }
            }
        }
        throw new IllegalArgumentException(formatError("No compatible constructor found", clazz.getName(), "<init>", args));
    }

    // =================================================================================
    // Fields (필드)
    // =================================================================================

    /**
     * 객체의 필드 값을 가져옵니다. (필드 이름 기준)
     * @param target 대상 객체
     * @param fieldName 필드 이름
     * @param <T> 반환 타입
     * @return 필드 값
     */
    public static <T> T getFieldValue(@NotNull Object target, @NotNull String fieldName) {
        return (T) getField(target.getClass(), fieldName, null, 0).get(target);
    }

    /**
     * 클래스의 정적(static) 필드 값을 가져옵니다.
     * @param clazz 대상 클래스
     * @param fieldName 필드 이름
     * @param <T> 반환 타입
     * @return 필드 값
     */
    public static <T> T getStaticFieldValue(@NotNull Class<?> clazz, @NotNull String fieldName) {
        return (T) getField(clazz, fieldName, null, 0).get(null);
    }

    /**
     * 객체의 필드 값을 설정합니다.
     * @param target 대상 객체
     * @param fieldName 필드 이름
     * @param value 설정할 값
     */
    public static void setFieldValue(@NotNull Object target, @NotNull String fieldName, @Nullable Object value) {
        getField(target.getClass(), fieldName, null, 0).set(target, value);
    }

    /**
     * 조건에 맞는 필드에 접근할 수 있는 Accessor를 가져옵니다.
     * <p>
     * 이름, 타입, 순서(index)를 조합하여 필드를 찾을 수 있습니다.
     * 예를 들어, 이름은 모르지만 "두 번째 int 필드"를 찾고 싶다면 {@code getField(cls, null, int.class, 1)}과 같이 사용합니다.
     * </p>
     *
     * @param target 대상 클래스
     * @param name 필드 이름 (null이면 이름 무시)
     * @param fieldType 필드 타입 (null이면 타입 무시)
     * @param index 조건에 맞는 필드 중 몇 번째 필드인지 (0부터 시작)
     * @param <T> 필드 타입
     * @return 필드 Accessor
     * @throws IllegalArgumentException 필드를 찾지 못한 경우
     */
    public static <T> FieldAccessor<T> getField(Class<?> target, @Nullable String name, @Nullable Class<T> fieldType, int index) {
        String cacheKey = target.getName() + ":" + name + ":" + (fieldType == null ? "any" : fieldType.getName()) + ":" + index;
        return (FieldAccessor<T>) FIELD_CACHE.computeIfAbsent(cacheKey, k -> {
            int current = 0;
            for (Field field : getAllFields(target)) {
                boolean nameMatch = name == null || field.getName().equals(name);
                boolean typeMatch = fieldType == null || (fieldType.isAssignableFrom(field.getType()) || field.getType().isAssignableFrom(fieldType));

                if (nameMatch && typeMatch) {
                    if (current++ == index) {
                        return createFieldAccessor(field);
                    }
                }
            }
            throw new IllegalArgumentException(String.format("Field not found: class=%s, name=%s, type=%s, index=%d",
                    target.getName(), name, fieldType, index));
        });
    }

    /**
     * 이름과 타입으로 필드를 찾습니다.
     * @param target 대상 클래스
     * @param name 필드 이름
     * @param fieldType 필드 타입
     * @return 필드 Accessor
     */
    public static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType) {
        return getField(target, name, fieldType, 0);
    }

    /**
     * 타입과 순서로 필드를 찾습니다. (이름 무시)
     * @param target 대상 클래스
     * @param fieldType 필드 타입
     * @param index 순서
     * @return 필드 Accessor
     */
    public static <T> FieldAccessor<T> getField(Class<?> target, Class<T> fieldType, int index) {
        return getField(target, null, fieldType, index);
    }

    /**
     * 이름으로 필드를 찾습니다. (타입 무시)
     * @param target 대상 클래스
     * @param name 필드 이름
     * @return 필드 Accessor (Object 타입)
     */
    public static FieldAccessor<Object> getField(Class<?> target, String name) {
        return getField(target, name, Object.class, 0);
    }

    // =================================================================================
    // Methods (메서드)
    // =================================================================================

    /**
     * 객체의 메서드를 호출합니다.
     * <p>
     * Primitive 타입과 Wrapper 타입 간의 자동 매칭을 지원합니다.
     * </p>
     * @param target 대상 객체
     * @param methodName 메서드 이름
     * @param args 메서드 인자
     * @return 메서드 실행 결과
     */
    public static Object invokeMethod(@NotNull Object target, @NotNull String methodName, Object... args) {
        Class<?>[] types = toClassArray(args);
        try {
            // 1. 정확한 시그니처로 시도 (빠름)
            return getMethod(target.getClass(), methodName, types).invoke(target, args);
        } catch (Exception e) {
            // 2. 실패 시, 호환되는 메서드 검색 (느리지만 확실함)
            return findAndInvokeMethod(target.getClass(), target, methodName, args);
        }
    }

    /**
     * 정적(static) 메서드를 호출합니다.
     * @param clazz 대상 클래스
     * @param methodName 메서드 이름
     * @param args 메서드 인자
     * @return 메서드 실행 결과
     */
    public static Object invokeStaticMethod(@NotNull Class<?> clazz, @NotNull String methodName, Object... args) {
        Class<?>[] types = toClassArray(args);
        try {
            return getMethod(clazz, methodName, types).invoke(null, args);
        } catch (Exception e) {
            return findAndInvokeMethod(clazz, null, methodName, args);
        }
    }

    private static Object findAndInvokeMethod(Class<?> clazz, Object target, String methodName, Object... args) {
        // 모든 메서드 순회 (상속 포함)
        for (Method method : getAllMethods(clazz)) {
            if (method.getName().equals(methodName) && isParametersCompatible(method.getParameterTypes(), args)) {
                method.setAccessible(true);
                try {
                    return method.invoke(target, args);
                } catch (Exception ex) {
                    throw new RuntimeException("Invocation failed for compatible method: " + method, ex);
                }
            }
        }

        // 디버깅: 에러 메시지 생성
        throw new IllegalArgumentException(formatError("Method not found", clazz.getName(), methodName, args));
    }

    /**
     * 특정 이름과 파라미터 타입을 가진 메서드 호출자(Invoker)를 가져옵니다.
     * @param clazz 대상 클래스
     * @param methodName 메서드 이름
     * @param params 파라미터 타입 목록
     * @return 메서드 Invoker
     */
    public static MethodInvoker getMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... params) {
        return getTypedMethod(clazz, methodName, null, params);
    }

    /**
     * 이름, 반환 타입, 파라미터 타입을 모두 고려하여 메서드를 찾습니다.
     * @param clazz 대상 클래스
     * @param methodName 메서드 이름 (null이면 이름 무시)
     * @param returnType 반환 타입 (null이면 타입 무시)
     * @param params 파라미터 타입 목록
     * @return 메서드 Invoker
     */
    public static MethodInvoker getTypedMethod(Class<?> clazz, @Nullable String methodName, @Nullable Class<?> returnType, Class<?>... params) {
        String cacheKey = clazz.getName() + ":" + methodName + ":" + (returnType == null ? "any" : returnType.getName()) + ":" + Arrays.toString(params);
        return METHOD_CACHE.computeIfAbsent(cacheKey, k -> {
            Method found = null;
            Class<?> current = clazz;

            // 계층 구조를 따라가며 메서드 검색
            while (current != null && current != Object.class) {
                try {
                    // 1. getDeclaredMethod 시도
                    Method m = current.getDeclaredMethod(methodName == null ? "" : methodName, params);
                    if ((methodName == null || m.getName().equals(methodName)) &&
                            (returnType == null || returnType.isAssignableFrom(m.getReturnType()))) {
                        found = m;
                        break;
                    }
                } catch (NoSuchMethodException ignored) {
                    // 2. 정확한 매칭 실패 시, 조건부 검색 (타입 호환성 등)
                    for (Method m : current.getDeclaredMethods()) {
                        boolean nameMatch = methodName == null || m.getName().equals(methodName);
                        boolean returnMatch = returnType == null || returnType.isAssignableFrom(m.getReturnType());
                        boolean paramsMatch = Arrays.equals(m.getParameterTypes(), params);

                        if (nameMatch && returnMatch && paramsMatch) {
                            found = m;
                            break;
                        }
                    }
                }
                if (found != null) break;
                current = current.getSuperclass();
            }

            if (found != null) {
                found.setAccessible(true);
                Method finalFound = found;
                return (target, args) -> {
                    try {
                        return finalFound.invoke(target, args);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to invoke method " + finalFound.getName(), e);
                    }
                };
            }
            // 검색 실패 시 캐싱하지 않고 예외 발생 (잘못된 요청일 수 있음)
            METHOD_CACHE.remove(cacheKey);
            throw new IllegalStateException("Method not found: " + methodName + " in " + clazz.getName());
        });
    }

    // =================================================================================
    // Annotations & Enums & Generics
    // =================================================================================

    /**
     * 특정 어노테이션이 붙은 필드 목록을 가져옵니다.
     * @param clazz 대상 클래스
     * @param annotation 찾을 어노테이션 클래스
     * @return 필드 목록
     */
    public static List<Field> getFieldsAnnotatedWith(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getAllFields(clazz).stream().filter(f -> f.isAnnotationPresent(annotation)).collect(Collectors.toList());
    }

    /**
     * 특정 어노테이션이 붙은 첫 번째 메서드를 가져옵니다.
     * @param clazz 대상 클래스
     * @param annotation 찾을 어노테이션 클래스
     * @return 메서드 Optional
     */
    public static Optional<Method> getMethodAnnotatedWith(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getAllMethods(clazz).stream().filter(m -> m.isAnnotationPresent(annotation)).findFirst();
    }

    /**
     * 문자열 이름으로 Enum 상수를 가져옵니다.
     * @param enumClass Enum 클래스
     * @param constantName 상수 이름
     * @param <E> Enum 타입
     * @return Enum 상수
     */
    public static <E extends Enum<E>> E getEnumConstant(Class<?> enumClass, String constantName) {
        if (!enumClass.isEnum()) throw new IllegalArgumentException(enumClass.getName() + " is not an enum.");
        try {
            return Enum.valueOf((Class<E>) enumClass, constantName);
        } catch (Exception e) {
            throw new RuntimeException("Enum constant " + constantName + " not found in " + enumClass.getName(), e);
        }
    }

    /**
     * 필드의 제네릭 타입 정보를 가져옵니다.
     * (예: {@code List<String>} 필드에서 {@code String.class}를 추출)
     * @param field 대상 필드
     * @param index 제네릭 인자의 인덱스 (0부터 시작)
     * @return 제네릭 타입 클래스 (실패 시 Object.class)
     */
    public static Class<?> getFieldGenericType(Field field, int index) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) genericType).getActualTypeArguments();
            if (index >= 0 && index < args.length) {
                if (args[index] instanceof Class) {
                    return (Class<?>) args[index];
                }
            }
        }
        return Object.class;
    }

    /**
     * Fluent API: 리플렉션을 사용할 대상 객체를 지정합니다.
     * @param target 대상 인스턴스
     * @return ReflectedObject 래퍼
     */
    public static ReflectedObject on(@NotNull Object target) {
        return new ReflectedObject(target);
    }

    /**
     * Fluent API: 리플렉션을 사용할 대상 클래스(static)를 지정합니다.
     * @param clazz 대상 클래스
     * @return ReflectedObject 래퍼
     */
    public static ReflectedObject on(@NotNull Class<?> clazz) {
        return new ReflectedObject(clazz);
    }

    /**
     * Fluent API: 클래스 이름을 통해 리플렉션 대상을 지정합니다.
     * @param className 클래스 이름 (플레이스홀더 지원)
     * @return ReflectedObject 래퍼
     */
    public static ReflectedObject at(@NotNull String className) {
        return on(getClass(className));
    }

    /**
     * 리플렉션 작업을 메서드 체이닝 방식으로 수행하기 위한 래퍼 클래스입니다.
     */
    public static class ReflectedObject {
        private final Object target;
        private final Class<?> clazz;
        private final boolean isStatic;

        private ReflectedObject(Object target) {
            this.target = target;
            this.clazz = target instanceof Class<?> c ? c : target.getClass();
            this.isStatic = target instanceof Class<?>;
        }

        /**
         * 현재 래핑된 원본 객체를 가져옵니다.
         * @param <T> 반환 타입 캐스팅
         * @return 원본 객체
         */
        public <T> T get() {
            return (T) target;
        }

        /**
         * 현재 객체를 특정 인터페이스의 구현체(Proxy)로 만듭니다.
         * 리플렉션으로 접근해야 하는 객체를 인터페이스로 정의하여 마치 일반 객체처럼 사용할 때 유용합니다.
         *
         * @param interfaceClass 구현할 인터페이스
         * @param <P> 인터페이스 타입
         * @return 프록시 객체
         */
        public <P> P as(Class<P> interfaceClass) {
            return (P) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, (proxy, method, args) -> {
                try {
                    return invokeMethod(isStatic ? clazz : target, method.getName(), args);
                } catch (Exception e) {
                    if (method.getName().equals("toString")) return "ReflectedProxy:" + target.toString();
                    if (method.getName().equals("hashCode")) return target.hashCode();
                    if (method.getName().equals("equals") && args.length == 1) return target.equals(args[0]);
                    throw e;
                }
            });
        }

        /**
         * 필드에 접근하기 위한 체이닝 객체를 반환합니다.
         * @param name 필드 이름
         * @return ReflectedField
         */
        public ReflectedField field(@NotNull String name) {
            return new ReflectedField(this, name);
        }

        /**
         * 타입과 인덱스로 필드에 접근합니다.
         * @param type 필드 타입
         * @param index 순서
         * @return ReflectedField
         */
        public <T> ReflectedField field(@NotNull Class<T> type, int index) {
            return new ReflectedField(this, getField(clazz, type, index));
        }

        /**
         * 메서드를 호출하고 결과값을 다시 ReflectedObject로 감싸 반환합니다.
         * @param name 메서드 이름
         * @return 결과값의 ReflectedObject
         */
        public ReflectedObject call(@NotNull String name) {
            return new ReflectedObject(invokeMethod(isStatic ? clazz : target, name));
        }

        /**
         * 인자를 사용하여 메서드를 호출합니다.
         * @param name 메서드 이름
         * @param args 인자 목록
         * @return 결과값의 ReflectedObject
         */
        public ReflectedObject call(@NotNull String name, Object... args) {
            if (isStatic) {
                return new ReflectedObject(invokeStaticMethod(clazz, name, args));
            }
            return new ReflectedObject(invokeMethod(target, name, args));
        }

        /**
         * 파라미터 타입을 명시하여 메서드를 호출합니다. (오버로딩 충돌 방지)
         * @param name 메서드 이름
         * @param params 파라미터 타입 배열
         * @param args 인자 목록
         * @return 결과값의 ReflectedObject
         */
        public ReflectedObject callTyped(@NotNull String name, Class<?>[] params, Object... args) {
            return new ReflectedObject(getMethod(clazz, name, params).invoke(isStatic ? null : target, args));
        }
    }

    /**
     * ReflectedObject 내부 필드에 대한 접근을 담당하는 래퍼 클래스입니다.
     */
    public static class ReflectedField {
        private final ReflectedObject parent;
        private final FieldAccessor<Object> accessor;

        private ReflectedField(ReflectedObject parent, String name) {
            this.parent = parent;
            this.accessor = getField(parent.clazz, name);
        }

        private ReflectedField(ReflectedObject parent, FieldAccessor<?> accessor) {
            this.parent = parent;
            this.accessor = (FieldAccessor<Object>) accessor;
        }

        /**
         * 필드 값을 가져옵니다.
         * @param <T> 반환 타입
         * @return 필드 값
         */
        public <T> T value() {
            return (T) accessor.get(parent.isStatic ? null : parent.target);
        }

        /**
         * 필드 값을 가져와 다시 ReflectedObject로 감쌉니다. (체이닝 계속)
         * @return ReflectedObject
         */
        public ReflectedObject object() {
            return new ReflectedObject(value());
        }

        /**
         * 필드 값을 설정하고, 다시 필드 래퍼를 반환합니다.
         * @param value 설정할 값
         * @return 자기 자신 (ReflectedField)
         */
        public ReflectedField set(Object value) {
            accessor.set(parent.isStatic ? null : parent.target, value);
            return this;
        }
    }

    /**
     * 내부 캐시를 모두 비웁니다.
     * 플러그인 리로드 시 메모리 누수 방지를 위해 호출할 수 있습니다.
     */
    public static void clearCache() {
        CLASS_CACHE.clear();
        FIELD_CACHE.clear();
        METHOD_CACHE.clear();
        CONSTRUCTOR_CACHE.clear();
    }

    // =================================================================================
    // Internal Helper Methods (내부 로직)
    // =================================================================================

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            Collections.addAll(fields, current.getDeclaredFields());
            current = current.getSuperclass();
        }
        return fields;
    }

    private static List<Method> getAllMethods(Class<?> type) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            Collections.addAll(methods, current.getDeclaredMethods());
            current = current.getSuperclass();
        }
        return methods;
    }

    /**
     * 필드 접근자를 생성하며, final 수정자를 제거하는 안전한 로직을 수행합니다.
     */
    private static <T> FieldAccessor<T> createFieldAccessor(Field field) {
        field.setAccessible(true);
        // Java 17+ 대응: modifiers 필드 접근은 InaccessibleObjectException을 유발하므로 try-catch로 감쌉니다.
        // 대부분의 경우 setAccessible(true)만으로 private 필드 수정이 가능하지만, static final은 어려울 수 있습니다.
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}

        return new FieldAccessor<>() {
            public T get(Object target) {
                try {
                    return (T) field.get(target);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot access field " + field.getName(), e);
                }
            }
            @Override
            public void set(Object target, T value) {
                try {
                    field.set(target, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(String.format("Could not set field '%s' in %s. (Is it a static final field in Java 17+?)", field.getName(), field.getDeclaringClass().getSimpleName()), e);
                }
            }
            @Override
            public boolean hasField(Object target) {
                return field.getDeclaringClass().isAssignableFrom(target.getClass());
            }
        };
    }

    private static Class<?> getCanonicalClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found: " + name, e);
        }
    }

    private static String expandVariables(String name) {
        if (name == null || name.isEmpty()) return name;

        // 1. 모든 변수를 대응되는 값으로 치환
        String result = name;

        // Matcher를 써도 좋지만, 가독성과 안정성을 위해 순차적 치환 후 정리 방식을 권장합니다.
        if (result.contains("{nms}")) {
            result = result.replace("{nms}", MINECRAFT_PREFIX);
        }
        if (result.contains("{obc}")) {
            result = result.replace("{obc}", CRAFTBUKKIT_PREFIX);
        }
        if (result.contains("{version}")) {
            result = result.replace("{version}", VERSION_STRING);
        }

        // 2. 혹시 발생했을지 모르는 중복 점(..) 처리
        // 예: {nms}.Entity -> net.minecraft..Entity 가 된 경우를 대비
        while (result.contains("..")) {
            result = result.replace("..", ".");
        }

        // 3. (선택사항) {nms}Entity 처럼 점을 빼먹고 쓴 경우 보정 로직
        // 패키지 접두사와 클래스 이름 사이에 점이 없다면 자동으로 삽입
        // 단, MINECRAFT_PREFIX 자체가 비어있지 않을 때만 수행
        if (name.startsWith("{nms}") && !result.startsWith(MINECRAFT_PREFIX + ".")) {
            result = result.replace(MINECRAFT_PREFIX, MINECRAFT_PREFIX + ".");
        }
        if (name.startsWith("{obc}") && !result.startsWith(CRAFTBUKKIT_PREFIX + ".")) {
            result = result.replace(CRAFTBUKKIT_PREFIX, CRAFTBUKKIT_PREFIX + ".");
        }

        // 최종 정리: 다시 한 번 중복 점 제거 및 앞뒤 공백 제거
        return result.replace("..", ".").trim();
    }

    private static Class<?>[] toClassArray(Object[] args) {
        Class<?>[] types = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i] == null ? Object.class : args[i].getClass();
        }
        return types;
    }

    /**
     * 파라미터 타입과 인자 값들의 호환성을 검사합니다. (Auto-boxing, Inheritance 고려)
     */
    private static boolean isParametersCompatible(Class<?>[] paramTypes, Object[] args) {
        if (paramTypes.length != args.length) return false;
        for (int i = 0; i < paramTypes.length; i++) {
            Object arg = args[i];
            Class<?> paramType = paramTypes[i];

            if (arg == null) {
                // Primitive 타입은 null 불가
                if (paramType.isPrimitive()) return false;
                continue;
            }

            Class<?> argType = arg.getClass();

            // 1. 직접 할당 가능 (상속 관계)
            if (paramType.isAssignableFrom(argType)) continue;

            // 2. Wrapper <-> Primitive 호환성
            if (paramType.isPrimitive()) {
                Class<?> wrapper = PRIMITIVE_TO_WRAPPER.get(paramType);
                if (wrapper != null && wrapper.isAssignableFrom(argType)) continue;
            } else if (argType.isPrimitive()) {
                Class<?> prim = WRAPPER_TO_PRIMITIVE.get(paramType);
                if (prim != null && prim == argType) continue;
            }

            return false; // 매칭 실패
        }
        return true;
    }

    /**
     * 디버깅을 위한 에러 메시지 포맷팅
     */
    private static String formatError(String message, String className, String memberName, Object[] args) {
        String argTypes = Arrays.stream(args).map(o -> o == null ? "null" : o.getClass().getSimpleName()).collect(Collectors.joining(", "));
        return String.format("%s: %s.%s(%s)", message, className, memberName, argTypes);
    }
}
