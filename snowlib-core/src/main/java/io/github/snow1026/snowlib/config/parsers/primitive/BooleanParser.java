package io.github.snow1026.snowlib.config.parsers.primitive;

import io.github.snow1026.snowlib.config.parsers.ConfigParser;

public class BooleanParser implements ConfigParser<Boolean> {

    @Override
    public Class<Boolean> type() {
        return Boolean.class;
    }

    public Boolean parse(Object raw) {
        return Boolean.parseBoolean(raw.toString());
    }
}
