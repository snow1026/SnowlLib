package io.github.snow1026.snowlib.api.lifecycle;

import io.github.snow1026.snowlib.api.event.EventHandle;

import java.util.Objects;

/**
 * 등록된 이벤트 핸들과 전역 인터셉터를 관리하는 중앙 저장소입니다.
 */
public final class EventRegistry {
    private static EventLifeCycle defaultLifecycle;

    public static void init(EventLifeCycle lc) {
        defaultLifecycle = lc;
    }

    public static EventLifeCycle getLifecycle() {
        return Objects.requireNonNull(defaultLifecycle, "SnowLib Lifecycle is not initialized!");
    }

    public static void bind(EventHandle handle) {
        getLifecycle().register(handle);
    }
}
