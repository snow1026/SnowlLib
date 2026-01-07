package io.github.snow1026.snowlib;

import io.github.snow1026.snowlib.api.gui.GUIListener;
import io.github.snow1026.snowlib.api.lifecycle.EventLifeCycle;
import io.github.snow1026.snowlib.api.lifecycle.EventRegistry;
import io.github.snow1026.snowlib.internal.task.SnowTasker;
import io.github.snow1026.snowlib.registry.RegistryAccess;
import io.github.snow1026.snowlib.registry.RegistryKey;
import io.github.snow1026.snowlib.registry.internal.*;
import io.github.snow1026.snowlib.utils.reflect.Reflection;
import org.bukkit.plugin.java.JavaPlugin;

public final class SnowLibrary extends JavaPlugin {
    private static EventLifeCycle eventLifecycle;
    private static SnowLibrary snowlibrary;

    @Override
    public void onEnable() {
        snowlibrary = this;
        eventLifecycle = new EventLifeCycle(snowlibrary());
        SnowTasker.init(snowlibrary());
        GUIListener.setup();
        Reflection.clearCache();
        EventRegistry.init(eventLifecycle());

        SnowRegistryAccess.registerRegistry(RegistryKey.COMMAND, new CommandRegistry());
        SnowRegistryAccess.registerRegistry(RegistryKey.ATTRIBUTE, new AttributeRegistry());
        SnowRegistryAccess.registerRegistry(RegistryKey.ENCHANTMENT, new EnchantmentRegistry());
        SnowRegistryAccess.registerRegistry(RegistryKey.PACKET, new PacketRegistry());
    }

    @Override
    public void onDisable() {
        eventLifecycle().shutdown();
    }

    public static SnowLibrary snowlibrary() {
        return snowlibrary;
    }
    public static EventLifeCycle eventLifecycle() {
        return eventLifecycle;
    }
    public static RegistryAccess registryAccess() {
        return new SnowRegistryAccess();
    }
}
