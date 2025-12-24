package io.github.snow1026.snowlib.event;

public interface EventInterceptor {

    void before(EventContext<?> ctx);

    void after(EventContext<?> ctx);

    void onError(EventContext<?> ctx, Throwable throwable);
}
