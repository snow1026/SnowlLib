package io.github.snow1026.snowlib.util.reflect.enchants;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.component.enchantment.EnchantmentComponent;
import io.github.snow1026.snowlib.enchantment.SnowEnchantment;
import io.github.snow1026.snowlib.util.VersionUtil;
import io.github.snow1026.snowlib.util.reflect.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes"})
public final class EnchantmentRegister {
    private static final Class<?> LOCATION_CLASS = Reflection.getClass("net.minecraft.resources.ResourceLocation", "net.minecraft.resources.Identifier");
    private static final Class<?> REGISTRIES_CLASS = Reflection.getMinecraftClass("core.registries.Registries");
    private static final Class<?> RESOURCE_KEY_CLASS = Reflection.getMinecraftClass("resources.ResourceKey");
    private static final Class<?> ENCHANTMENT_CLASS = Reflection.getMinecraftClass("world.item.enchantment.Enchantment");
    private static final Class<?> ENCHANT_DEF_CLASS = Reflection.getMinecraftClass("world.item.enchantment.Enchantment$EnchantmentDefinition");
    private static final Class<?> ENCHANT_COST_CLASS = Reflection.getMinecraftClass("world.item.enchantment.Enchantment$Cost");
    private static final Class<?> HOLDER_SET_CLASS = Reflection.getMinecraftClass("core.HolderSet");
    private static final Class<?> SLOT_GROUP_CLASS = Reflection.getMinecraftClass("world.entity.EquipmentSlotGroup");
    private static final Class<?> COMPONENT_CLASS = Reflection.getMinecraftClass("network.chat.Component");
    private static final Class<?> DATA_COMP_MAP_CLASS = Reflection.getMinecraftClass("core.component.DataComponentMap");
    private static final Class<?> REG_INFO_CLASS = Reflection.getMinecraftClass("core.RegistrationInfo");
    private static final Class<?> MAPPED_REGISTRY_CLASS = Reflection.getMinecraftClass("core.MappedRegistry");
    private static final Reflection.FieldAccessor<Map> LOCATION_TO_ENTRY = Reflection.getField(MAPPED_REGISTRY_CLASS, "c", Map.class);
    private static final Reflection.FieldAccessor<Map> KEY_TO_ENTRY = Reflection.getField(MAPPED_REGISTRY_CLASS, "d", Map.class);
    private static final Reflection.FieldAccessor<Map> VALUE_TO_ENTRY = Reflection.getField(MAPPED_REGISTRY_CLASS, "e", Map.class);
    private static final Reflection.FieldAccessor<Map> REGISTRATION_INFOS = Reflection.getField(MAPPED_REGISTRY_CLASS, "f", Map.class);

    private static Object enchantRegistry = null;
    private static Object itemRegistry = null;

    public EnchantmentRegister() {
        Object server = Reflection.getMethod(Reflection.getCraftBukkitClass("CraftServer"), "getServer").invoke(Bukkit.getServer());
        Object access = Reflection.getMethod(server.getClass(), "bc").invoke(server);

        Object enchantKey = Reflection.getField(REGISTRIES_CLASS, "b", RESOURCE_KEY_CLASS).get(null);
        Object itemKey = Reflection.getField(REGISTRIES_CLASS, "f", RESOURCE_KEY_CLASS).get(null);

        enchantRegistry = lookup(access, enchantKey);
        itemRegistry = lookup(access, itemKey);
    }

