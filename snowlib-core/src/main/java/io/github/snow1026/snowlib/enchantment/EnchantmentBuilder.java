package io.github.snow1026.snowlib.enchantment;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.component.enchantment.*;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EnchantmentBuilder {
    private final SnowKey key;
    private String name;
    private Set<Material> supportedItems = Set.of();
    private EnchantmentItemSet primaryItems = null;
    private int weight = 10;
    private int maxLevel = 1;
    private EnchantmentCost minCost = new EnchantmentCost(1, 1);
    private EnchantmentCost maxCost = new EnchantmentCost(20, 1);
    private int anvilCost = 4;
    private final List<EnchantmentSlot> slots = new ArrayList<>();
    private boolean isTreasure = false;
    private boolean isCurse = false;
    private boolean isTradeable = true;
    private boolean isDiscoverable = true;
    private boolean isEnchantable = true;

    public EnchantmentBuilder(SnowKey key) {
        this.key = key;
        this.name = key.path();
    }

    public EnchantmentBuilder display(String name) {
        this.name = name;
        return this;
    }

    public EnchantmentBuilder supportedItems(Material... materials) {
        this.supportedItems = Set.of(materials);
        return this;
    }

    public EnchantmentBuilder primaryItems(Material... materials) {
        this.primaryItems = EnchantmentItemSet.of(materials);
        return this;
    }

    public EnchantmentBuilder weight(int weight) {
        this.weight = weight;
        return this;
    }

    public EnchantmentBuilder maxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    public EnchantmentBuilder cost(int minBase, int minPerLevel, int maxBase, int maxPerLevel) {
        this.minCost = new EnchantmentCost(minBase, minPerLevel);
        this.maxCost = new EnchantmentCost(maxBase, maxPerLevel);
        return this;
    }

    public EnchantmentBuilder anvilCost(int anvilCost) {
        this.anvilCost = anvilCost;
        return this;
    }

    public EnchantmentBuilder slots(EquipmentSlot... slots) {
        this.slots.add(EnchantmentSlot.of(slots));
        return this;
    }

    public EnchantmentBuilder treasure(boolean val) {
        this.isTreasure = val;
        return this;
    }

    public EnchantmentBuilder curse(boolean val) {
        this.isCurse = val;
        return this;
    }

    public EnchantmentBuilder tradeable(boolean val) {
        this.isTradeable = val;
        return this;
    }

    public EnchantmentBuilder discoverable(boolean val) {
        this.isDiscoverable = val;
        return this;
    }

    public EnchantmentBuilder enchantable(boolean val) {
        this.isEnchantable = val;
        return this;
    }

    public SnowEnchantment build() {
        EnchantmentComponent component = new EnchantmentComponent(name, new EnchantmentItemSet(supportedItems), Optional.ofNullable(primaryItems), weight, maxLevel, minCost, maxCost, anvilCost, slots.isEmpty() ? List.of(EnchantmentSlot.of(EquipmentSlot.values())) : slots, isTreasure, isCurse, isTradeable, isDiscoverable, isEnchantable);
        return new SnowEnchantment(key, component) {};
    }
}
