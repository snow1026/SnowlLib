package io.github.snow1026.snowlib.exception;

public class ConfigException extends RuntimeException {

    public ConfigException(String msg) {
        super(msg);
    }

    public ConfigException(String msg, Throwable t) {
        super(msg, t);
    }
}
