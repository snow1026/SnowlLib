package io.github.snow1026.snowlib.debug;

import io.github.snow1026.snowlib.events.EventKey;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class EventTree {
    // 스레드 안전성을 위해 ConcurrentHashMap 사용
    private static final Map<EventKey, Set<String>> tree = new ConcurrentHashMap<>();

    public static void record(EventKey key, String source) {
        tree.computeIfAbsent(key, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(source);
    }

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

    public static void clear() {
        tree.clear();
    }
}
