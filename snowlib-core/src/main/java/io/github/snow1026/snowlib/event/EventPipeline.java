package io.github.snow1026.snowlib.event;

import org.bukkit.event.Event;

/** 이벤트 실행 전/후에 로직을 삽입하는 파이프라인입니다. */
public interface EventPipeline<T extends Event> {
    boolean pre(EventContext<T> ctx); // false 반환 시 이벤트 실행 중단
    void post(EventContext<T> ctx);
}
