package io.github.snow1026.snowlib.config.parsers.primitive;

import io.github.snow1026.snowlib.config.parsers.ConfigParser;

public class IntegerParser implements ConfigParser<Integer> {

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    public Integer parse(Object raw){
        return Integer.parseInt(raw.toString());
    }
}
