package io.github.snow1026.snowlib.api.event;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * 등록된 이벤트 리스너에 대한 제어권을 가진 핸들 객체입니다.
 * 이 객체를 통해 이벤트를 개별적으로 해제하거나 활성 상태를 확인할 수 있습니다.
 */
public final class EventHandle {
    private final Listener listener;
    private boolean active = true;

    /**
     * @param listener Bukkit 리스너 객체
     */
    public EventHandle(Listener listener) {
        this.listener = listener;
    }

    /**
     * 이 이벤트를 서버 리스너 목록에서 제거합니다.
     * 이미 해제된 경우 중복 실행되지 않습니다.
     */
    public void unregister() {
        if (!active) return;
        HandlerList.unregisterAll(listener);
        active = false;
    }

    /** @return 현재 리스너가 활성화되어 서버에서 작동 중인지 여부 */
    public boolean isActive() {
        return active;
    }
}
