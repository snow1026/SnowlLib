package io.github.snow1026.snowlib.event;

import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 여러 이벤트를 한꺼번에 등록하거나 해제할 때 사용하는 그룹 클래스입니다.
 */
public final class EventGroup {
    private final List<EventBuilder<?>> builders = new ArrayList<>();
    private final List<EventHandle> handles = new ArrayList<>();

    /** 그룹 내에 새로운 리스너를 정의합니다. */
    public <T extends Event> EventBuilder<T> listen(Class<T> type, Consumer<T> handler) {
        EventBuilder<T> builder = Events.listen(type, handler);
        builders.add(builder);
        return builder;
    }

    /** 정의된 모든 이벤트를 실제 서버에 등록합니다. */
    public void register(Plugin plugin) {
        for (EventBuilder<?> builder : builders) {
            handles.add(builder.register(plugin));
        }
    }

    /** 그룹에 등록된 모든 이벤트를 서버에서 해제합니다. */
    public void unregister() {
        handles.forEach(EventHandle::unregister);
        handles.clear();
    }
}
