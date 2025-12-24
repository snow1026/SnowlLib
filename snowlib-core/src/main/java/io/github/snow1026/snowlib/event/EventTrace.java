package io.github.snow1026.snowlib.event;

import java.util.ArrayDeque;
import java.util.Deque;

public final class EventTrace {

    private static final ThreadLocal<Deque<String>> TRACE = ThreadLocal.withInitial(ArrayDeque::new);

    public static void enter(String stage) {
        TRACE.get().push(stage);
    }

    public static void exit() {
        TRACE.get().pop();
    }

    public static String dump() {
        return String.join(" -> ", TRACE.get());
    }
}
