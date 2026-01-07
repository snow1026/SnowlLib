package io.github.snow1026.snowlib.internal.attribute;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.api.attribute.SnowAttribute;
import io.github.snow1026.snowlib.utils.VersionUtil;
import io.github.snow1026.snowlib.utils.reflect.Reflection;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"rawtypes"})
public final class AttributeRegister {
    private static final Class<?> REGISTRIES = Reflection.getMinecraftClass("core.registries.Registries");
    private static final Class<?> RESOURCE_KEY = Reflection.getMinecraftClass("resources.ResourceKey");
    private static final Class<?> MAPPED_REGISTRY = Reflection.getMinecraftClass("core.MappedRegistry");
    private static final Class<?> RANGED_ATTRIBUTE = Reflection.getMinecraftClass("world.entity.ai.attributes.RangedAttribute");
    private static final Class<?> REGISTRATION_INFO = Reflection.getMinecraftClass("core.RegistrationInfo");
    private static final Class<?> RESOURCE_LOCATION;

    private static final Object ATTRIBUTE_REGISTRY;

    private static final Reflection.FieldAccessor<Boolean> FROZEN = Reflection.getField(MAPPED_REGISTRY, "frozen", boolean.class);
    private static final Reflection.FieldAccessor<Map> INTRUSIVE = Reflection.getField(MAPPED_REGISTRY, "unregisteredIntrusiveHolders", Map.class);

    static {
        try {
            Object craftServer = Bukkit.getServer();
            Object server = Reflection.getMethod(Reflection.getCraftBukkitClass("CraftServer"), "getServer").invoke(craftServer);
            Object registryAccess = Reflection.getMethod(server.getClass(), "registryAccess").invoke(server);

            Object attributeKey = Reflection.getField(REGISTRIES, "ATTRIBUTE", RESOURCE_KEY).get(null);

            if (VersionUtil.getNmsVersion() != VersionUtil.MappingsVersion.v1_21_R7) {
                RESOURCE_LOCATION = Reflection.getMinecraftClass("resources.ResourceLocation");
            } else {
                RESOURCE_LOCATION = Reflection.getMinecraftClass("resources.Identifier");
            }

            ATTRIBUTE_REGISTRY = getRegistry(registryAccess, attributeKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AttributeRegister", e);
        }
    }

    private AttributeRegister() {
        throw new UnsupportedOperationException();
    }

    private static Object getRegistry(Object registryAccess, Object key) {
        Object optional = Reflection.getMethod(registryAccess.getClass(), "lookup", RESOURCE_KEY).invoke(registryAccess, key);
        return Reflection.getMethod(Optional.class, "orElseThrow").invoke(optional);
    }

    public static void unfreeze(Object registry) {
        FROZEN.set(registry, false);
        INTRUSIVE.set(registry, null);
    }

    public static void freeze(Object registry) {
        FROZEN.set(registry, true);
    }

    public static void register(SnowAttribute attribute) {
        SnowKey key = attribute.key();
        if (isRegistered(key)) return;

        unfreeze(ATTRIBUTE_REGISTRY);
        try {
            Object location = Reflection.getMethod(RESOURCE_LOCATION, "fromNamespaceAndPath", String.class, String.class).invoke(null, key.root(), key.path());

            Object attributeRegistryResourceKey = Reflection.getField(REGISTRIES, "ATTRIBUTE", RESOURCE_KEY).get(null);
            Object resKey = Reflection.getMethod(RESOURCE_KEY, "create", RESOURCE_KEY, RESOURCE_LOCATION).invoke(null, attributeRegistryResourceKey, location);

            Object nmsAttribute = Reflection.getConstructor(RANGED_ATTRIBUTE, String.class, double.class, double.class, double.class).invoke(attribute.key().getKey(), attribute.def(), attribute.min(), attribute.max());

            Reflection.getMethod(RANGED_ATTRIBUTE, "setSyncable", boolean.class).invoke(nmsAttribute, attribute.sync());

            Object info = Reflection.getField(REGISTRATION_INFO, "BUILT_IN", REGISTRATION_INFO).get(null);

            Reflection.getMethod(MAPPED_REGISTRY, "register", RESOURCE_KEY, Object.class, REGISTRATION_INFO).invoke(ATTRIBUTE_REGISTRY, resKey, nmsAttribute, info);

        } catch (Exception e) {
            throw new RuntimeException("Failed to register attribute: " + key, e);
        } finally {
            freeze(ATTRIBUTE_REGISTRY);
        }
    }

    public static void unregister(SnowKey key) {
        if (!isRegistered(key)) return;

        unfreeze(ATTRIBUTE_REGISTRY);
        try {
            removeInternal(key);
        } finally {
            freeze(ATTRIBUTE_REGISTRY);
        }
    }

    private static void removeInternal(SnowKey key) {
        try {
            Object location = Reflection.getMethod(RESOURCE_LOCATION, "fromNamespaceAndPath", String.class, String.class).invoke(null, key.root(), key.path());

            Map byLocation = Reflection.getField(MAPPED_REGISTRY, "byLocation", Map.class).get(ATTRIBUTE_REGISTRY);
            Map byKey = Reflection.getField(MAPPED_REGISTRY, "byKey", Map.class).get(ATTRIBUTE_REGISTRY);
            Map byValue = Reflection.getField(MAPPED_REGISTRY, "byValue", Map.class).get(ATTRIBUTE_REGISTRY);
            List byId = Reflection.getField(MAPPED_REGISTRY, "byId", List.class).get(ATTRIBUTE_REGISTRY);
            Map toId = Reflection.getField(MAPPED_REGISTRY, "toId", Map.class).get(ATTRIBUTE_REGISTRY);

            Object holder = byLocation.get(location);
            if (holder == null) return;

            Class<?> holderRefClass = Reflection.getMinecraftClass("core.Holder$Reference");
            Object nmsAttribute = null;
            if ((boolean) Reflection.getMethod(holderRefClass, "isBound").invoke(holder)) {
                nmsAttribute = Reflection.getMethod(holderRefClass, "value").invoke(holder);
            }
            Object resKey = Reflection.getMethod(holderRefClass, "key").invoke(holder);

            byLocation.remove(location);
            if (resKey != null) byKey.remove(resKey);
            if (nmsAttribute != null) {
                byValue.remove(nmsAttribute);
                toId.remove(nmsAttribute);
                byId.remove(holder);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to unregister attribute internally: " + key, e);
        }
    }

    public static boolean isRegistered(SnowKey key) {
        try {
            Object location = Reflection.getMethod(RESOURCE_LOCATION, "fromNamespaceAndPath", String.class, String.class).invoke(null, key.root(), key.path());
            Map byLocation = Reflection.getField(MAPPED_REGISTRY, "byLocation", Map.class).get(ATTRIBUTE_REGISTRY);
            return byLocation.containsKey(location);
        } catch (Exception e) {
            return false;
        }
    }
}
