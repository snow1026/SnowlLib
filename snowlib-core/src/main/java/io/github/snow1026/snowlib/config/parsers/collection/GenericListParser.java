package io.github.snow1026.snowlib.config.parsers.collection;

import io.github.snow1026.snowlib.config.parsers.GenericConfigParser;
import io.github.snow1026.snowlib.config.parsers.ParserUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GenericListParser implements GenericConfigParser<List<?>> {

    @Override
    public boolean supports(Type type) {
        return type instanceof ParameterizedType p && p.getRawType().equals(List.class);
    }

    @Override
    public List<?> parse(Object raw, Type type) {
        if (!(raw instanceof List<?> list))
            return List.of();

        ParameterizedType pt = (ParameterizedType) type;
        Type elementType = pt.getActualTypeArguments()[0];

        List<Object> result = new ArrayList<>();

        for (Object o : list) {
            result.add(ParserUtil.parseGeneric(o, elementType));
        }
        return result;
    }
}
