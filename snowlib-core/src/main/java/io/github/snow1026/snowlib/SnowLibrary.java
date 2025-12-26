package io.github.snow1026.snowlib;

import io.github.snow1026.snowlib.command.Sommand;
import io.github.snow1026.snowlib.gui.GUIListener;
import io.github.snow1026.snowlib.lifecycle.EventRegistry;
import io.github.snow1026.snowlib.lifecycle.EventLifeCycle;
import io.github.snow1026.snowlib.task.Tasker;
import org.bukkit.plugin.java.JavaPlugin;

public class SnowLibrary extends JavaPlugin {
    private EventLifeCycle lifecycle;

    @Override
    public void onEnable() {
        lifecycle = new EventLifeCycle(this);
        EventRegistry.init(lifecycle);
        Sommand.init(this);
        Tasker.init(this);
        GUIListener.setup();
    }

    @Override
    public void onDisable() {
        lifecycle.shutdown();
    }
}
