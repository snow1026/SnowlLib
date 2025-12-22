package io.github.snow1026.snowlib.config.parsers.primitive;

import io.github.snow1026.snowlib.config.parsers.ConfigParser;

import java.util.UUID;

public class UUIDParser implements ConfigParser<UUID> {

    @Override
    public Class<UUID> getType() {
        return UUID.class;
    }

    public UUID parse(Object raw) {
        return UUID.fromString(raw.toString());
    }
}
