package io.github.snow1026.snowlib.internal.event;

import io.github.snow1026.snowlib.SnowLibrary;
import io.github.snow1026.snowlib.api.event.EventKey;
import io.github.snow1026.snowlib.api.event.Events;
import io.github.snow1026.snowlib.api.event.Subscription;
import io.github.snow1026.snowlib.api.event.debug.EventDebug;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SnowEvents<T extends Event> implements Events<T> {
    private final Class<T> type;
    private final Consumer<T> handler;

    private Plugin plugin = SnowLibrary.snowlibrary();
    private EventPriority priority = EventPriority.NORMAL;
    private boolean ignoreCancelled = false;
    private boolean forceCancel = false;
    private int executionLimit = -1;
    private Duration expiry;
    private Duration cooldown;
    private BiConsumer<T, Throwable> exceptionHandler;

    private final List<Predicate<T>> filters = new ArrayList<>();

    private String debugSource;

    public SnowEvents(Class<T> type, Consumer<T> handler) {
        this.type = type;
        this.handler = handler;
    }

    @Override
    public Events<T> plugin(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

    @Override
    public Events<T> priority(EventPriority priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public Events<T> ignoreCancelled(boolean ignore) {
        this.ignoreCancelled = ignore;
        return this;
    }

    @Override
    public Events<T> cancel(boolean value) {
        this.forceCancel = value;
        return this;
    }

    @Override
    public Events<T> once() {
        return limit(1);
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
    public Events<T> cooldown(Duration duration) {
        this.cooldown = duration;
        return this;
    }

    @Override
    public Events<T> exceptionHandler(BiConsumer<T, Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    @Override
    public Events<T> filter(Predicate<T> filter) {
        this.filters.add(filter);
        return this;
    }

    @Override
    public Events<T> debug() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length > 2) {
            StackTraceElement caller = stack[2];
            this.debugSource = caller.getClassName() + ":" + caller.getLineNumber();
        }
        return this;
    }

    @Override
    public Events<T> debug(String source) {
        this.debugSource = source;
        return this;
    }

    @Override
    public Subscription register() {
        if (plugin == null) {
            throw new IllegalStateException("Plugin is not set! Use .plugin(yourPluginInstance) before registering.");
        }

        if (debugSource != null) {
            EventDebug.record(new EventKey(type), debugSource);
        }

        SimpleEventListener listener = new SimpleEventListener();
        SnowEventExecutor<T> executor = new SnowEventExecutor<>(listener, handler, List.copyOf(filters), forceCancel, executionLimit, expiry, cooldown, exceptionHandler, debugSource != null);

        Bukkit.getPluginManager().registerEvent(type, listener, priority, executor, plugin, ignoreCancelled);

        return executor; // Executor implements Subscription
    }
}
