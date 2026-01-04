package io.github.snow1026.snowlib;

import io.github.snow1026.snowlib.gui.GUIListener;
import io.github.snow1026.snowlib.lifecycle.EventRegistry;
import io.github.snow1026.snowlib.lifecycle.EventLifeCycle;
import io.github.snow1026.snowlib.task.Tasker;
import io.github.snow1026.snowlib.util.reflect.Reflection;
import org.bukkit.plugin.java.JavaPlugin;

public final class SnowLibrary extends JavaPlugin {
    private static EventLifeCycle eventLifecycle;
    private static SnowLibrary snowlibrary;

    @Override
    public void onEnable() {
        snowlibrary = this;
        eventLifecycle = new EventLifeCycle(snowlibrary());
        Tasker.init(snowlibrary());
        GUIListener.setup();
        Reflection.clearCache();
        EventRegistry.init(eventLifecycle());
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
}
