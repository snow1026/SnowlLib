package io.github.snow1026.snowlib.config.parsers.object;

import io.github.snow1026.snowlib.annotation.config.ConfigKey;
import io.github.snow1026.snowlib.annotation.config.ConfigRoot;
import io.github.snow1026.snowlib.annotation.config.ConfigSection;
import io.github.snow1026.snowlib.config.ConfigData;
import io.github.snow1026.snowlib.config.ConfigReader;
import io.github.snow1026.snowlib.config.Validator;
import io.github.snow1026.snowlib.config.parsers.ConfigParser;
import io.github.snow1026.snowlib.config.parsers.ParserRegistry;
import io.github.snow1026.snowlib.exception.ConfigBindException;
import io.github.snow1026.snowlib.util.reflect.Reflection; // 추가

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@SuppressWarnings("unchecked")
public final class ObjectBinder {

    private ObjectBinder() {}

    public static <T> T bind(ConfigData data, Class<T> type) {
        try {
            T instance = (T) Reflection.getConstructor(type).invoke();

            String basePath = resolveBasePath(type, data.getPath());
            ConfigData scoped = ConfigData.create(data.getConfig(), basePath);

            for (Field field : type.getDeclaredFields()) {
                bindField(field, instance, scoped);
            }

            Validator.validate(instance);
            return instance;
        } catch (Exception e) {
            throw new ConfigBindException("Failed to bind config to " + type.getName(), e);
        }
    }

    private static void bindField(Field field, Object instance, ConfigData data) {
        Reflection.FieldAccessor<Object> accessor = Reflection.getField(instance.getClass(), field.getName(), (Class<Object>) field.getType());

        if (field.isAnnotationPresent(ConfigSection.class)) {
            ConfigSection section = field.getAnnotation(ConfigSection.class);
            ConfigData child = ConfigData.create(data.getConfig(), data.build(section.value()));
            accessor.set(instance, bind(child, field.getType()));
            return;
        }

        ConfigKey key = field.getAnnotation(ConfigKey.class);
        String path = key != null ? key.value() : field.getName();
        String[] alias = key != null ? key.alias() : new String[0];
        boolean saveDefault = key != null && key.saveDefault();

        Object def = (key != null && !key.def().isEmpty()) ? parseDefaultValue(key.def(), field.getType()) : null;

        Type genericType = field.getGenericType();
        Object value = ConfigReader.read(data, genericType instanceof ParameterizedType ? genericType : field.getType(), path, def, saveDefault, alias);

        if (value != null) {
            accessor.set(instance, value);
        }
    }

    private static Object parseDefaultValue(String defStr, Class<?> type) {
        ConfigParser<?> parser = ParserRegistry.get(type);
        if (parser != null) {
            try {
                return parser.parse(defStr);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static String resolveBasePath(Class<?> type, String currentPath) {
        if (!type.isAnnotationPresent(ConfigRoot.class)) return currentPath;
        ConfigSection section = type.getAnnotation(ConfigSection.class);
        if (section == null) return currentPath;

        return (currentPath == null || currentPath.isEmpty()) ? section.value() : currentPath + "." + section.value();
    }
}
