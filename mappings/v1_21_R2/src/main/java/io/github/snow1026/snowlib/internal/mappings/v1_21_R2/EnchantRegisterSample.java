package io.github.snow1026.snowlib.internal.mappings.v1_21_R2;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.component.enchantment.EnchantmentComponent;
import io.github.snow1026.snowlib.enchantment.SnowEnchantment;
import io.github.snow1026.snowlib.util.reflect.Reflection;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class EnchantRegisterSample {

    private static final MinecraftServer SERVER;
    private static final MappedRegistry<Enchantment> ENCHANTS;
    private static final MappedRegistry<Item> ITEMS;

    static {
        SERVER = ((CraftServer) Bukkit.getServer()).getServer();
        ENCHANTS = (MappedRegistry<Enchantment>) SERVER.registryAccess().lookup(Registries.ENCHANTMENT).orElseThrow();
        ITEMS = (MappedRegistry<Item>) SERVER.registryAccess().lookup(Registries.ITEM).orElseThrow();
    }

    private static final Reflection.FieldAccessor<Boolean> REGISTRY_FROZEN = Reflection.getField(MappedRegistry.class, "l", boolean.class);
    private static final Reflection.FieldAccessor<Map> REGISTRY_INTRUSIVE = Reflection.getField(MappedRegistry.class, "m", Map.class);
    private static final Reflection.FieldAccessor<Map> REGISTRY_FROZEN_TAGS = Reflection.getField(MappedRegistry.class, "j", Map.class);
    private static final Reflection.FieldAccessor<Object> REGISTRY_ALL_TAGS = Reflection.getField(MappedRegistry.class, "k", Object.class);
    private static final Reflection.FieldAccessor<Map> TAGSET_MAP = Reflection.getField(Object.class, "a", Map.class);
    private static final Reflection.MethodInvoker TAGSET_UNBOUND = Reflection.getMethod(Reflection.getClass("net.minecraft.core.MappedRegistry$a"), "a");

    public void register(SnowEnchantment snowEnchantment) {
        unfreezeRegistry();

        SnowKey snowKey = snowEnchantment.key();
        EnchantmentComponent comp = snowEnchantment.component();

        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(snowKey.root(), snowKey.path());
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, location);

        HolderSet<Item> supportedItems = HolderSet.direct(comp.supportedItems().materials().stream().map(this::getNmsItem).map(ITEMS::wrapAsHolder).collect(Collectors.toList()));
        Optional<HolderSet<Item>> primaryItems = comp.primaryItems().map(set -> HolderSet.direct(set.materials().stream().map(this::getNmsItem).map(ITEMS::wrapAsHolder).collect(Collectors.toList())));
        List<EquipmentSlotGroup> nmsSlots = comp.slots().stream().flatMap(slot -> slot.slots().stream()).map(this::getNmsSlotGroup).toList();
        Enchantment.EnchantmentDefinition definition = new Enchantment.EnchantmentDefinition(supportedItems, primaryItems, comp.weight(), comp.maxLevel(), new Enchantment.Cost(comp.minCost().base(), comp.minCost().perLevel()), new Enchantment.Cost(comp.maxCost().base(), comp.maxCost().perLevel()), comp.anvilCost(), nmsSlots);

        Enchantment nmsEnchantment = new Enchantment(Component.translatable("enchantment." + snowKey.root() + "." + snowKey.path()), definition, HolderSet.direct(), DataComponentMap.EMPTY);

        ENCHANTS.register(key, nmsEnchantment, RegistrationInfo.BUILT_IN);

        freezeRegistry();
    }

    private Item getNmsItem(Material material) {
        return CraftMagicNumbers.getItem(material);
    }

    private EquipmentSlotGroup getNmsSlotGroup(org.bukkit.inventory.EquipmentSlot slot) {
        return switch (slot) {
            case HAND -> EquipmentSlotGroup.MAINHAND;
            case OFF_HAND -> EquipmentSlotGroup.OFFHAND;
            case FEET -> EquipmentSlotGroup.FEET;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case HEAD -> EquipmentSlotGroup.HEAD;
            case BODY -> EquipmentSlotGroup.ARMOR;
        };
    }

    public void unfreezeRegistry() {
        unfreeze(ENCHANTS);
        unfreeze(ITEMS);
    }

    public void freezeRegistry() {
        freeze(ITEMS);
        freeze(ENCHANTS);
    }

    private static void unfreeze(MappedRegistry<?> registry) {
        REGISTRY_FROZEN.set(registry, false);
        REGISTRY_INTRUSIVE.set(registry, new IdentityHashMap<>());
    }

    private static void freeze(MappedRegistry<?> registry) {
        Object originalTagSet = REGISTRY_ALL_TAGS.get(registry);

        Map<TagKey<?>, HolderSet.Named<?>> frozenTags = (Map<TagKey<?>, HolderSet.Named<?>>) REGISTRY_FROZEN_TAGS.get(registry);
        Map<TagKey<?>, HolderSet.Named<?>> originalMap = (Map<TagKey<?>, HolderSet.Named<?>>) TAGSET_MAP.get(originalTagSet);
        Map<TagKey<?>, HolderSet.Named<?>> copiedMap = new HashMap<>(originalMap);

        copiedMap.forEach(frozenTags::putIfAbsent);

        Object unbound = TAGSET_UNBOUND.invoke(registry);
        REGISTRY_ALL_TAGS.set(registry, unbound);

        registry.freeze();

        frozenTags.forEach(copiedMap::putIfAbsent);

        TAGSET_MAP.set(originalTagSet, copiedMap);
        REGISTRY_ALL_TAGS.set(registry, originalTagSet);
    }
}
