package io.github.snow1026.snowlib.config.parsers;

public record EnumParser<E extends Enum<E>>(Class<E> type) implements ConfigParser<E> {

    @Override
    public E parse(Object raw) {
        if (raw == null) return null;
        return Enum.valueOf(type, raw.toString().toUpperCase());
    }
}
