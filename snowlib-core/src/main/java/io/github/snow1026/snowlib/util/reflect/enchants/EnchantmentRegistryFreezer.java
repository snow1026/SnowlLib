package io.github.snow1026.snowlib.util.reflect.enchants;

import io.github.snow1026.snowlib.util.reflect.Reflection;
import java.util.Map;

@SuppressWarnings({"rawtypes"})
public final class EnchantmentRegistryFreezer {
    private static final Class<?> MAPPED_REGISTRY = Reflection.getMinecraftClass("core.MappedRegistry");
    private static final Reflection.FieldAccessor<Boolean> FROZEN = Reflection.getField(MAPPED_REGISTRY, "frozen", boolean.class);
    private static final Reflection.FieldAccessor<Map> INTRUSIVE = Reflection.getField(MAPPED_REGISTRY, "unregisteredIntrusiveHolders", Map.class);

    private EnchantmentRegistryFreezer() {}

    public static void unfreeze(Object registry) {
        FROZEN.set(registry, false);
        INTRUSIVE.set(registry, null);
    }

    public static void freeze(Object registry) {
        FROZEN.set(registry, true);
    }
}
