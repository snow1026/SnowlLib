package io.github.snow1026.snowlib.config.loader;

import io.github.snow1026.snowlib.config.cache.ConfigCache;

import java.util.ArrayList;
import java.util.List;

public final class ConfigWatcher {

    private static final List<ConfigReloadListener> LISTENERS = new ArrayList<>();

    private ConfigWatcher() {}

    public static void register(ConfigReloadListener listener) {
        LISTENERS.add(listener);
    }

    public static void reload() {
        ConfigCache.clear();
        LISTENERS.forEach(ConfigReloadListener::onReload);
    }
}
