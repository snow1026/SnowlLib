package io.github.snow1026.snowlib.exceptions;

public class ConfigParserNotFoundException extends ConfigException{

    public ConfigParserNotFoundException(Class<?> type) {
        super("No ConfigParser registered for type: " + type.getName());
    }
}
