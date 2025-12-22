package io.github.snow1026.snowlib.config.parsers.primitive;

import io.github.snow1026.snowlib.config.parsers.ConfigParser;

public class FloatParser implements ConfigParser<Float> {

    @Override
    public Class<Float> getType() {
        return Float.class;
    }

    @Override
    public Float parse(Object raw) {
        return Float.parseFloat(raw.toString());
    }
}
