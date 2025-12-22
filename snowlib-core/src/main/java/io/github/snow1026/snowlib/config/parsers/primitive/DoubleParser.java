package io.github.snow1026.snowlib.config.parsers.primitive;

import io.github.snow1026.snowlib.config.parsers.ConfigParser;

public class DoubleParser implements ConfigParser<Double> {

    @Override
    public Class<Double> type() {
        return Double.class;
    }

    public Double parse(Object raw) {
        return Double.parseDouble(raw.toString());
    }
}
