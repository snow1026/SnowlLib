package io.github.snow1026.snowlib.config.parsers.object;

import io.github.snow1026.snowlib.config.ConfigData;
import io.github.snow1026.snowlib.config.parsers.GenericConfigParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ObjectParser<T> implements GenericConfigParser<T> {

    private final Class<T> type;

    public ObjectParser(Class<T> type) {
        this.type = type;
    }

    @Override
    public boolean supports(java.lang.reflect.Type t) {
        return t instanceof Class<?> c && c.equals(type);
    }

    @Override
    public T parse(Object raw, java.lang.reflect.Type t) {
        if (!(raw instanceof ConfigurationSection section))
            return null;

        return ObjectBinder.bind(ConfigData.create((FileConfiguration) section), type);
    }
}
