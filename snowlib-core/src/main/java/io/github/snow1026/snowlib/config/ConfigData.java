package io.github.snow1026.snowlib.config;

import io.github.snow1026.snowlib.internal.config.ConfigDataImpl;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public interface ConfigData {

    static ConfigData create(FileConfiguration config, String path) {
        return new ConfigDataImpl(config, path);
    }

    static ConfigData create(FileConfiguration config) {
        return new ConfigDataImpl(config);
    }

    FileConfiguration getConfig();

    String getPath();

    String build(String key);

    void editPath(String path);

    void editPath(EditType type, String... args);

    ConfigurationSection getSection();

    boolean exists();
}
