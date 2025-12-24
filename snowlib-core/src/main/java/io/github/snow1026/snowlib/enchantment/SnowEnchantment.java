package io.github.snow1026.snowlib.enchantment;

import io.github.snow1026.snowlib.Snow;
import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.component.enchantment.EnchantmentComponent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.enchantments.Enchantment;

public abstract class SnowEnchantment extends Snow {
    private final SnowKey key;
    private final EnchantmentComponent component;

    protected SnowEnchantment(SnowKey key, EnchantmentComponent component) {
        this.key = key;
        this.component = component;
    }

    public static EnchantmentBuilder builder(SnowKey key) {
        return new EnchantmentBuilder(key);
    }

    public SnowKey key() {
        return key;
    }

    public EnchantmentComponent component() {
        return component;
    }

    public Enchantment bukkit() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(key.bukkit());
    }
}
