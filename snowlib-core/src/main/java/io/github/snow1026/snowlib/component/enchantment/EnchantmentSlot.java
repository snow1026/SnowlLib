package io.github.snow1026.snowlib.component.enchantment;

import org.bukkit.inventory.EquipmentSlot;

import java.util.Set;

public record EnchantmentSlot(Set<EquipmentSlot> slots) {

    public static EnchantmentSlot of(EquipmentSlot... slots) {
        return new EnchantmentSlot(Set.of(slots));
    }
}
