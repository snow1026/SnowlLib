package io.github.snow1026.snowlib.util.reflect;

import io.github.snow1026.snowlib.util.VersionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Java 리플렉션(Reflection)을 쉽고 안전하게 사용하기 위한 유틸리티 클래스입니다.
 * <p>
 * 이 클래스는 마인크래프트(Bukkit/Spigot) 플러그인 개발 환경에 최적화되어 있으며,
 * 버전별 NMS(net.minecraft) 및 OBC(org.bukkit.craftbukkit) 경로를 자동으로 처리합니다.
 * </p>
 * <p>
 * 주요 기능:
 * <ul>
 * <li>클래스, 메서드, 필드, 생성자 캐싱을 통한 성능 최적화</li>
 * <li>{nms}, {obc} 플레이스홀더를 사용한 동적 클래스 로딩</li>
 * <li>Fluent API 스타일의 객체 조작 지원 (ReflectedObject)</li>
 * <li>private 및 final 필드 접근 및 수정 지원</li>
 * </ul>
 * </p>
 */
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

    // 정적 초기화 블록: 서버 버전을 감지하고 패키지 경로를 설정합니다.
    static {
        String obcPath;
        VersionUtil.MappingsVersion version = VersionUtil.getNmsVersion();
        switch (version) {
            // 최신 버전의 경우 패키지 구조가 변경되었을 수 있음을 고려 (여기서는 예시 버전들)
            case v1_20_R4, v1_21_R1, v1_21_R2, v1_21_R3, v1_21_R4, v1_21_R5, v1_21_R6, v1_21_R7 -> obcPath = "org.bukkit.craftbukkit";
            default -> obcPath = "org.bukkit.craftbukkit" + (version.name().isEmpty() ? "" : "." + version.name());
        }
        VERSION_STRING = version.name();
        CRAFTBUKKIT_PREFIX = obcPath;
        // 최신 버전의 마인크래프트는 버전 패키징을 사용하지 않을 수 있으나 기본적으로 net.minecraft를 가리킴
        MINECRAFT_PREFIX = "net.minecraft";
    }

    /**
     * 유틸리티 클래스는 인스턴스화할 수 없습니다.
     */
    private Reflection() {
        throw new UnsupportedOperationException("Reflection utility class cannot be instantiated.");
    }

    /**
     * 생성자를 호출하는 함수형 인터페이스입니다.
     */
    @FunctionalInterface
    public interface ConstructorInvoker {
        /**
         * 생성자를 호출하여 새 인스턴스를 만듭니다.
         *
         * @param arguments 생성자 인자
         * @return 생성된 객체
         */
        Object invoke(Object... arguments);
    }

    /**
     * 메서드를 호출하는 함수형 인터페이스입니다.
     */
    @FunctionalInterface
    public interface MethodInvoker {
        /**
         * 메서드를 호출합니다.
         *
         * @param target    메서드를 호출할 대상 객체 (정적 메서드인 경우 null)
         * @param arguments 메서드 인자
         * @return 메서드 반환값
         */
        Object invoke(@Nullable Object target, Object... arguments);
    }

    /**
     * 필드 값을 읽거나 쓰는 인터페이스입니다.
     *
     * @param <T> 필드의 데이터 타입
     */
    public interface FieldAccessor<T> {
        /**
         * 필드 값을 가져옵니다.
         *
         * @param target 대상 객체 (정적 필드인 경우 null 허용 가능성 있음)
         * @return 필드 값
         */
        T get(@Nullable Object target);

        /**
         * 필드 값을 설정합니다.
         *
         * @param target 대상 객체
         * @param value  설정할 값
         */
        void set(@Nullable Object target, @Nullable T value);

        /**
         * 해당 객체가 이 필드를 가지고 있는지 확인합니다.
         *
         * @param target 대상 객체
         * @return 필드 보유 여부
         */
        boolean hasField(Object target);
    }

    /**
     * 감지된 서버 버전을 반환합니다.
     *
     * @return 버전 문자열 (예: v1_20_R1)
     */
    public static String getVersion() {
        return VERSION_STRING;
    }

    /**
     * 클래스 이름으로 Class 객체를 가져옵니다.
     * <p>
     * 이름에 <code>{nms}</code> 또는 <code>{obc}</code>가 포함된 경우
     * 자동으로 현재 서버 버전에 맞는 패키지 경로로 변환됩니다.
     * </p>
     *
     * @param name 클래스 이름 (예: "{nms}.world.entity.Entity")
     * @return 찾은 Class 객체
     * @throws IllegalArgumentException 클래스를 찾을 수 없는 경우
     */
    public static Class<?> getClass(@NotNull String name) {
        return CLASS_CACHE.computeIfAbsent(expandVariables(name), Reflection::getCanonicalClass);
    }

    /**
     * 예외를 발생시키지 않고 안전하게 클래스를 가져옵니다.
     *
     * @param name 클래스 이름
     * @return 클래스가 존재하면 Optional에 담아 반환, 없으면 empty
     */
    public static Optional<Class<?>> getClassSafe(@NotNull String name) {
        try { return Optional.of(getClass(name)); }
        catch (Exception e) { return Optional.empty(); }
    }

    /**
     * 여러 이름(별칭) 중 하나라도 일치하는 클래스를 찾아 반환합니다.
     *
     * @param name    기본 클래스 이름
     * @param aliases 대체 가능한 클래스 이름 목록
     * @return 찾은 Class 객체
     * @throws IllegalArgumentException 모든 이름에 대해 클래스를 찾을 수 없는 경우
     */
    public static Class<?> getClass(@NotNull String name, @NotNull String... aliases) {
        try {
            return getClass(name);
        } catch (Exception e) {
            for (String alias : aliases) {
                try { return getClass(alias); } catch (Exception ignored) {}
            }
            throw new IllegalArgumentException("Could not find class: " + name + " or its aliases " + Arrays.toString(aliases));
        }
    }

    /**
     * NMS(net.minecraft) 패키지 하위의 클래스를 가져옵니다.
     *
     * @param name 패키지 경로를 포함하지 않은 클래스 이름 또는 하위 경로
     * @return NMS 클래스 객체
     */
    public static Class<?> getMinecraftClass(@NotNull String name) {
        return getClass("{nms}." + name);
    }

    /**
     * OBC(org.bukkit.craftbukkit) 패키지 하위의 클래스를 가져옵니다.
     *
     * @param name 패키지 경로를 포함하지 않은 클래스 이름
     * @return OBC 클래스 객체
     */
    public static Class<?> getCraftBukkitClass(@NotNull String name) {
        return getClass("{obc}." + name);
    }

    /**
     * 해당 클래스의 새 인스턴스를 생성합니다.
     *
     * @param clazz 대상 클래스
     * @param args  생성자 인자
     * @return 생성된 인스턴스
     */
    public static Object newInstance(@NotNull Class<?> clazz, Object... args) {
        Class<?>[] types = toClassArray(args);
        return getConstructor(clazz, types).invoke(args);
    }

    /**
     * 지정된 파라미터 타입을 가진 생성자를 찾아 호출자(Invoker)를 반환합니다.
     * <p>결과는 캐싱됩니다.</p>
     *
     * @param clazz  대상 클래스
     * @param params 생성자 파라미터 타입 목록
     * @return 생성자 호출자 (ConstructorInvoker)
     */
    public static ConstructorInvoker getConstructor(@NotNull Class<?> clazz, Class<?>... params) {
        String cacheKey = clazz.getName() + ":<init>:" + Arrays.toString(params);
        return CONSTRUCTOR_CACHE.computeIfAbsent(cacheKey, k -> {
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (Arrays.equals(constructor.getParameterTypes(), params)) {
                    constructor.setAccessible(true);
                    return args -> {
                        try { return constructor.newInstance(args); }
                        catch (Exception e) { throw new RuntimeException("Failed to invoke constructor of " + clazz.getName(), e); }
                    };
                }
            }
            throw new IllegalStateException("Constructor not found for " + clazz.getName() + " with params " + Arrays.toString(params));
        });
    }

    /**
     * 객체의 필드 값을 가져옵니다.
     *
     * @param target    대상 객체
     * @param fieldName 필드 이름
     * @param <T>       반환 타입
     * @return 필드 값
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(@NotNull Object target, @NotNull String fieldName) {
        return (T) getField(target.getClass(), fieldName, null).get(target);
    }

    /**
     * 클래스의 정적(static) 필드 값을 가져옵니다.
     *
     * @param clazz     대상 클래스
     * @param fieldName 필드 이름
     * @param <T>       반환 타입
     * @return 정적 필드 값
     */
    @SuppressWarnings("unchecked")
    public static <T> T getStaticFieldValue(@NotNull Class<?> clazz, @NotNull String fieldName) {
        return (T) getField(clazz, fieldName, null).get(null);
    }

    /**
     * 객체의 필드 값을 설정합니다.
     *
     * @param target    대상 객체
     * @param fieldName 필드 이름
     * @param value     설정할 값
     */
    public static void setFieldValue(@NotNull Object target, @NotNull String fieldName, @Nullable Object value) {
        getField(target.getClass(), fieldName, null).set(target, value);
    }

    /**
     * 조건에 맞는 필드를 찾아 접근자(Accessor)를 반환합니다.
     * <p>이름, 타입, 인덱스(순서)를 조합하여 필드를 검색할 수 있습니다.</p>
     *
     * @param target    대상 클래스
     * @param name      필드 이름 (null인 경우 이름 무시)
     * @param fieldType 필드 타입 (null인 경우 타입 무시)
     * @param index     매칭되는 필드 중 몇 번째 필드인지 (0부터 시작)
     * @param <T>       필드 타입 제네릭
     * @return 필드 접근자
     */
    @SuppressWarnings("unchecked")
    public static <T> FieldAccessor<T> getField(Class<?> target, @Nullable String name, @Nullable Class<T> fieldType, int index) {
        String cacheKey = target.getName() + ":" + name + ":" + (fieldType == null ? "any" : fieldType.getName()) + ":" + index;
        return (FieldAccessor<T>) FIELD_CACHE.computeIfAbsent(cacheKey, k -> {
            int current = 0;
            for (Field field : getAllFields(target)) {
                boolean nameMatch = name == null || field.getName().equals(name);
                boolean typeMatch = fieldType == null || fieldType.isAssignableFrom(field.getType());

                if (nameMatch && typeMatch) {
                    if (current++ == index) {
                        return createFieldAccessor(field);
                    }
                }
            }
            throw new IllegalArgumentException("Field not found: " + name + " (index " + index + ") in " + target.getName());
        });
    }

    /**
     * 이름과 타입으로 필드 접근자를 가져옵니다.
     *
     * @param target    대상 클래스
     * @param name      필드 이름
     * @param fieldType 필드 타입
     * @param <T>       필드 타입 제네릭
     * @return 필드 접근자
     */
    public static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType) {
        return getField(target, name, fieldType, 0);
    }

    /**
     * 타입과 순서로 필드 접근자를 가져옵니다. (이름을 모르거나 난독화된 경우 유용)
     *
     * @param target    대상 클래스
     * @param fieldType 필드 타입
     * @param index     순서 인덱스
     * @param <T>       필드 타입 제네릭
     * @return 필드 접근자
     */
    public static <T> FieldAccessor<T> getField(Class<?> target, Class<T> fieldType, int index) {
        return getField(target, null, fieldType, index);
    }

    /**
     * 이름으로 필드 접근자를 가져옵니다.
     *
     * @param target 대상 클래스
     * @param name   필드 이름
     * @return Object 타입의 필드 접근자
     */
    public static FieldAccessor<Object> getField(Class<?> target, String name) {
        return getField(target, name, Object.class, 0);
    }

    /**
     * 클래스와 모든 상위 클래스의 선언된 필드를 가져옵니다.
     *
     * @param type 대상 클래스
     * @return 필드 목록
     */
    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * Java의 Field 객체를 래핑하여 접근 권한을 설정하고 final 수정자를 제거한 Accessor를 생성합니다.
     *
     * @param field 원본 필드
     * @param <T>   필드 타입
     * @return 생성된 FieldAccessor
     */
    private static <T> FieldAccessor<T> createFieldAccessor(Field field) {
        field.setAccessible(true);
        try {
            // 'modifiers' 필드를 조작하여 final 속성을 제거 (Java 버전에 따라 작동하지 않을 수 있음)
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new FieldAccessor<>() {
            @Override @SuppressWarnings("unchecked")
            public T get(Object target) {
                try {
                    return (T) field.get(target);
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot access field " + field.getName(), e);
                }
            }
            @Override
            public void set(Object target, T value) {
                try {
                    field.set(target, value);
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot set field " + field.getName(), e);
                }
            }
            @Override
            public boolean hasField(Object target) {
                return field.getDeclaringClass().isAssignableFrom(target.getClass());
            }
        };
    }

    /**
     * 메서드를 호출합니다.
     *
     * @param target     대상 객체
     * @param methodName 메서드 이름
     * @param args       메서드 인자
     * @return 메서드 반환값
     */
    public static Object invokeMethod(@NotNull Object target, @NotNull String methodName, Object... args) {
        Class<?>[] types = toClassArray(args);
        return getMethod(target.getClass(), methodName, types).invoke(target, args);
    }

    /**
     * 정적(static) 메서드를 호출합니다.
     *
     * @param clazz      대상 클래스
     * @param methodName 메서드 이름
     * @param args       메서드 인자
     * @return 메서드 반환값
     */
    public static Object invokeStaticMethod(@NotNull Class<?> clazz, @NotNull String methodName, Object... args) {
        Class<?>[] types = toClassArray(args);
        return getMethod(clazz, methodName, types).invoke(null, args);
    }

    /**
     * 메서드를 찾아 호출자(Invoker)를 반환합니다.
     *
     * @param clazz      대상 클래스
     * @param methodName 메서드 이름
     * @param params     파라미터 타입 목록
     * @return 메서드 호출자
     */
    public static MethodInvoker getMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... params) {
        return getTypedMethod(clazz, methodName, null, params);
    }

    /**
     * 반환 타입과 파라미터를 모두 고려하여 메서드를 찾아 호출자를 반환합니다.
     *
     * @param clazz      대상 클래스
     * @param methodName 메서드 이름 (null 가능)
     * @param returnType 반환 타입 (null 가능)
     * @param params     파라미터 타입 목록
     * @return 메서드 호출자
     */
    public static MethodInvoker getTypedMethod(Class<?> clazz, @Nullable String methodName, @Nullable Class<?> returnType, Class<?>... params) {
        String cacheKey = clazz.getName() + ":" + methodName + ":" + (returnType == null ? "any" : returnType.getName()) + ":" + Arrays.toString(params);
        return METHOD_CACHE.computeIfAbsent(cacheKey, k -> {
            for (Method method : clazz.getDeclaredMethods()) {
                boolean nameMatch = methodName == null || method.getName().equals(methodName);
                boolean returnMatch = returnType == null || method.getReturnType().equals(returnType);
                boolean paramsMatch = Arrays.equals(method.getParameterTypes(), params);

                if (nameMatch && returnMatch && paramsMatch) {
                    method.setAccessible(true);
                    return (target, args) -> {
                        try { return method.invoke(target, args); }
                        catch (Exception e) { throw new RuntimeException("Failed to invoke method " + method.getName(), e); }
                    };
                }
            }
            if (clazz.getSuperclass() != null) return getTypedMethod(clazz.getSuperclass(), methodName, returnType, params);
            throw new IllegalStateException("Method not found in " + clazz.getName());
        });
    }

    /**
     * 특정 어노테이션이 붙은 모든 필드를 가져옵니다.
     *
     * @param clazz      대상 클래스
     * @param annotation 찾을 어노테이션 클래스
     * @return 필드 목록
     */
    public static List<Field> getFieldsAnnotatedWith(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getAllFields(clazz).stream().filter(f -> f.isAnnotationPresent(annotation)).collect(Collectors.toList());
    }

    /**
     * 특정 어노테이션이 붙은 첫 번째 메서드를 가져옵니다.
     *
     * @param clazz      대상 클래스
     * @param annotation 찾을 어노테이션 클래스
     * @return 메서드를 포함한 Optional
     */
    public static Optional<Method> getMethodAnnotatedWith(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(annotation)).findFirst();
    }

    /**
     * 이름으로 Enum 상수를 가져옵니다.
     *
     * @param enumClass    Enum 클래스
     * @param constantName 상수 이름
     * @param <E>          Enum 타입
     * @return Enum 상수
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E getEnumConstant(Class<?> enumClass, String constantName) {
        if (!enumClass.isEnum()) throw new IllegalArgumentException(enumClass.getName() + " is not an enum.");
        try {
            return Enum.valueOf((Class<E>) enumClass, constantName);
        } catch (Exception e) {
            throw new RuntimeException("Enum constant " + constantName + " not found in " + enumClass.getName(), e);
        }
    }

    /**
     * Fluent API 사용을 위해 객체를 래핑합니다.
     *
     * @param target 래핑할 대상 객체
     * @return ReflectedObject
     */
    public static ReflectedObject on(@NotNull Object target) {
        return new ReflectedObject(target);
    }

    /**
     * Fluent API 사용을 위해 클래스(정적 접근용)를 래핑합니다.
     *
     * @param clazz 래핑할 대상 클래스
     * @return ReflectedObject
     */
    public static ReflectedObject on(@NotNull Class<?> clazz) {
        return new ReflectedObject(clazz);
    }

    /**
     * 클래스 이름을 사용하여 Fluent API 객체를 생성합니다.
     *
     * @param className 클래스 이름 (변수 포함 가능)
     * @return ReflectedObject
     */
    public static ReflectedObject at(@NotNull String className) {
        return on(getClass(className));
    }

    /**
     * 리플렉션 작업을 연쇄적으로(Chain) 수행하기 위한 래퍼 클래스입니다.
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
         * 래핑된 원본 객체를 가져옵니다.
         *
         * @param <T> 반환 타입
         * @return 원본 객체
         */
        @SuppressWarnings("unchecked")
        public <T> T get() {
            return (T) target;
        }

        /**
         * 이름으로 필드에 접근합니다.
         *
         * @param name 필드 이름
         * @return ReflectedField
         */
        public ReflectedField field(@NotNull String name) {
            return new ReflectedField(this, name);
        }

        /**
         * 타입과 순서로 필드에 접근합니다.
         *
         * @param type  필드 타입
         * @param index 필드 순서
         * @param <T>   필드 타입 제네릭
         * @return ReflectedField
         */
        public <T> ReflectedField field(@NotNull Class<T> type, int index) {
            return new ReflectedField(this, getField(clazz, type, index));
        }

        /**
         * 메서드를 호출하고 결과를 다시 ReflectedObject로 감싸 반환합니다.
         *
         * @param name 메서드 이름
         * @return 결과가 래핑된 ReflectedObject
         */
        public ReflectedObject call(@NotNull String name) {
            return new ReflectedObject(invokeMethod(isStatic ? clazz : target, name));
        }

        /**
         * 인자와 함께 메서드를 호출하고 결과를 다시 ReflectedObject로 감싸 반환합니다.
         *
         * @param name 메서드 이름
         * @param args 메서드 인자
         * @return 결과가 래핑된 ReflectedObject
         */
        public ReflectedObject call(@NotNull String name, Object... args) {
            Class<?>[] params = toClassArray(args);
            return new ReflectedObject(getMethod(clazz, name, params).invoke(isStatic ? null : target, args));
        }

        /**
         * 파라미터 타입을 명시하여 메서드를 호출합니다. (null 인자가 있을 때 안전함)
         *
         * @param name   메서드 이름
         * @param params 파라미터 타입 배열
         * @param args   메서드 인자
         * @return 결과가 래핑된 ReflectedObject
         */
        public ReflectedObject callTyped(@NotNull String name, Class<?>[] params, Object... args) {
            return new ReflectedObject(getMethod(clazz, name, params).invoke(isStatic ? null : target, args));
        }
    }

    /**
     * Fluent API 내에서 필드 접근을 돕는 래퍼 클래스입니다.
     */
    public static class ReflectedField {
        private final ReflectedObject parent;
        private final FieldAccessor<Object> accessor;

        private ReflectedField(ReflectedObject parent, String name) {
            this.parent = parent;
            this.accessor = getField(parent.clazz, name);
        }

        @SuppressWarnings("unchecked")
        private ReflectedField(ReflectedObject parent, FieldAccessor<?> accessor) {
            this.parent = parent;
            this.accessor = (FieldAccessor<Object>) accessor;
        }

        /**
         * 필드의 실제 값을 가져옵니다.
         *
         * @param <T> 반환 타입
         * @return 필드 값
         */
        @SuppressWarnings("unchecked")
        public <T> T value() {
            return (T) accessor.get(parent.isStatic ? null : parent.target);
        }

        /**
         * 필드 값을 가져와서 다시 ReflectedObject로 감쌉니다. (체이닝용)
         *
         * @return 값이 래핑된 ReflectedObject
         */
        public ReflectedObject object() {
            return new ReflectedObject(value());
        }

        /**
         * 필드 값을 설정합니다.
         *
         * @param value 설정할 값
         * @return 메서드 체이닝을 위한 ReflectedField 자신
         */
        public ReflectedField set(Object value) {
            accessor.set(parent.isStatic ? null : parent.target, value);
            return this;
        }
    }

    /**
     * 클래스 이름을 사용하여 정규 클래스 객체를 로드합니다.
     *
     * @param name 클래스 이름
     * @return Class 객체
     */
    private static Class<?> getCanonicalClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found: " + name, e);
        }
    }

    /**
     * 클래스 이름 문자열 내의 변수({nms}, {obc} 등)를 실제 경로로 확장합니다.
     *
     * @param name 원본 문자열
     * @return 변수가 치환된 문자열
     */
    private static String expandVariables(String name) {
        Matcher matcher = VARIABLE_PATTERN.matcher(name);
        StringBuilder output = new StringBuilder();

        while (matcher.find()) {
            String variable = matcher.group(1);
            String replacement = getVariableReplacement(name, variable, matcher);
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    /**
     * 특정 변수에 대한 치환 값을 결정합니다.
     *
     * @param name     전체 문자열
     * @param variable 변수 이름 (nms, obc, version)
     * @param matcher  현재 매처
     * @return 치환할 문자열
     */
    private static String getVariableReplacement(String name, String variable, Matcher matcher) {
        String replacement = switch (variable.toLowerCase()) {
            case "nms" -> MINECRAFT_PREFIX;
            case "obc" -> CRAFTBUKKIT_PREFIX;
            case "version" -> VERSION_STRING;
            default -> throw new IllegalArgumentException("Unknown variable: " + variable);
        };

        // 변수 뒤에 점(.)이 없으면 자동으로 점을 추가하여 패키지 경로를 완성합니다.
        if (!replacement.isEmpty() && matcher.end() < name.length() && name.charAt(matcher.end()) != '.') {
            replacement += ".";
        }
        return replacement;
    }

    /**
     * 객체 배열을 클래스 배열로 변환합니다. (null 값은 Object.class로 처리)
     *
     * @param args 객체 배열
     * @return 클래스 타입 배열
     */
    private static Class<?>[] toClassArray(Object[] args) {
        Class<?>[] types = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i] == null ? Object.class : args[i].getClass();
        }
        return types;
    }
}
