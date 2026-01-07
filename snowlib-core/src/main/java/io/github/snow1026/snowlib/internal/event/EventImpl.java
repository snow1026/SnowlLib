package io.github.snow1026.snowlib.internal.event;

import io.github.snow1026.snowlib.api.event.EventHandle;
import io.github.snow1026.snowlib.api.event.EventKey;
import io.github.snow1026.snowlib.api.event.Events;
import io.github.snow1026.snowlib.api.lifecycle.EventRegistry;
import io.github.snow1026.snowlib.utils.reflect.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class EventImpl<T extends Event> implements Events<T> {

    private final Class<T> type;
    private final Consumer<T> handler;
    private final EventKey key;

    private EventPriority priority = EventPriority.NORMAL;
    private boolean ignoreCancelled = false;
    private boolean forceCancel = false;
    private int executionLimit = -1;
    private Duration expiry;
    private boolean debug = false;

    private final List<Predicate<T>> filters = new ArrayList<>();

    public EventImpl(Class<T> type, Consumer<T> handler) {
        this.type = type;
        this.handler = handler;
        this.key = new EventKey(type);
    }

    @Override
    public Events<T> priority(EventPriority priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public Events<T> ignoreCancelled(boolean value) {
        this.ignoreCancelled = value;
        return this;
    }

    @Override
    public Events<T> cancel(boolean value) {
        this.forceCancel = value;
        return this;
    }

    @Override
    public Events<T> once() {
        return once(true);
    }

    @Override
    public Events<T> once(boolean value) {
        this.executionLimit = value ? 1 : -1;
        return this;
    }

    @Override
    public Events<T> limit(int count) {
        this.executionLimit = count;
        return this;
    }

    @Override
    public Events<T> expireAfter(Duration duration) {
        this.expiry = duration;
        return this;
    }

    @Override
    public Events<T> debug() {
        this.debug = true;
        return this;
    }

    @Override
    public Events<T> debug(boolean value) {
        this.debug = value;
        return this;
    }

    @Override
    public Events<T> filter(Predicate<T> filter) {
        this.filters.add(filter);
        return this;
    }

    @Override
    public EventHandle register() {
        return register(EventRegistry.getLifecycle().plugin());
    }

    @SuppressWarnings("unchecked")
    @Override
    public EventHandle register(Plugin plugin) {
        LambdaListener listener = new LambdaListener();
        EventHandle handle = new EventHandle(listener);

        SnowEventExecutor<T> executor = (SnowEventExecutor<T>) Reflection.getConstructor(SnowEventExecutor.class, EventKey.class, Consumer.class, List.class, EventHandle.class, boolean.class, int.class, Duration.class, boolean.class).invoke(key, handler, List.copyOf(filters), handle, forceCancel, executionLimit, expiry, debug);

        Bukkit.getPluginManager().registerEvent(type, listener, priority, executor, plugin, ignoreCancelled);
        EventRegistry.bind(handle);
        return handle;
    }
}
