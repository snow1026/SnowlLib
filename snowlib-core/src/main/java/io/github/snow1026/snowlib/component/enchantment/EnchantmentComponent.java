package io.github.snow1026.snowlib.component.enchantment;

import java.util.List;
import java.util.Optional;

public record EnchantmentComponent(EnchantmentItemSet supportedItems, Optional<EnchantmentItemSet> primaryItems, int weight, int maxLevel, EnchantmentCost minCost, EnchantmentCost maxCost, int anvilCost, List<EnchantmentSlot> slots) {}
