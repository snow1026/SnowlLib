package io.github.snow1026.snowlib.task;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Tasker {
    private static Plugin plugin;

    public static void init(@NotNull Plugin plugin) {
        Tasker.plugin = plugin;
    }

    public static TaskContext sync() {
        return new TaskContext(plugin, false);
    }

    public static TaskContext async() {
        return new TaskContext(plugin, true);
    }
}