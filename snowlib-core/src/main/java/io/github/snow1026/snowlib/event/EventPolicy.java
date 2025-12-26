package io.github.snow1026.snowlib.event;

/** 이벤트 실행 중 발생한 예외 처리 정책을 정의합니다. */
public interface EventPolicy {
    boolean catchException(); // true이면 SnowLib 내부에서 에러를 잡고 로그를 찍음
    static EventPolicy defaultPolicy() { return () -> true; }
}
