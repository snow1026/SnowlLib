package io.github.snow1026.snowlib.internal.event;

import io.github.snow1026.snowlib.api.event.Subscription;
import io.github.snow1026.snowlib.api.event.debug.EventDebug;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SnowEventExecutor<T extends Event> implements EventExecutor, Subscription {
    private final Listener listener;
    private final Consumer<T> handler;
    private final List<Predicate<T>> filters;

    private final boolean debug;

    private final boolean forceCancel;
    private final int executionLimit;
    private final Instant expiryTime;
    private final long cooldownMs;
    private final BiConsumer<T, Throwable> exceptionHandler;

    private final AtomicInteger callCount = new AtomicInteger(0);
    private final AtomicLong lastExecutionTime = new AtomicLong(0);
    private volatile boolean active = true;

    public SnowEventExecutor(Listener listener, Consumer<T> handler, List<Predicate<T>> filters, boolean forceCancel, int executionLimit, Duration expiry, Duration cooldown, BiConsumer<T, Throwable> exceptionHandler, boolean debug) {
        this.listener = listener;
        this.handler = handler;
        this.filters = filters;
        this.debug = debug;
        this.forceCancel = forceCancel;
        this.executionLimit = executionLimit;
        this.expiryTime = expiry != null ? Instant.now().plus(expiry) : null;
        this.cooldownMs = cooldown != null ? cooldown.toMillis() : 0;
        this.exceptionHandler = exceptionHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) {
        if (!active) return;

        // Expiry Check
        if (expiryTime != null && Instant.now().isAfter(expiryTime)) {
            unregister();
            return;
        }

        long start = debug ? System.nanoTime() : 0;

        T e = (T) event;

        try {
            // Filter Check
            for (Predicate<T> filter : filters) {
                if (!filter.test(e)) return;
            }

            // Cooldown Check (Global for this listener)
            long now = System.currentTimeMillis();
            if (cooldownMs > 0) {
                long last = lastExecutionTime.get();
                if (now - last < cooldownMs) return;
                lastExecutionTime.set(now);
            }

            // Execute Handler
            handler.accept(e);
            int currentCount = callCount.incrementAndGet();

            // Force Cancel
            if (forceCancel && e instanceof Cancellable c) {
                c.setCancelled(true);
            }

            // Limit Check
            if (executionLimit > 0 && currentCount >= executionLimit) {
                unregister();
            }

            if (debug) {
                long duration = System.nanoTime() - start;
                EventDebug.log(event, duration);
            }

        } catch (Throwable t) {
            if (exceptionHandler != null) {
                exceptionHandler.accept(e, t);
            } else {
                EventDebug.handleException(t, event);
            }
        }
    }

    @Override
    public void unregister() {
        if (!active) return;
        active = false;
        HandlerList.unregisterAll(listener);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public int getCallCount() {
        return callCount.get();
    }
}
