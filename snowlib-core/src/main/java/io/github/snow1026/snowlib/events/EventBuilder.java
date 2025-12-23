// io.github.snow1026.snowlib.events.EventBuilder
package io.github.snow1026.snowlib.events;

import io.github.snow1026.snowlib.internals.event.EventExecutorImpl;
import io.github.snow1026.snowlib.internals.event.LambdaListener;
import io.github.snow1026.snowlib.lifecycle.EventRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class EventBuilder<T extends Event> {
    private final Class<T> type;
    private final Consumer<T> handler;
    private final EventKey key;

    private EventPriority priority = EventPriority.NORMAL;
    private boolean ignoreCancelled = false;
    private boolean forceCancel = false;
    private int executionLimit = -1; // -1은 무제한
    private Duration expiry = null;
    private boolean debug = false;

    private final EventPolicy policy = EventPolicy.defaultPolicy();

    private final List<Predicate<T>> filters = new ArrayList<>();
    private final List<EventInterceptor> interceptors = new ArrayList<>();
    private final List<EventPipeline<T>> pipelines = new ArrayList<>();

    public EventBuilder(Class<T> type, Consumer<T> handler) {
        this.type = type;
        this.handler = handler;
        this.key = new EventKey(type);
    }

    public EventBuilder<T> priority(EventPriority priority) {
        this.priority = priority;
        return this;
    }

    public EventBuilder<T> ignoreCancelled(boolean value) {
        this.ignoreCancelled = value;
        return this;
    }

    public EventBuilder<T> cancel(boolean value) {
        this.forceCancel = value;
        return this;
    }

    public EventBuilder<T> debug(boolean value) {
        this.debug = value;
        return this;
    }

    public EventBuilder<T> once() {
        return once(true);
    }

    public EventBuilder<T> once(boolean value) {
        this.executionLimit = value ? 1 : -1;
        return this;
    }

    public EventBuilder<T> limit(int count) {
        this.executionLimit = count;
        return this;
    }

    public EventBuilder<T> expireAfter(Duration duration) {
        this.expiry = duration;
        return this;
    }

    public EventBuilder<T> debug() {
        this.debug = true;
        return this;
    }

    public EventBuilder<T> filter(Predicate<T> filter) {
        this.filters.add(filter);
        return this;
    }

    /**
     * 기본 등록 메서드 (SnowLib이 관리하는 플러그인에 등록)
     */
    public EventHandle register() {
        return register(EventRegistry.getLifecycle().plugin());
    }

    public EventHandle register(Plugin plugin) {
        LambdaListener listener = new LambdaListener();

        EventExecutorImpl<T> executor = new EventExecutorImpl<>(key, handler, filters, interceptors, pipelines, policy, forceCancel, executionLimit, expiry, debug);

        Bukkit.getPluginManager().registerEvent(type, listener, priority, executor, plugin, ignoreCancelled);

        EventHandle handle = new EventHandle(listener);
        EventRegistry.bind(handle); // Lifecycle에 자동 등록
        return handle;
    }
}
