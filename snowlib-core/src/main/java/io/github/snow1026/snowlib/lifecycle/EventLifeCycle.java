// io.github.snow1026.snowlib.lifecycle.SnowLifecycle
package io.github.snow1026.snowlib.lifecycle;

import io.github.snow1026.snowlib.SnowLifeCycle;
import io.github.snow1026.snowlib.SnowHandler;
import io.github.snow1026.snowlib.event.EventHandle;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public final class EventLifeCycle extends SnowLifeCycle {
    private final List<EventHandle> handles = new ArrayList<>();

    public EventLifeCycle(Plugin plugin) {
        super(plugin);
    }

    public void register(SnowHandler handle) {
        if (handle instanceof EventHandle) {
            handles.add((EventHandle) handle);
        }
    }

    public void shutdown() {
        handles.forEach(EventHandle::unregister);
        handles.clear();
    }
}
