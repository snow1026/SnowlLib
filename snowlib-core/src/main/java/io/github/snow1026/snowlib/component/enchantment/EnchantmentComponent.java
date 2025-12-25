package io.github.snow1026.snowlib.component.enchantment;

import java.util.List;
import java.util.Optional;

public record EnchantmentComponent(String name, EnchantmentItemSet supportedItems, Optional<EnchantmentItemSet> primaryItems, int weight, int maxLevel, EnchantmentCost minCost, EnchantmentCost maxCost, int anvilCost, List<EnchantmentSlot> slots, boolean isTreasure, boolean isCurse, boolean isTradeable, boolean isDiscoverable, boolean isEnchantable) {}
