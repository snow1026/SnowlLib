package io.github.snow1026.snowlib.config;

import io.github.snow1026.snowlib.annotations.config.valid.Max;
import io.github.snow1026.snowlib.annotations.config.valid.Min;
import io.github.snow1026.snowlib.annotations.config.valid.NotNull;
import io.github.snow1026.snowlib.exceptions.ConfigException;

import java.lang.reflect.Field;

public final class Validator {

    private Validator() {}

    public static void validate(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);

                if (field.isAnnotationPresent(NotNull.class) && value == null) {
                    throw new ConfigException("Field " + field.getName() + " must not be null");
                }

                if (value instanceof Number n) {
                    if (field.isAnnotationPresent(Min.class)) {
                        long min = field.getAnnotation(Min.class).value();
                        if (n.longValue() < min) throw new ConfigException(field.getName() + " < Min");
                    }
                    if (field.isAnnotationPresent(Max.class)) {
                        long max = field.getAnnotation(Max.class).value();
                        if (n.longValue() > max) throw new ConfigException(field.getName() + " > Max");
                    }
                }

            } catch (IllegalAccessException ignored) {}
        }
    }
}
