package io.github.snow1026.snowlib.api.component.enchantment;

import org.bukkit.Material;

import java.util.Set;

public record EnchantmentItemSet(Set<Material> materials) {

    public static EnchantmentItemSet of(Material... materials) {
        return new EnchantmentItemSet(Set.of(materials));
    }
}
