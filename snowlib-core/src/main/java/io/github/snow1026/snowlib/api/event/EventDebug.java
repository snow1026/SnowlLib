package io.github.snow1026.snowlib.api.event;

import org.bukkit.event.Event;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    /** 이벤트 키와 해당 이벤트를 등록한 소스들의 집합을 매핑하는 스토리지입니다. */
    private static final Map<EventKey, Set<String>> tree = new ConcurrentHashMap<>();

    /**
     * 특정 이벤트의 등록 소스를 기록합니다.
     * * @param key    등록된 이벤트의 고유 키
     * @param source 등록을 요청한 주체 (예: 클래스명, 메서드명 등)
     */
    public static void record(EventKey key, String source) {
        tree.computeIfAbsent(key, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(source);
    }

    /**
     * 현재까지 등록된 모든 이벤트와 그 출처를 트리 형태로 콘솔에 출력(Dump)합니다.
     * 등록된 이벤트가 없을 경우 별도의 안내 메시지를 표시합니다.
     */
    public static void dump() {
        System.out.println("\n========== [SnowLib Event Tree] ==========");
        if (tree.isEmpty()) {
            System.out.println(" No events registered.");
        }
        tree.forEach((k, sources) -> {
            System.out.println("Event: " + k.type().getSimpleName());
            sources.forEach(s -> System.out.println(" └─ Registered by: " + s));
        });
        System.out.println("==========================================\n");
    }

    /**
     * 저장된 모든 이벤트 트리 데이터를 초기화합니다.
     */
    public static void clear() {
        tree.clear();
    }
}
