package io.github.snow1026.snowlib.config.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ConfigCache {

    private static final Map<String, Object> CACHE = new HashMap<>();

    private ConfigCache() {}

    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Supplier<T> loader) {
        return (T) CACHE.computeIfAbsent(key, k -> loader.get());
    }

    public static void invalidate(String key) {
        CACHE.remove(key);
    }

    public static void clear() {
        CACHE.clear();
    }
}
