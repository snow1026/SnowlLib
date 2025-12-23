// io.github.snow1026.snowlib.lifecycle.SnowLifecycle
package io.github.snow1026.snowlib.lifecycle;

import io.github.snow1026.snowlib.events.EventHandle;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public final class SnowLifecycle {

    private final Plugin plugin;
    private final List<EventHandle> handles = new ArrayList<>();

    public SnowLifecycle(Plugin plugin) {
        this.plugin = plugin;
    }

    public void register(EventHandle handle) {
        handles.add(handle);
    }

    public void shutdown() {
        handles.forEach(EventHandle::unregister);
        handles.clear();
    }

    public Plugin plugin() {
        return plugin;
    }
}
