package io.github.snow1026.snowlib;

import org.bukkit.plugin.Plugin;

public abstract class SnowLifeCycle {
    private final Plugin plugin;

    public SnowLifeCycle(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin plugin() {
        return plugin;
    }

    public abstract void register(SnowHandler handler);

    public abstract void shutdown();
}
