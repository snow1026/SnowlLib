package io.github.snow1026.snowlib.config.parsers;

import java.lang.reflect.Type;

public interface GenericConfigParser<T> {

    boolean supports(Type type);

    T parse(Object raw, Type type);
}
