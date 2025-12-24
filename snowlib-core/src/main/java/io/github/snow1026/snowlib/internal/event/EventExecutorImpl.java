package io.github.snow1026.snowlib.internal.event;

import io.github.snow1026.snowlib.event.*;
import io.github.snow1026.snowlib.lifecycle.EventRegistry;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class EventExecutorImpl<T extends Event> implements EventExecutor {
    private final EventKey key;
    private final Consumer<T> handler;
    private final List<Predicate<T>> filters;
    private final List<EventInterceptor> interceptors;
    private final List<EventPipeline<T>> pipelines;
    private final EventPolicy policy;

    private final boolean forceCancel;
    private final int executionLimit;
    private final Instant expiryTime;
    private final boolean debug;

    private final AtomicInteger callCount = new AtomicInteger(0);
    private volatile boolean unregistered = false;

    private EventExecutorImpl(EventKey key, Consumer<T> handler, List<Predicate<T>> filters, List<EventInterceptor> interceptors, List<EventPipeline<T>> pipelines, EventPolicy policy, boolean forceCancel, int executionLimit, Duration expiry, boolean debug) {
        this.key = key;
        this.handler = handler;
        this.filters = filters;
        this.interceptors = interceptors;
        this.pipelines = pipelines;
        this.policy = policy;
        this.forceCancel = forceCancel;
        this.executionLimit = executionLimit;
        this.expiryTime = (expiry != null) ? Instant.now().plus(expiry) : null;
        this.debug = debug;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) {
        if (unregistered) return;

        if (expiryTime != null && Instant.now().isAfter(expiryTime)) {
            unregister(listener);
            return;
        }

        T e = (T) event;
        EventContext<T> ctx = new EventContext<>(e, key);

        try {
            for (EventInterceptor i : EventRegistry.getGlobalInterceptors()) i.before(ctx);
            if (!interceptors.isEmpty()) {
                for (EventInterceptor i : interceptors) i.before(ctx);
            }

            if (!pipelines.isEmpty()) {
                for (EventPipeline<T> pipeline : pipelines) {
                    if (!pipeline.pre(ctx)) return;
                }
            }

            if (!filters.isEmpty()) {
                for (Predicate<T> filter : filters) {
                    if (!filter.test(e)) return;
                }
            }

            EventTrace.enter("handler");
            handler.accept(e);
            EventTrace.exit();

            if (!pipelines.isEmpty()) {
                for (EventPipeline<T> pipeline : pipelines) {
                    pipeline.post(ctx);
                }
            }

            if (!interceptors.isEmpty()) {
                for (EventInterceptor i : interceptors) i.after(ctx);
            }
            for (EventInterceptor i : EventRegistry.getGlobalInterceptors()) i.after(ctx);

            if (forceCancel && e instanceof Cancellable cancellable) {
                cancellable.setCancelled(true);
            }

            if (debug) {
                EventDebug.log(e, ctx.executionTimeNs());
            }

            if (executionLimit > 0 && callCount.incrementAndGet() >= executionLimit) {
                unregister(listener);
            }

        } catch (Throwable t) {
            EventRegistry.getGlobalInterceptors().forEach(i -> i.onError(ctx, t));
            interceptors.forEach(i -> i.onError(ctx, t));

            if (policy.catchException()) {
                EventDebug.handleException(t, e);
            } else {
                throw new RuntimeException("Exception in SnowLib Event Handler", t);
            }
        }
    }

    private void unregister(Listener listener) {
        unregistered = true;
        HandlerList.unregisterAll(listener);
    }
}
