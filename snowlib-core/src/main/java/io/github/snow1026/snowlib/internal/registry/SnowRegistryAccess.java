package io.github.snow1026.snowlib.internal.registry;

import io.github.snow1026.snowlib.registry.MappedRegistry;
import io.github.snow1026.snowlib.registry.Registrable;
import io.github.snow1026.snowlib.registry.RegistryAccess;
import io.github.snow1026.snowlib.registry.RegistryKey;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SnowRegistryAccess implements RegistryAccess {
    private static final Map<RegistryKey, MappedRegistry<?>> registries = new ConcurrentHashMap<>();

    public static <T extends Registrable> void registerRegistry(RegistryKey key, MappedRegistry<T> registry) {
        registries.put(key, registry);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Registrable> MappedRegistry<T> lookup(RegistryKey key) {
        MappedRegistry<?> registry = registries.get(key);
        if (registry == null) {
            throw new IllegalArgumentException("Registry not found for key: " + key.key());
        }
        return (MappedRegistry<T>) registry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Registrable> Optional<MappedRegistry<T>> find(RegistryKey key) {
        return Optional.ofNullable((MappedRegistry<T>) registries.get(key));
    }
}
