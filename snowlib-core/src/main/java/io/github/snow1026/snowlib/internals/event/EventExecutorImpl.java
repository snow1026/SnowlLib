package io.github.snow1026.snowlib.internals.event;

import io.github.snow1026.snowlib.events.*;
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

    public EventExecutorImpl(EventKey key, Consumer<T> handler, List<Predicate<T>> filters, List<EventInterceptor> interceptors, List<EventPipeline<T>> pipelines, EventPolicy policy, boolean forceCancel, int executionLimit, Duration expiry, boolean debug) {
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

        // 1. 시간 만료 체크 (성능을 위해 최상단 배치)
        if (expiryTime != null && Instant.now().isAfter(expiryTime)) {
            unregister(listener);
            return;
        }

        T e = (T) event;
        EventContext<T> ctx = new EventContext<>(e, key);

        try {
            // 2. Global + Local Interceptor (Before)
            // EventRegistry에서 관리하는 전역 인터셉터를 먼저 실행
            for (EventInterceptor i : EventRegistry.getGlobalInterceptors()) i.before(ctx);
            if (!interceptors.isEmpty()) {
                for (EventInterceptor i : interceptors) i.before(ctx);
            }

            // 3. Pipelines (Pre)
            if (!pipelines.isEmpty()) {
                for (EventPipeline<T> pipeline : pipelines) {
                    if (!pipeline.pre(ctx)) return;
                }
            }

            // 4. Filters
            if (!filters.isEmpty()) {
                for (Predicate<T> filter : filters) {
                    if (!filter.test(e)) return;
                }
            }

            // 5. Actual Handler Execution (MethodHandle이 내부에서 작동)
            EventTrace.enter("handler");
            handler.accept(e);
            EventTrace.exit();

            // 6. Pipelines (Post)
            if (!pipelines.isEmpty()) {
                for (EventPipeline<T> pipeline : pipelines) {
                    pipeline.post(ctx);
                }
            }

            // 7. Global + Local Interceptor (After)
            if (!interceptors.isEmpty()) {
                for (EventInterceptor i : interceptors) i.after(ctx);
            }
            for (EventInterceptor i : EventRegistry.getGlobalInterceptors()) i.after(ctx);

            // 8. Force Cancel
            if (forceCancel && e instanceof Cancellable cancellable) {
                cancellable.setCancelled(true);
            }

            // 9. Debug Logging
            if (debug) {
                EventDebug.log(e, ctx.executionTimeNs());
            }

            // 10. 실행 횟수 제한 체크 (Atomic 연산으로 스레드 안전성 확보)
            if (executionLimit > 0 && callCount.incrementAndGet() >= executionLimit) {
                unregister(listener);
            }

        } catch (Throwable t) {
            // 에러 발생 시 모든 인터셉터에 알림
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
