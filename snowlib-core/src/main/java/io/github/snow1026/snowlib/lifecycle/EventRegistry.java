package io.github.snow1026.snowlib.lifecycle;

import io.github.snow1026.snowlib.event.EventHandle;
import io.github.snow1026.snowlib.event.EventInterceptor;
import java.util.*;

/**
 * 등록된 이벤트 핸들과 전역 인터셉터를 관리하는 중앙 저장소입니다.
 */
public final class EventRegistry {
    private static EventLifeCycle defaultLifecycle;
    private static final List<EventInterceptor> globalInterceptors = new ArrayList<>();

    public static void init(EventLifeCycle lc) { defaultLifecycle = lc; }

    public static EventLifeCycle getLifecycle() {
        return Objects.requireNonNull(defaultLifecycle, "SnowLib Lifecycle is not initialized!");
    }

    public static void bind(EventHandle handle) { getLifecycle().register(handle); }

    /** 전역적으로 모든 이벤트에 적용될 인터셉터를 추가합니다. */
    public static void addGlobalInterceptor(EventInterceptor interceptor) {
        globalInterceptors.add(interceptor);
    }

    public static List<EventInterceptor> getGlobalInterceptors() {
        return Collections.unmodifiableList(globalInterceptors);
    }
}
