package io.github.snow1026.snowlib.util.reflect.enchants;

import io.github.snow1026.snowlib.util.reflect.Reflection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class EnchantmentRegistryFreezer {
    private static final Class<?> MAPPED_REGISTRY_CLASS = Reflection.getMinecraftClass("core.MappedRegistry");
    private static final Reflection.FieldAccessor<Boolean> FROZEN = Reflection.getField(MAPPED_REGISTRY_CLASS, "l", boolean.class);
    private static final Reflection.FieldAccessor<Map> INTRUSIVE = Reflection.getField(MAPPED_REGISTRY_CLASS, "m", Map.class);
    private static final Reflection.FieldAccessor<Map> FROZEN_TAGS = Reflection.getField(MAPPED_REGISTRY_CLASS, "j", Map.class);
    private static final Reflection.FieldAccessor<Object> ALL_TAGS = Reflection.getField(MAPPED_REGISTRY_CLASS, "k", Object.class);

    private static final Class<?> TAG_SET_CLASS = Reflection.getClass("net.minecraft.core.MappedRegistry$a");
    private static final Reflection.FieldAccessor<Map> TAG_SET_MAP = Reflection.getField(Object.class, "a", Map.class);
    private static final Reflection.MethodInvoker TAG_SET_UNBOUND = Reflection.getMethod(TAG_SET_CLASS, "a");

    public static void unfreeze(Object registry) {
        FROZEN.set(registry, false);
        INTRUSIVE.set(registry, new IdentityHashMap<>());
    }

    public static void freeze(Object registry) {
        Object originalTagSet = ALL_TAGS.get(registry);
        Map frozenTagsMap = FROZEN_TAGS.get(registry);
        Map originalMap = TAG_SET_MAP.get(originalTagSet);
        Map copiedMap = new HashMap<>(originalMap);

        copiedMap.forEach(frozenTagsMap::putIfAbsent);

        Object unbound = TAG_SET_UNBOUND.invoke(registry);
        ALL_TAGS.set(registry, unbound);

        Reflection.getMethod(MAPPED_REGISTRY_CLASS, "m").invoke(registry);

        frozenTagsMap.forEach(copiedMap::putIfAbsent);
        TAG_SET_MAP.set(originalTagSet, copiedMap);
        ALL_TAGS.set(registry, originalTagSet);
    }
}
