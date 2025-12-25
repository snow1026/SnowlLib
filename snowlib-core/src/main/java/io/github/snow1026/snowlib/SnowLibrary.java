package io.github.snow1026.snowlib;

import io.github.snow1026.snowlib.command.Sommand;
import io.github.snow1026.snowlib.enchantment.SnowEnchantment;
import io.github.snow1026.snowlib.gui.GUIListener;
import io.github.snow1026.snowlib.lifecycle.EventRegistry;
import io.github.snow1026.snowlib.lifecycle.EventLifeCycle;
import io.github.snow1026.snowlib.registry.EnchantmentRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

public class SnowLibrary extends JavaPlugin {
    private EventLifeCycle lifecycle;

    @Override
    public void onEnable() {
        lifecycle = new EventLifeCycle(this);
        EventRegistry.init(lifecycle);
        Sommand.init(this);
        GUIListener.setup();
        EnchantmentRegistry.register(SnowEnchantment.builder(new SnowKey(this, "light"))
                .display("번개")
                .maxLevel(1)
                .supportedItems(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
                        Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD)
                .primaryItems(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
                        Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD)
                .weight(5)
                .slots(EquipmentSlot.HAND)
                .anvilCost(10)
                .cost(3, 1, 20, 30)
                .curse(false)
                .treasure(true)
                .enchantable(true)
                .tradeable(true)
                .discoverable(true)
                .build());
    }

    @Override
    public void onDisable() {
        lifecycle.shutdown();
    }
}
