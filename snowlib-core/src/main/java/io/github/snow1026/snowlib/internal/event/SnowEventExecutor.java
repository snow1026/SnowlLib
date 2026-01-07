package io.github.snow1026.snowlib.internal.event;

import io.github.snow1026.snowlib.api.event.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class SnowEventExecutor<T extends Event> implements EventExecutor {

    private final EventKey key;
    private final Consumer<T> handler;
    private final List<Predicate<T>> filters;
    private final EventHandle handle;

    private final boolean forceCancel;
    private final int executionLimit;
    private final Instant expiryTime;
    private final boolean debug;

    private final AtomicInteger callCount = new AtomicInteger();
    private volatile boolean unregistered = false;

    public SnowEventExecutor(EventKey key, Consumer<T> handler, List<Predicate<T>> filters, EventHandle handle, boolean forceCancel, int executionLimit, Duration expiry, boolean debug) {
        this.key = key;
        this.handler = handler;
        this.filters = filters;
        this.handle = handle;
        this.forceCancel = forceCancel;
        this.executionLimit = executionLimit;
        this.expiryTime = expiry != null ? Instant.now().plus(expiry) : null;
        this.debug = debug;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) {
        if (unregistered) return;

        if (expiryTime != null && Instant.now().isAfter(expiryTime)) {
            unregister();
            return;
        }

        T e = (T) event;
        EventContext<T> ctx = new EventContext<>(e, key);

        int current = callCount.incrementAndGet();

        try {
            for (Predicate<T> filter : filters) {
                if (!filter.test(e)) return;
            }

            EventTrace.enter("handler");
            handler.accept(e);
            EventTrace.exit();

            if (forceCancel && e instanceof Cancellable cancellable) {
                cancellable.setCancelled(true);
            }

            if (debug) {
                EventDebug.log(e, ctx.executionTimeNs());
            }

        } catch (Throwable t) {
            EventDebug.handleException(t, e);
        } finally {
            if (executionLimit > 0 && current >= executionLimit) {
                unregister();
            }
        }
    }

    private void unregister() {
        if (unregistered) return;
        unregistered = true;
        handle.unregister();
    }
}
