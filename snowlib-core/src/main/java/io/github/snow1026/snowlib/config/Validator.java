package io.github.snow1026.snowlib.config;

import io.github.snow1026.snowlib.annotation.config.valid.Max;
import io.github.snow1026.snowlib.annotation.config.valid.Min;
import io.github.snow1026.snowlib.annotation.config.valid.NotNull;
import io.github.snow1026.snowlib.exception.ConfigException;
import io.github.snow1026.snowlib.util.reflect.Reflection;

import java.lang.reflect.Field;

public final class Validator {

    private Validator() {}

    public static void validate(Object obj) {
        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            Reflection.FieldAccessor<Object> accessor = Reflection.getField(clazz, field.getName(), Object.class);
            Object value = accessor.get(obj);

            if (field.isAnnotationPresent(NotNull.class) && value == null) {
                throw new ConfigException("Field " + field.getName() + " in " + clazz.getSimpleName() + " must not be null");
            }

            if (value instanceof Number n) {
                if (field.isAnnotationPresent(Min.class)) {
                    long min = field.getAnnotation(Min.class).value();
                    if (n.longValue() < min)
                        throw new ConfigException(field.getName() + " value " + n + " is less than minimum " + min);
                }
                if (field.isAnnotationPresent(Max.class)) {
                    long max = field.getAnnotation(Max.class).value();
                    if (n.longValue() > max)
                        throw new ConfigException(field.getName() + " value " + n + " exceeds maximum " + max);
                }
            }
        }
    }
}

