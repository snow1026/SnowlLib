package io.github.snow1026.snowlib.config.parsers.object;

import io.github.snow1026.snowlib.config.ConfigData;
import io.github.snow1026.snowlib.config.parsers.GenericConfigParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Type;

public record ObjectParser<T>(Class<T> type) implements GenericConfigParser<T> {

    @Override
    public boolean supports(Type t) {
        return t instanceof Class<?> c && c.equals(type);
    }

    @Override
    public T parse(Object raw, Type t) {
        if (!(raw instanceof ConfigurationSection section))
            return null;

        return ObjectBinder.bind(ConfigData.create((FileConfiguration) section), type);
    }
}
