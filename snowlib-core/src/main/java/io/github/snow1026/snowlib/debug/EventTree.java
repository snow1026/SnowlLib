package io.github.snow1026.snowlib.debug;

import io.github.snow1026.snowlib.event.EventKey;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 어떤 이벤트가 어떤 소스(클래스 또는 모듈)에 의해 등록되었는지 추적하는 디버깅 유틸리티입니다.
 * 등록된 이벤트들의 관계를 트리 구조로 메모리에 보관하고 콘솔에 출력할 수 있습니다.
 */
public final class EventTree {
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
