package io.github.snow1026.snowlib.config;

import io.github.snow1026.snowlib.config.parsers.ConfigParser;
import io.github.snow1026.snowlib.config.parsers.ParserRegistry;
import io.github.snow1026.snowlib.config.parsers.ParserUtil;
import io.github.snow1026.snowlib.config.parsers.object.ObjectBinder;
import io.github.snow1026.snowlib.exceptions.ConfigParserNotFoundException;
import io.github.snow1026.snowlib.exceptions.ConfigResultParseException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Type;
import java.util.Optional;

public class ConfigReader {

    private ConfigReader() {}

    public static <T> T read(ConfigData data, Class<T> type, String key, T def, boolean saveDefault, String... alias) {
        Object raw = find(data, key, alias);
        if (raw == null) {
            if (saveDefault) {
                data.getConfig().set(data.build(key), def);
            }
            return def;
        }
        return parse(type, raw);
    }

    public static <T> T read(ConfigData data, Class<T> type, String key, T def, String... alias) {
        return read(data, type, key, def, false, alias);
    }

    public static <T> T read(ConfigData data, Class<T> type, String key, String... alias) {
        Object raw = find(data, key, alias);
        return raw == null ? null : parse(type, raw);
    }

    public static Object read(ConfigData data, String key, String... alias) {
        return find(data, key, alias);
    }

    public static <T> Optional<T> read(ConfigData data, Class<T> type, String key, boolean ignoredOptional, String... alias) {
        Object raw = find(data, key, alias);
        return raw == null ? Optional.empty() : Optional.of(parse(type, raw));
    }

    @SuppressWarnings("unchecked")
    public static <T> T read(ConfigData data, Type type, String key, T def, boolean saveDefault, String... alias) {
        Object raw = find(data, key, alias);
        if (raw == null) {
            if (saveDefault)
                data.getConfig().set(data.build(key), def);
            return def;
        }
        return (T) ParserUtil.parseGeneric(raw, type);
    }

    public static <T> T readSection(ConfigData data, String key, Class<T> type) {
        Object raw = data.getConfig().get(data.build(key));
        if (!(raw instanceof ConfigurationSection section))
            return null;

        ParserRegistry.registerObject(type);
        return ObjectBinder.bind(ConfigData.create((FileConfiguration) section), type);
    }

    private static Object find(ConfigData data, String key, String... alias) {
        FileConfiguration config = data.getConfig();

        Object raw = config.get(data.build(key));
        if (raw != null) return raw;

        for (String a : alias) {
            raw = config.get(data.build(a));
            if (raw != null) return raw;
        }
        return null;
    }

    private static <T> T parse(Class<T> type, Object raw) {
        ConfigParser<T> parser = ParserRegistry.get(type);
        if (parser == null)
            throw new ConfigParserNotFoundException(type);

        try {
            return parser.parse(raw);
        } catch (Exception e) {
            throw new ConfigResultParseException("Failed to parse value [" + raw + "] to " + type.getName(), e);
        }
    }
}
