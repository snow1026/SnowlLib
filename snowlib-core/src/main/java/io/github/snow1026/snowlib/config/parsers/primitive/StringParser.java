package io.github.snow1026.snowlib.config.parsers.primitive;

import io.github.snow1026.snowlib.config.parsers.ConfigParser;

public class StringParser implements ConfigParser<String> {

    @Override
    public Class<String> getType() {
        return String.class;
    }

    public String parse(Object raw) {
        return String.valueOf(raw);
    }
}
