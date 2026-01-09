package io.github.snow1026.snowlib.internal.registry;

import io.github.snow1026.snowlib.registry.SnowRegistry;
import io.github.snow1026.snowlib.registry.Registrable;
import io.github.snow1026.snowlib.registry.RegistryAccess;
import io.github.snow1026.snowlib.registry.RegistryKey;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SnowRegistryAccess implements RegistryAccess {
    private static final Map<RegistryKey, SnowRegistry<?>> registries = new ConcurrentHashMap<>();

    public static <T extends Registrable> void registerRegistry(RegistryKey key, SnowRegistry<T> registry) {
        registries.put(key, registry);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Registrable> SnowRegistry<T> lookup(RegistryKey key) {
        SnowRegistry<?> registry = registries.get(key);
        if (registry == null) {
            throw new IllegalArgumentException("Registry not found for key: " + key.key());
        }
        return (SnowRegistry<T>) registry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Registrable> Optional<SnowRegistry<T>> find(RegistryKey key) {
        return Optional.ofNullable((SnowRegistry<T>) registries.get(key));
    }
}
