package io.github.snow1026.snowlib.events;

import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class EventGroup {
    private final List<EventBuilder<?>> builders = new ArrayList<>();
    private final List<EventHandle> handles = new ArrayList<>();

    public <T extends Event> EventBuilder<T> listen(Class<T> type, Consumer<T> handler) {
        EventBuilder<T> builder = Events.listen(type, handler);
        builders.add(builder);
        return builder;
    }

    public void register(Plugin plugin) {
        for (EventBuilder<?> builder : builders) {
            handles.add(builder.register(plugin));
        }
    }

    public void unregister() {
        handles.forEach(EventHandle::unregister);
        handles.clear();
    }
}
