package io.github.snow1026.snowlib.internal.enchant;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.api.component.enchantment.EnchantmentComponent;
import io.github.snow1026.snowlib.api.enchantment.SnowEnchantment;
import io.github.snow1026.snowlib.utils.VersionUtil;
import io.github.snow1026.snowlib.utils.reflect.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class EnchantmentRegister {
    private static final Class<?> REGISTRIES = Reflection.getMinecraftClass("core.registries.Registries");
    private static final Class<?> ENCHANTMENT = Reflection.getMinecraftClass("world.item.enchantment.Enchantment");
    private static final Class<?> ENCHANT_DEF = Reflection.getMinecraftClass("world.item.enchantment.Enchantment$EnchantmentDefinition");
    private static final Class<?> ENCHANT_COST = Reflection.getMinecraftClass("world.item.enchantment.Enchantment$Cost");
    private static final Class<?> HOLDER_SET = Reflection.getMinecraftClass("core.HolderSet");
    private static final Class<?> HOLDER_SET_NAMED = Reflection.getMinecraftClass("core.HolderSet$Named");
    private static final Class<?> HOLDER_REF = Reflection.getMinecraftClass("core.Holder$Reference");
    private static final Class<?> TAG_KEY = Reflection.getMinecraftClass("tags.TagKey");
    private static final Class<?> SLOT_GROUP = Reflection.getMinecraftClass("world.entity.EquipmentSlotGroup");
    private static final Class<?> COMPONENT = Reflection.getMinecraftClass("network.chat.Component");
    private static final Class<?> DATA_COMPONENT_MAP = Reflection.getMinecraftClass("core.component.DataComponentMap");
    private static final Class<?> REGISTRATION_INFO = Reflection.getMinecraftClass("core.RegistrationInfo");
    private static final Class<?> RESOURCE_KEY = Reflection.getMinecraftClass("resources.ResourceKey");
    private static final Class<?> MAPPED_REGISTRY = Reflection.getMinecraftClass("core.MappedRegistry");
    private static final Class<?> RESOURCE_LOCATION;

    private static final Object ENCHANT_REGISTRY;
    private static final Object ITEM_REGISTRY;

    private static final Reflection.FieldAccessor<Boolean> FROZEN = Reflection.getField(MAPPED_REGISTRY, "frozen", boolean.class);
    private static final Reflection.FieldAccessor<Map> INTRUSIVE = Reflection.getField(MAPPED_REGISTRY, "unregisteredIntrusiveHolders", Map.class);

    static {
        try {
            Object craftServer = Bukkit.getServer();
            Object server = Reflection.getMethod(Reflection.getCraftBukkitClass("CraftServer"), "getServer").invoke(craftServer);
            Object registryAccess = Reflection.getMethod(server.getClass(), "registryAccess").invoke(server);

            Object enchantKey = Reflection.getField(REGISTRIES, "ENCHANTMENT", RESOURCE_KEY).get(null);
            Object itemKey = Reflection.getField(REGISTRIES, "ITEM", RESOURCE_KEY).get(null);

            if (VersionUtil.getNmsVersion() != VersionUtil.MappingsVersion.v1_21_R7) {
                RESOURCE_LOCATION = Reflection.getMinecraftClass("resources.ResourceLocation");
            } else {
                RESOURCE_LOCATION = Reflection.getMinecraftClass("resources.Identifier");
            }

            ENCHANT_REGISTRY = getRegistry(registryAccess, enchantKey);
            ITEM_REGISTRY = getRegistry(registryAccess, itemKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EnchantmentRegister", e);
        }
    }

    private EnchantmentRegister() {
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

    public static void register(SnowEnchantment snowEnchantment) {
        SnowKey key = snowEnchantment.key();
        unfreeze(ENCHANT_REGISTRY);
        unfreeze(ITEM_REGISTRY);

        try {
            if (isRegistered(key)) return;

            Object location = Reflection.getMethod(RESOURCE_LOCATION, "fromNamespaceAndPath", String.class, String.class).invoke(null, key.root(), key.path());
            Object enchantRegistryResourceKey = Reflection.getField(REGISTRIES, "ENCHANTMENT", RESOURCE_KEY).get(null);
            Object resKey = Reflection.getMethod(RESOURCE_KEY, "create", RESOURCE_KEY, RESOURCE_LOCATION).invoke(null, enchantRegistryResourceKey, location);

            EnchantmentComponent comp = snowEnchantment.component();

            Object supported = createHolderSet(comp.supportedItems().materials());
            Optional primary = comp.primaryItems().map(p -> createHolderSet(p.materials()));
            Object minCost = Reflection.getConstructor(ENCHANT_COST, int.class, int.class).invoke(comp.minCost().base(), comp.minCost().perLevel());
            Object maxCost = Reflection.getConstructor(ENCHANT_COST, int.class, int.class).invoke(comp.maxCost().base(), comp.maxCost().perLevel());
            List slots = comp.slots().stream().flatMap(s -> s.slots().stream()).map(EnchantmentRegister::getNmsSlotGroup).toList();

            Object definition = Reflection.getConstructor(ENCHANT_DEF, HOLDER_SET, Optional.class, int.class, int.class, ENCHANT_COST, ENCHANT_COST, int.class, List.class).invoke(supported, primary, comp.weight(), comp.maxLevel(), minCost, maxCost, comp.anvilCost(), slots);

            Object title = Reflection.getMethod(COMPONENT, "translatable", String.class).invoke(null, comp.name());
            Object emptyData = Reflection.getField(DATA_COMPONENT_MAP, "EMPTY", DATA_COMPONENT_MAP).get(null);
            Object emptyHolderSet = Reflection.getMethod(HOLDER_SET, "direct", List.class).invoke(null, List.of());

            Object nmsEnchant = Reflection.getConstructor(ENCHANTMENT, COMPONENT, ENCHANT_DEF, HOLDER_SET, DATA_COMPONENT_MAP).invoke(title, definition, emptyHolderSet, emptyData);

            Object info = Reflection.getField(REGISTRATION_INFO, "BUILT_IN", REGISTRATION_INFO).get(null);

            Object holder = Reflection.getMethod(MAPPED_REGISTRY, "register", RESOURCE_KEY, Object.class, REGISTRATION_INFO).invoke(ENCHANT_REGISTRY, resKey, nmsEnchant, info);

            if (HOLDER_REF.isInstance(holder)) {
                Reflection.getMethod(HOLDER_REF, "bindValue", Object.class).invoke(holder, nmsEnchant);

                List<Object> tagsToBind = new ArrayList<>();
                if (comp.isTreasure()) injectTag(holder, "treasure", tagsToBind);
                if (comp.isCurse()) injectTag(holder, "curse", tagsToBind);
                if (comp.isTradeable()) injectTag(holder, "on_trade_offers", tagsToBind);
                if (comp.isDiscoverable()) injectTag(holder, "on_random_loot", tagsToBind);
                if (comp.isEnchantable()) injectTag(holder, "in_enchanting_table", tagsToBind);

                Reflection.getMethod(HOLDER_REF, "bindTags", Collection.class).invoke(holder, tagsToBind);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to register enchantment: " + key, e);
        } finally {
            freeze(ENCHANT_REGISTRY);
            freeze(ITEM_REGISTRY);
        }
    }

    private static void injectTag(Object holder, String tagName, List<Object> tagsToBind) {
        try {
            Object tagLocation = Reflection.getMethod(RESOURCE_LOCATION, "withDefaultNamespace", String.class).invoke(null, tagName);
            Object enchantRegistryKey = Reflection.getField(REGISTRIES, "ENCHANTMENT", RESOURCE_KEY).get(null);
            Object tagKey = Reflection.getMethod(TAG_KEY, "create", RESOURCE_KEY, RESOURCE_LOCATION).invoke(null, enchantRegistryKey, tagLocation);

            Object holderSet = Reflection.getMethod(MAPPED_REGISTRY, "getOrCreateTagForRegistration", TAG_KEY).invoke(ENCHANT_REGISTRY, tagKey);

            if (HOLDER_SET_NAMED.isInstance(holderSet)) {
                Reflection.FieldAccessor<List> contentsAccessor = Reflection.getField(HOLDER_SET_NAMED, List.class, 0);

                Object rawContents = contentsAccessor.get(holderSet);
                List currentList = rawContents == null ? new ArrayList<>() : new ArrayList<>((Collection) rawContents);

                if (!currentList.contains(holder)) {
                    currentList.add(holder);
                    contentsAccessor.set(holderSet, List.copyOf(currentList));
                }

                tagsToBind.add(tagKey);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject tag: " + tagName, e);
        }
    }

    private static void removeInternal(SnowKey key) {
        try {
            Object location = Reflection.getMethod(RESOURCE_LOCATION, "fromNamespaceAndPath", String.class, String.class).invoke(null, key.root(), key.path());

            Map byLocation = Reflection.getField(MAPPED_REGISTRY, "byLocation", Map.class).get(ENCHANT_REGISTRY);
            Map byKey = Reflection.getField(MAPPED_REGISTRY, "byKey", Map.class).get(ENCHANT_REGISTRY);
            Map byValue = Reflection.getField(MAPPED_REGISTRY, "byValue", Map.class).get(ENCHANT_REGISTRY);
            List byId = Reflection.getField(MAPPED_REGISTRY, "byId", List.class).get(ENCHANT_REGISTRY);
            Map toId = Reflection.getField(MAPPED_REGISTRY, "toId", Map.class).get(ENCHANT_REGISTRY);

            Object holder = byLocation.get(location);
            if (holder == null) return;

            Object nmsEnchant = null;
            if ((boolean) Reflection.getMethod(HOLDER_REF, "isBound").invoke(holder)) {
                nmsEnchant = Reflection.getMethod(HOLDER_REF, "value").invoke(holder);
            }

            Object resKey = Reflection.getMethod(HOLDER_REF, "key").invoke(holder);

            byLocation.remove(location);
            if (resKey != null) byKey.remove(resKey);
            if (nmsEnchant != null) {
                byValue.remove(nmsEnchant);
                toId.remove(nmsEnchant);
                byId.remove(holder);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void unregister(SnowKey key) {
        if (!isRegistered(key)) return;
        unfreeze(ENCHANT_REGISTRY);
        unfreeze(ITEM_REGISTRY);
        try {
            removeInternal(key);
        } finally {
            freeze(ENCHANT_REGISTRY);
            freeze(ITEM_REGISTRY);
        }
    }

    private static Object createHolderSet(Set<Material> materials) {
        try {
            Class<?> magic = Reflection.getCraftBukkitClass("util.CraftMagicNumbers");

            List holders = new ArrayList<>();
            for (Material m : materials) {
                Object item = Reflection.getMethod(magic, "getItem", Material.class).invoke(null, m);
                Object holder = Reflection.getMethod(ITEM_REGISTRY.getClass(), "wrapAsHolder", Object.class).invoke(ITEM_REGISTRY, item);
                holders.add(holder);
            }
            return Reflection.getMethod(HOLDER_SET, "direct", List.class).invoke(null, holders);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getNmsSlotGroup(EquipmentSlot slot) {
        String name = switch (slot) {
            case HAND -> "MAINHAND";
            case OFF_HAND -> "OFFHAND";
            case FEET -> "FEET";
            case LEGS -> "LEGS";
            case CHEST -> "CHEST";
            case HEAD -> "HEAD";
            case BODY -> "ARMOR";
            case SADDLE -> "SADDLE";
        };
        return Reflection.getEnumConstant(SLOT_GROUP, name);
    }

    public static boolean isRegistered(SnowKey key) {
        try {
            Object location = Reflection.getMethod(RESOURCE_LOCATION, "fromNamespaceAndPath", String.class, String.class).invoke(null, key.root(), key.path());
            Map byLocation = Reflection.getField(MAPPED_REGISTRY, "byLocation", Map.class).get(ENCHANT_REGISTRY);
            return byLocation.containsKey(location);
        } catch (Exception e) {
            return false;
        }
    }
}
