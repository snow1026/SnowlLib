package io.github.snow1026.snowlib.api.event;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 이벤트 실행 스택을 추적하여 현재 어떤 단계에서 로직이 수행 중인지 기록합니다.
 * ThreadLocal을 사용하여 비동기 이벤트 환경에서도 스레드 안전하게 작동합니다.
 */
public final class EventTrace {

    private static final ThreadLocal<Deque<String>> TRACE = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * 새로운 실행 단계에 진입했음을 알립니다.
     * @param stage 단계 이름 (예: "database-save", "cooldown-check")
     */
    public static void enter(String stage) {
        TRACE.get().push(stage);
    }

    /**
     * 현재 실행 단계를 종료하고 이전 단계로 돌아갑니다.
     */
    public static void exit() {
        TRACE.get().pop();
    }

    /**
     * 현재까지의 실행 경로를 문자열 형태로 출력합니다.
     * @return 실행 경로 (예: "handler -> database-save")
     */
    public static String dump() {
        return String.join(" -> ", TRACE.get());
    }
}
