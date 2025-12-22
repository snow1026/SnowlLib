package io.github.snow1026.snowlib.config.parsers.primitive;

import io.github.snow1026.snowlib.config.parsers.ConfigParser;

public class LongParser implements ConfigParser<Long> {

    @Override
    public Class<Long> type() {
        return Long.class;
    }

    @Override
    public Long parse(Object raw) {
        return Long.parseLong(raw.toString());
    }
}
