package io.github.snow1026.snowlib.config.parsers.collection;

import io.github.snow1026.snowlib.config.parsers.GenericConfigParser;
import io.github.snow1026.snowlib.config.parsers.ParserUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GenericMapParser implements GenericConfigParser<Map<?, ?>> {

    @Override
    public boolean supports(Type type) {
        return type instanceof ParameterizedType p && p.getRawType().equals(Map.class);
    }

    @Override
    public Map<?, ?> parse(Object raw, Type type) {
        if (!(raw instanceof ConfigurationSection section))
            return Map.of();

        ParameterizedType pt = (ParameterizedType) type;
        Type keyType = pt.getActualTypeArguments()[0];
        Type valueType = pt.getActualTypeArguments()[1];

        Map<Object, Object> map = new HashMap<>();

        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            Object parsedKey = ParserUtil.parseGeneric(key, keyType);
            Object parsedValue = ParserUtil.parseGeneric(value, valueType);
            map.put(parsedKey, parsedValue);
        }
        return map;
    }
}