    public static void register(SnowEnchantment snowEnchantment) {
        EnchantmentRegistryFreezer.unfreeze(enchantRegistry);
        EnchantmentRegistryFreezer.unfreeze(itemRegistry);

        try {
            SnowKey snowKey = snowEnchantment.key();
            EnchantmentComponent comp = snowEnchantment.component();

            Object location = Reflection.getMethod(LOCATION_CLASS, "a", String.class, String.class).invoke(null, snowKey.root(), snowKey.path());
            Object enchantRegKey = Reflection.getField(REGISTRIES_CLASS, "b", RESOURCE_KEY_CLASS).get(null);
            Object key = Reflection.getMethod(RESOURCE_KEY_CLASS, "a", RESOURCE_KEY_CLASS, LOCATION_CLASS).invoke(null, enchantRegKey, location);

            Object supportedItems = createHolderSet(comp.supportedItems().materials());
            Object primaryItems = comp.primaryItems().isPresent() ? Optional.of(createHolderSet(comp.primaryItems().get().materials())) : Optional.empty();

            List<Object> nmsSlots = comp.slots().stream().flatMap(s -> s.slots().stream()).map(EnchantmentRegister::getNmsSlotGroup).collect(Collectors.toList());

            Object minCost = Reflection.getConstructor(ENCHANT_COST_CLASS, int.class, int.class).invoke(comp.minCost().base(), comp.minCost().perLevel());
            Object maxCost = Reflection.getConstructor(ENCHANT_COST_CLASS, int.class, int.class).invoke(comp.maxCost().base(), comp.maxCost().perLevel());

            Object definition = Reflection.getConstructor(ENCHANT_DEF_CLASS, HOLDER_SET_CLASS, Optional.class, int.class, int.class, ENCHANT_COST_CLASS, ENCHANT_COST_CLASS, int.class, List.class).invoke(supportedItems, primaryItems, comp.weight(), comp.maxLevel(), minCost, maxCost, comp.anvilCost(), nmsSlots);
            Object title = Reflection.getMethod(COMPONENT_CLASS, "b", String.class).invoke(null, "enchantment." + snowKey.root() + "." + snowKey.path());
            Object emptyDataMap = Reflection.getField(DATA_COMP_MAP_CLASS, "a", DATA_COMP_MAP_CLASS).get(null);
            Object emptyHolderSet = Reflection.getMethod(HOLDER_SET_CLASS, "a").invoke(null); // direct()

            Object nmsEnchantment = Reflection.getConstructor(ENCHANTMENT_CLASS, COMPONENT_CLASS, ENCHANT_DEF_CLASS, HOLDER_SET_CLASS, DATA_COMP_MAP_CLASS).invoke(title, definition, emptyHolderSet, emptyDataMap);

            Object regInfo = Reflection.getField(REG_INFO_CLASS, "a", REG_INFO_CLASS).get(null); // BUILT_IN
            Reflection.getMethod(enchantRegistry.getClass(), "a", RESOURCE_KEY_CLASS, Object.class, REG_INFO_CLASS).invoke(enchantRegistry, key, nmsEnchantment, regInfo);

        } finally {
            EnchantmentRegistryFreezer.freeze(itemRegistry);
            EnchantmentRegistryFreezer.freeze(enchantRegistry);
        }
    }

    public static void unRegister(SnowKey snowKey) {
        if (!isRegistered(snowKey)) return;

        EnchantmentRegistryFreezer.unfreeze(enchantRegistry);

        try {
            Object location = createLocation(snowKey);
            Object enchantRegKey = Reflection.getField(REGISTRIES_CLASS, "b", RESOURCE_KEY_CLASS).get(null);
            Object key = Reflection.getMethod(RESOURCE_KEY_CLASS, "a", RESOURCE_KEY_CLASS, LOCATION_CLASS).invoke(null, enchantRegKey, location);

            Map locMap = LOCATION_TO_ENTRY.get(enchantRegistry);
            Map keyMap = KEY_TO_ENTRY.get(enchantRegistry);
            Map valMap = VALUE_TO_ENTRY.get(enchantRegistry);
            Map infoMap = REGISTRATION_INFOS.get(enchantRegistry);

            Object holder = locMap.get(location);
            if (holder != null) {
                Object value = Reflection.getMethod(holder.getClass(), "a").invoke(holder);

                locMap.remove(location);
                keyMap.remove(key);
                if (value != null) {
                    valMap.remove(value);
                    infoMap.remove(value);
                }
            }
        } finally {
            EnchantmentRegistryFreezer.freeze(enchantRegistry);
        }
    }

    public static boolean isRegistered(SnowKey snowKey) {
        Object location = createLocation(snowKey);
        return (boolean) Reflection.getMethod(MAPPED_REGISTRY_CLASS, "containsKey", LOCATION_CLASS).invoke(enchantRegistry, location);
    }

    private static Object createLocation(SnowKey snowKey) {
        return Reflection.getMethod(LOCATION_CLASS, "a", String.class, String.class).invoke(null, snowKey.root(), snowKey.path());
    }

    private static Object lookup(Object access, Object key) {
        Optional<?> opt = (Optional<?>) Reflection.getMethod(access.getClass(), "a", RESOURCE_KEY_CLASS).invoke(access, key);
        return opt.orElseThrow();
    }

    private static Object createHolderSet(Set<Material> materials) {
        Class<?> magicClass = Reflection.getCraftBukkitClass("util.CraftMagicNumbers");
        List<Object> holders = materials.stream().map(m -> Reflection.getMethod(magicClass, "getItem", Material.class).invoke(null, m)).map(item -> Reflection.getMethod(itemRegistry.getClass(), "a", Object.class).invoke(itemRegistry, item)).collect(Collectors.toList());
        return Reflection.getMethod(HOLDER_SET_CLASS, "a", List.class).invoke(null, holders);
    }

    private static Object getNmsSlotGroup(EquipmentSlot slot) {
        String groupName = switch (slot) {
            case HAND -> "MAINHAND";
            case OFF_HAND -> "OFFHAND";
            case FEET -> "FEET";
            case LEGS -> "LEGS";
            case CHEST -> "CHEST";
            case HEAD -> "HEAD";
            case BODY -> "ARMOR";
            case SADDLE -> "SADDLE";
        };

        if (groupName.equals("SADDLE") && !VersionUtil.isAtLeast(VersionUtil.MappingsVersion.v1_21_R4)) {
            groupName = "ANY";
        }

        return Reflection.getEnumConstant(SLOT_GROUP_CLASS, groupName);
    }
}
