package io.github.snow1026.snowlib.config.parsers;

public interface ConfigParser<T> {

    Class<T> type();
    T parse(Object raw);
}
