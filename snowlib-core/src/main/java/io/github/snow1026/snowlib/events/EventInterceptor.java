package io.github.snow1026.snowlib.events;

public interface EventInterceptor {

    void before(EventContext<?> ctx);

    void after(EventContext<?> ctx);

    void onError(EventContext<?> ctx, Throwable throwable);
}
