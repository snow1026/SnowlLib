package io.github.snow1026.snowlib.config.loader;

import io.github.snow1026.snowlib.annotations.config.ConfigRoot;
import io.github.snow1026.snowlib.config.ConfigData;
import io.github.snow1026.snowlib.config.Validator;
import io.github.snow1026.snowlib.config.parsers.object.ObjectBinder;
import io.github.snow1026.snowlib.exceptions.ConfigBindException;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigLoader {

    private ConfigLoader() {}

    public static <T> T load(FileConfiguration config, Class<T> rootType) {
        if (!rootType.isAnnotationPresent(ConfigRoot.class)) {
            throw new ConfigBindException(rootType.getName() + " is not annotated with @ConfigRoot", null);
        }

        T root = ObjectBinder.bind(ConfigData.create(config), rootType);
        Validator.validate(root);
        return root;
    }
}
