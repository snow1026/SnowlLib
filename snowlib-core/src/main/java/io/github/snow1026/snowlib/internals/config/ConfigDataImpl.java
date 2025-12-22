package io.github.snow1026.snowlib.internals.config;

import io.github.snow1026.snowlib.config.ConfigData;
import io.github.snow1026.snowlib.config.EditType;
import io.github.snow1026.snowlib.exceptions.ConfigPathEditException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigDataImpl implements ConfigData {
    private final FileConfiguration config;
    private String path;

    public ConfigDataImpl(FileConfiguration config, String path) {
        if (config == null) throw new IllegalArgumentException("config cannot be null");

        this.config = config;
        this.path = normalize(path);
    }

    public ConfigDataImpl(FileConfiguration config) {
        if (config == null) throw new IllegalArgumentException("config cannot be null");

        this.config = config;
        this.path = normalize(null);
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String build(String key) {
        if (key == null || key.isEmpty()) return path;
        return path == null || path.isEmpty() ? key : path + "." + key;
    }

    @Override
    public void editPath(String newPath) {
        this.path = normalize(newPath);
    }

    @Override
    public void editPath(EditType type, String... args) {

        List<String> parts = split(path);

        switch (type) {

            case ADD -> {
                if (args == null || args.length == 0)
                    throw new ConfigPathEditException("ADD requires arguments");
                parts.addAll(Arrays.asList(args));
            }

            case REMOVE -> {
                int count = getCount(args);

                for (int i = 0; i < count && !parts.isEmpty(); i++)
                    parts.removeLast();
            }

            case ALL -> {
                if (args == null || args.length != 1)
                    throw new ConfigPathEditException("ALL requires exactly one path");
                parts.clear();
                parts.addAll(split(args[0]));
            }

            case MODIFY -> {
                if (args == null || args.length != 2)
                    throw new ConfigPathEditException(
                            "MODIFY requires [from, to]"
                    );

                boolean modified = false;
                for (int i = 0; i < parts.size(); i++) {
                    if (parts.get(i).equals(args[0])) {
                        parts.set(i, args[1]);
                        modified = true;
                    }
                }

                if (!modified)
                    throw new ConfigPathEditException("MODIFY target not found: " + args[0]);
            }
        }

        this.path = String.join(".", parts);
    }

    private static int getCount(String[] args) {
        int count = 1;

        if (args != null && args.length > 0) {
            try {
                count = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new ConfigPathEditException(
                        "REMOVE requires numeric count"
                );
            }
        }

        if (count < 1)
            throw new ConfigPathEditException("REMOVE count must be >= 1");
        return count;
    }

    @Override
    public ConfigurationSection getSection() {
        return path == null || path.isEmpty() ? config : config.getConfigurationSection(path);
    }

    @Override
    public boolean exists() {
        return path != null && config.contains(path);
    }

    private static String normalize(String path) {
        if (path == null) return "";
        return path.startsWith(".") ? path.substring(1) : path;
    }

    private static List<String> split(String path) {
        if (path == null || path.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(path.split("\\.")));
    }

    @Override
    public String toString() {
        return "ConfigData[path=" + path + "]";
    }
}
