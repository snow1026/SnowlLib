package io.github.snow1026.snowlib.config.parsers;

public class EnumParser<E extends Enum<E>> implements ConfigParser<E> {

    private final Class<E> type;

    public EnumParser(Class<E> type) {
        this.type = type;
    }

    @Override
    public Class<E> getType() {
        return type;
    }

    @Override
    public E parse(Object raw) {
        if (raw == null) return null;
        return Enum.valueOf(type, raw.toString().toUpperCase());
    }
}
