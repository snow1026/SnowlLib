package io.github.snow1026.snowlib.event;

/** 이벤트 실행 과정을 가로채서 모니터링하거나 에러를 처리하는 인터셉터입니다. */
public interface EventInterceptor {
    void before(EventContext<?> ctx);
    void after(EventContext<?> ctx);
    void onError(EventContext<?> ctx, Throwable throwable);
}
