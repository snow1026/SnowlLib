package io.github.snow1026.snowlib.event;

import org.bukkit.event.Event;
import java.util.function.Consumer;

/**
 * SnowLib 이벤트 시스템의 메인 진입점입니다.
 */
public final class Events {
    private Events() {}

    /**
     * 특정 이벤트 타입에 대한 리스너 작성을 시작합니다.
     * @param type    이벤트 클래스 (예: PlayerJoinEvent.class)
     * @param handler 이벤트를 처리할 로직
     * @return 설정을 위한 EventBuilder 인스턴스
     */
    public static <T extends Event> EventBuilder<T> listen(Class<T> type, Consumer<T> handler) {
        return new EventBuilder<>(type, handler);
    }

    /**
     * 여러 이벤트를 묶어서 관리할 수 있는 그룹을 생성합니다.
     * @return 새로운 EventGroup 인스턴스
     */
    public static EventGroup group() {
        return new EventGroup();
    }
}
