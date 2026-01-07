package io.github.snow1026.snowlib.registry.internal;

import io.github.snow1026.snowlib.registry.MappedRegistry;
import io.github.snow1026.snowlib.registry.Registrable;
import io.github.snow1026.snowlib.registry.RegistryAccess;
import io.github.snow1026.snowlib.registry.RegistryKey;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SnowRegistryAccess implements RegistryAccess {
    private static final Map<RegistryKey, MappedRegistry<?>> registries = new ConcurrentHashMap<>();

    /**
     * 새로운 레지스트리를 시스템에 등록합니다. (라이브러리 초기화 단계에서 사용)
     */
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
