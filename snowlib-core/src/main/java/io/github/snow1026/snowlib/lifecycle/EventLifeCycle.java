package io.github.snow1026.snowlib.lifecycle;

import io.github.snow1026.snowlib.SnowLifeCycle;
import io.github.snow1026.snowlib.SnowHandler;
import io.github.snow1026.snowlib.event.EventHandle;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * 플러그인의 활성화/비활성화 시점에 맞추어 이벤트 리스너들의 생명 주기를 관리합니다.
 * 플러그인이 종료될 때 등록된 모든 이벤트를 자동으로 해제하여 메모리 누수를 방지합니다.
 */
public final class EventLifeCycle extends SnowLifeCycle {
    private final List<EventHandle> handles = new ArrayList<>();

    public EventLifeCycle(Plugin plugin) {
        super(plugin);
    }

    /**
     * 관리할 이벤트 핸들을 등록합니다.
     * @param handle 등록할 SnowHandler 인스턴스
     */
    @Override
    public void register(SnowHandler handle) {
        if (handle instanceof EventHandle) {
            handles.add((EventHandle) handle);
        }
    }

    /**
     * 플러그인이 종료될 때 호출되며, 등록된 모든 이벤트를 서버에서 해제(Unregister)합니다.
     */
    @Override
    public void shutdown() {
        handles.forEach(EventHandle::unregister);
        handles.clear();
    }
}
