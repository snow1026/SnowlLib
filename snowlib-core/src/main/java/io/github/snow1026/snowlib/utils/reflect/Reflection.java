package io.github.snow1026.snowlib.utils.reflect;

import io.github.snow1026.snowlib.utils.VersionUtil;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Reflection {
    private static final String VERSION_STRING;
    private static final String MINECRAFT_PREFIX;
    private static final String CRAFTBUKKIT_PREFIX;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");

    static {
        String obcPath;
        VersionUtil.MappingsVersion version = VersionUtil.getNmsVersion();
        switch (version) {
            case v1_20_R4, v1_21_R1, v1_21_R2, v1_21_R3, v1_21_R4, v1_21_R5, v1_21_R6, v1_21_R7 -> obcPath = "org.bukkit.craftbukkit";
            default -> obcPath = "org.bukkit.craftbukkit" + version.name();
        }
        VERSION_STRING = version.name();
        CRAFTBUKKIT_PREFIX = obcPath;
        MINECRAFT_PREFIX = "net.minecraft";
    }

    private Reflection() {}

    public static String getVersion() {
        return VERSION_STRING;
    }

    @FunctionalInterface
    public interface ConstructorInvoker {
        Object invoke(Object... arguments);
    }

    @FunctionalInterface
    public interface MethodInvoker {
        Object invoke(@Nullable Object target, Object... arguments);
    }

    public interface FieldAccessor<T> {
        T get(@Nullable Object target);
        void set(@Nullable Object target, @Nullable T value);
        boolean hasField(Object target);
    }

    public static Class<?> getClass(String name) {
        return getCanonicalClass(expandVariables(name));
    }

    public static Class<?> getClass(String name, String... aliases) {
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

    public static Class<?> getMinecraftClass(String name) {
        return getCanonicalClass(MINECRAFT_PREFIX + "." + name);
    }

    public static Class<?> getCraftBukkitClass(String name) {
        return getCanonicalClass(CRAFTBUKKIT_PREFIX + "." + name);
    }

    private static Class<?> getCanonicalClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found: " + name, e);
        }
    }

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

    private static String getVariableReplacement(String name, String variable, Matcher matcher) {
        String replacement = switch (variable.toLowerCase()) {
            case "nms" -> MINECRAFT_PREFIX;
            case "obc" -> CRAFTBUKKIT_PREFIX;
            case "version" -> VERSION_STRING;
            default -> throw new IllegalArgumentException("Unknown variable: " + variable);
        };

        if (!replacement.isEmpty() && matcher.end() < name.length() && name.charAt(matcher.end()) != '.') {
            replacement += ".";
        }
        return replacement;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E getEnumConstant(Class<?> enumClass, String constantName) {
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException(enumClass.getName() + " is not an enum.");
        }
        try {
            return Enum.valueOf((Class<E>) enumClass, constantName);
        } catch (Exception e) {
            throw new RuntimeException("Enum constant " + constantName + " not found in " + enumClass.getName(), e);
        }
    }

    public static Object getEnumConstant(Class<?> enumClass, int index) {
        if (!enumClass.isEnum()) throw new IllegalArgumentException("Not an Enum: " + enumClass.getName());
        Object[] constants = enumClass.getEnumConstants();
        if (index < 0 || index >= constants.length) {
            throw new IndexOutOfBoundsException("Enum index out of bounds: " + index);
        }
        return constants[index];
    }

    public static ConstructorInvoker getConstructor(Class<?> clazz, Class<?>... params) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (Arrays.equals(constructor.getParameterTypes(), params)) {
                constructor.setAccessible(true);
                return args -> {
                    try {
                        return constructor.newInstance(args);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to invoke constructor of " + clazz.getName(), e);
                    }
                };
            }
        }
        throw new IllegalStateException("Constructor not found for " + clazz.getName() + " with params " + Arrays.toString(params));
    }

    public static MethodInvoker getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        return getTypedMethod(clazz, methodName, null, params);
    }

    public static MethodInvoker getTypedMethod(Class<?> clazz, @Nullable String methodName, @Nullable Class<?> returnType, Class<?>... params) {
        for (Method method : clazz.getDeclaredMethods()) {
            if ((methodName == null || method.getName().equals(methodName)) && (returnType == null || method.getReturnType().equals(returnType)) && Arrays.equals(method.getParameterTypes(), params)) {

                method.setAccessible(true);
                return (target, args) -> {
                    try {
                        return method.invoke(target, args);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to invoke method " + method.getName(), e);
                    }
                };
            }
        }
        if (clazz.getSuperclass() != null) return getTypedMethod(clazz.getSuperclass(), methodName, returnType, params);
        throw new IllegalStateException("Method not found in " + clazz.getName());
    }

    public static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType) {
        return getField(target, name, fieldType, 0);
    }

    public static <T> FieldAccessor<T> getField(Class<?> target, Class<T> fieldType, int index) {
        return getField(target, null, fieldType, index);
    }

    private static <T> FieldAccessor<T> getField(Class<?> target, @Nullable String name, Class<T> fieldType, int index) {
        int current = 0;
        for (Field field : target.getDeclaredFields()) {
            if ((name == null || field.getName().equals(name)) && fieldType.isAssignableFrom(field.getType())) {
                if (current++ == index) {
                    field.setAccessible(true);
                    return new FieldAccessor<>() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public T get(Object target) {
                            try { return (T) field.get(target); }
                            catch (IllegalAccessException e) { throw new RuntimeException(e); }
                        }

                        @Override
                        public void set(Object target, T value) {
                            try { field.set(target, value); }
                            catch (IllegalAccessException e) { throw new RuntimeException(e); }
                        }

                        @Override
                        public boolean hasField(Object target) {
                            return field.getDeclaringClass().isAssignableFrom(target.getClass());
                        }
                    };
                }
            }
        }

        if (target.getSuperclass() != null) return getField(target.getSuperclass(), name, fieldType, index);
        throw new IllegalArgumentException("Field not found: " + name + " (index " + index + ") in " + target.getName());
    }
}
