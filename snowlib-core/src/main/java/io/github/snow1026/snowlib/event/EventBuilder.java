package io.github.snow1026.snowlib.event;

import io.github.snow1026.snowlib.internal.event.EventExecutorImpl;
import io.github.snow1026.snowlib.internal.event.LambdaListener;
import io.github.snow1026.snowlib.lifecycle.EventRegistry;
import io.github.snow1026.snowlib.util.reflect.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 이벤트를 정의하고 상세 옵션을 설정하는 빌더 클래스입니다.
 */
public final class EventBuilder<T extends Event> {
    private final Class<T> type;
    private final Consumer<T> handler;
    private final EventKey key;

    private EventPriority priority = EventPriority.NORMAL;
    private boolean ignoreCancelled = false;
    private boolean forceCancel = false;
    private int executionLimit = -1;
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

    /** 이벤트 우선순위를 설정합니다. */
    public EventBuilder<T> priority(EventPriority priority) { this.priority = priority; return this; }
    /** 취소된 이벤트는 무시할지 설정합니다. */
    public EventBuilder<T> ignoreCancelled(boolean value) { this.ignoreCancelled = value; return this; }
    /** 핸들러 실행 후 이벤트를 강제로 취소합니다. */
    public EventBuilder<T> cancel(boolean value) { this.forceCancel = value; return this; }
    /** 이벤트가 단 한 번만 실행되도록 설정합니다. */
    public EventBuilder<T> once() { return once(true); }
    public EventBuilder<T> once(boolean value) { this.executionLimit = value ? 1 : -1; return this; }
    /** 최대 실행 횟수를 제한합니다. */
    public EventBuilder<T> limit(int count) { this.executionLimit = count; return this; }
    /** 일정 시간 후에 리스너가 자동 해제되도록 설정합니다. */
    public EventBuilder<T> expireAfter(Duration duration) { this.expiry = duration; return this; }
    /** 실행 시간을 로그로 출력하는 디버그 모드를 활성화합니다. */
    public EventBuilder<T> debug() { this.debug = true; return this; }
    public EventBuilder<T> debug(boolean value) { this.debug = value; return this; }
    /** 특정 조건을 만족할 때만 핸들러가 실행되도록 필터를 추가합니다. */
    public EventBuilder<T> filter(Predicate<T> filter) { this.filters.add(filter); return this; }
    /** 이벤트 실행 과정을 모니터링할 인터셉터를 추가합니다. */
    public EventBuilder<T> intercept(EventInterceptor interceptor) { this.interceptors.add(interceptor); return this; }
    /** 이벤트 실행 전/후 로직을 처리할 파이프라인을 추가합니다. */
    public EventBuilder<T> pipeline(EventPipeline<T> pipeline) { this.pipelines.add(pipeline); return this; }
    /** 기본 플러그인을 사용하여 리스너를 등록합니다. */
    public EventHandle register() { return register(EventRegistry.getLifecycle().plugin()); }

    /** 특정 플러그인을 지정하여 리스너를 등록합니다. */
    @SuppressWarnings("unchecked")
    public EventHandle register(Plugin plugin) {
        LambdaListener listener = new LambdaListener();

        EventExecutorImpl<T> executor = (EventExecutorImpl<T>) Reflection.getConstructor(EventExecutorImpl.class, EventKey.class, Consumer.class, List.class, List.class, List.class, EventPolicy.class, boolean.class, int.class, Duration.class, boolean.class).invoke(key, handler, List.copyOf(filters), List.copyOf(interceptors), List.copyOf(pipelines), policy, forceCancel, executionLimit, expiry, debug);

        Bukkit.getPluginManager().registerEvent(type, listener, priority, executor, plugin, ignoreCancelled);

        EventHandle handle = new EventHandle(listener);
        EventRegistry.bind(handle);
        return handle;
    }
}
