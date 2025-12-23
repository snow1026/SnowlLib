package io.github.snow1026.snowlib.events;

import org.bukkit.event.Event;

import java.util.function.Consumer;

public final class Events {

    private Events() {}

    public static <T extends Event> EventBuilder<T> listen(Class<T> type, Consumer<T> handler) {
        return new EventBuilder<>(type, handler);
    }

    public static EventGroup group() {
        return new EventGroup();
    }
}
