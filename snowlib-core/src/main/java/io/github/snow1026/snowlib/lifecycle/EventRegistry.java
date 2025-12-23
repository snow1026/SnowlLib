// io.github.snow1026.snowlib.lifecycle.EventRegistry
package io.github.snow1026.snowlib.lifecycle;

import io.github.snow1026.snowlib.events.EventHandle;
import io.github.snow1026.snowlib.events.EventInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class EventRegistry {
    private static SnowLifecycle defaultLifecycle;
    private static final List<EventInterceptor> globalInterceptors = new ArrayList<>();

    public static void init(SnowLifecycle lc) {
        defaultLifecycle = lc;
    }

    public static SnowLifecycle getLifecycle() {
        return Objects.requireNonNull(defaultLifecycle, "SnowLib Lifecycle is not initialized!");
    }

    public static void bind(EventHandle handle) {
        getLifecycle().register(handle);
    }

    public static void addGlobalInterceptor(EventInterceptor interceptor) {
        globalInterceptors.add(interceptor);
    }

    public static List<EventInterceptor> getGlobalInterceptors() {
        return Collections.unmodifiableList(globalInterceptors);
    }
}
