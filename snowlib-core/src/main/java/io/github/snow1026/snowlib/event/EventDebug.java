package io.github.snow1026.snowlib.event;

import org.bukkit.event.Event;

/**
 * 이벤트 시스템의 성능 측정 및 에러 보고를 담당하는 유틸리티 클래스입니다.
 */
public final class EventDebug {

    private EventDebug() {}

    /**
     * 이벤트 실행 소요 시간을 콘솔에 출력합니다.
     * @param event 실행된 이벤트
     * @param timeNs 소요 시간 (나노초 단위)
     */
    public static void log(Event event, long timeNs) {
        System.out.println("[SnowLib] Event " + event.getEventName() + " executed in " + timeNs + " ns");
    }

    /**
     * 이벤트 처리 중 발생한 예외를 포맷에 맞게 출력하고 런타임 예외로 던집니다.
     * @param t 발생한 예외
     * @param event 예외가 발생한 시점의 이벤트
     */
    public static void handleException(Throwable t, Event event) {
        System.err.println("[SnowLib] Exception in event " + event.getEventName());
        throw new RuntimeException(t);
    }
}
