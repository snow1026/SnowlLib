package io.github.snow1026.snowlib;

import io.github.snow1026.snowlib.api.attribute.SnowAttribute;
import io.github.snow1026.snowlib.api.command.Sommand;
import io.github.snow1026.snowlib.api.enchantment.SnowEnchantment;
import io.github.snow1026.snowlib.api.gui.GUIListener;
import io.github.snow1026.snowlib.internal.registry.SnowRegistryAccess;
import io.github.snow1026.snowlib.internal.task.SnowTasker;
import io.github.snow1026.snowlib.registry.SnowRegistry;
import io.github.snow1026.snowlib.registry.RegistryAccess;
import io.github.snow1026.snowlib.registry.RegistryKey;
import io.github.snow1026.snowlib.registry.internal.*;
import io.github.snow1026.snowlib.utils.reflect.Reflection;
import org.bukkit.plugin.java.JavaPlugin;

public final class SnowLibrary extends JavaPlugin {
    private static SnowLibrary snowlibrary;

    @SuppressWarnings("unchecked")
    @Override
    public void onEnable() {
        snowlibrary = this;
        SnowTasker.init(snowlibrary());
        Reflection.clearCache();

        getServer().getPluginManager().registerEvents(new GUIListener(), snowlibrary());

        SnowRegistryAccess.registerRegistry(RegistryKey.COMMAND, (SnowRegistry<Sommand>) Reflection.newInstance(CommandRegistry.class));
        SnowRegistryAccess.registerRegistry(RegistryKey.ATTRIBUTE, (SnowRegistry<SnowAttribute>) Reflection.newInstance(AttributeRegistry.class));
        SnowRegistryAccess.registerRegistry(RegistryKey.ENCHANTMENT, (SnowRegistry<SnowEnchantment>) Reflection.newInstance(EnchantmentRegistry.class));
    }

    public static SnowLibrary snowlibrary() {
        return snowlibrary;
    }
    public static RegistryAccess registryAccess() {
        return new SnowRegistryAccess();
    }
}
