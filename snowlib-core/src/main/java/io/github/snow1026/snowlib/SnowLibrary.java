package io.github.snow1026.snowlib;

import io.github.snow1026.snowlib.commands.Sommand;
import io.github.snow1026.snowlib.lifecycle.EventRegistry;
import io.github.snow1026.snowlib.lifecycle.SnowLifecycle;
import org.bukkit.plugin.java.JavaPlugin;

public class SnowLibrary extends JavaPlugin {
    private SnowLifecycle lifecycle;

    @Override
    public void onEnable() {
        lifecycle = new SnowLifecycle(this);
        EventRegistry.init(lifecycle);
        Sommand.init(this);
    }

    @Override
    public void onDisable() {
        lifecycle.shutdown();
    }
}
