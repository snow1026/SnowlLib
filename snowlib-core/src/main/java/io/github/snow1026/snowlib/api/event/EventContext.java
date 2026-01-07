package io.github.snow1026.snowlib.api.event;

import org.bukkit.event.Event;

/**
 * 이벤트 실행 시의 상태와 메타데이터를 담는 컨텍스트입니다.
 */
public final class EventContext<T extends Event> {
    private final T event;
    private final EventKey key;
    private final long startNs = System.nanoTime();

    public EventContext(T event, EventKey key) {
        this.event = event;
        this.key = key;
    }

    /** @return 실행 중인 원본 Bukkit 이벤트 */
    public T event() {
        return event;
    }

    /** @return 이벤트의 고유 키 */
    public EventKey key() {
        return key;
    }

    /** @return 이벤트 시작부터 현재까지의 소요 시간(나노초) */
    public long executionTimeNs() {
        return System.nanoTime() - startNs;
    }
}
