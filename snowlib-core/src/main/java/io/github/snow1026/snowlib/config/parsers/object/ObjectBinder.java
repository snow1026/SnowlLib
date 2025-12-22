package io.github.snow1026.snowlib.config.parsers.object;

import io.github.snow1026.snowlib.annotations.config.ConfigKey;
import io.github.snow1026.snowlib.annotations.config.ConfigRoot;
import io.github.snow1026.snowlib.annotations.config.ConfigSection;
import io.github.snow1026.snowlib.config.ConfigData;
import io.github.snow1026.snowlib.config.ConfigReader;
import io.github.snow1026.snowlib.config.Validator;
import io.github.snow1026.snowlib.config.parsers.ConfigParser;
import io.github.snow1026.snowlib.config.parsers.ParserRegistry;
import io.github.snow1026.snowlib.exceptions.ConfigBindException;

import java.lang.reflect.*;
import java.util.*;

public final class ObjectBinder {

    private ObjectBinder() {}

    public static <T> T bind(ConfigData data, Class<T> type) {
        try {
            T instance = createInstance(type);
            String basePath = resolveBasePath(type, data.getPath());
            ConfigData scoped = ConfigData.create(data.getConfig(), basePath);

            for (Field field : getAllFields(type)) {
                field.setAccessible(true);
                bindField(field, instance, scoped);
            }

            Validator.validate(instance);
            return instance;
        } catch (Exception e) {
            throw new ConfigBindException("Failed to bind config to " + type.getName(), e);
        }
    }

    private static void bindField(Field field, Object instance, ConfigData data) throws IllegalAccessException {
        if (field.isAnnotationPresent(ConfigSection.class)) {
            ConfigSection section = field.getAnnotation(ConfigSection.class);
            ConfigData child = ConfigData.create(data.getConfig(), data.build(section.value()));
            field.set(instance, bind(child, field.getType()));
            return;
        }

        ConfigKey key = field.getAnnotation(ConfigKey.class);
        String path = key != null ? key.value() : field.getName();
        String[] alias = key != null ? key.alias() : new String[0];
        boolean saveDefault = key != null && key.saveDefault();

        Object def = null;
        if (key != null && !key.def().isEmpty()) {
            def = parseDefaultValue(key.def(), field.getType());
        }

        Object value;
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType) {
            value = ConfigReader.read(data, genericType, path, def, saveDefault, alias);
        } else {
            value = ConfigReader.read(data, field.getType(), path, def, saveDefault, alias);
        }

        if (value != null) {
            field.set(instance, value);
        }
    }

    private static Object parseDefaultValue(String defStr, Class<?> type) {
        ConfigParser<?> parser = ParserRegistry.get(type);
        if (parser != null) {
            try {
                return parser.parse(defStr);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static <T> T createInstance(Class<T> type) throws ReflectiveOperationException {
        Constructor<T> ctor = type.getDeclaredConstructor();
        ctor.setAccessible(true);
        return ctor.newInstance();
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    private static String resolveBasePath(Class<?> type, String currentPath) {
        if (!type.isAnnotationPresent(ConfigRoot.class)) return currentPath;
        ConfigSection section = type.getAnnotation(ConfigSection.class);
        if (section == null) return currentPath;

        return (currentPath == null || currentPath.isEmpty()) ? section.value() : currentPath + "." + section.value();
    }
}
