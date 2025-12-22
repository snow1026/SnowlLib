package io.github.snow1026.snowlib.config.parsers.collection;


import io.github.snow1026.snowlib.config.parsers.GenericConfigParser;
import io.github.snow1026.snowlib.config.parsers.ParserUtil;

import java.lang.reflect.*;
import java.util.*;

public class GenericSetParser implements GenericConfigParser<Set<?>> {

    @Override
    public boolean supports(Type type) {
        return type instanceof ParameterizedType p && p.getRawType().equals(Set.class);
    }

    @Override
    public Set<?> parse(Object raw, Type type) {
        if (!(raw instanceof Collection<?> c))
            return Set.of();

        ParameterizedType pt = (ParameterizedType) type;
        Type elementType = pt.getActualTypeArguments()[0];

        Set<Object> result = new HashSet<>();
        for (Object o : c) {
            result.add(ParserUtil.parseGeneric(o, elementType));
        }
        return result;
    }
}
