package io.github.snow1026.snowlib.event;

import org.bukkit.event.Event;

public final class EventContext<T extends Event> {
    private final T event;
    private final EventKey key;
    private final long startNs = System.nanoTime();

    public EventContext(T event, EventKey key) {
        this.event = event;
        this.key = key;
    }

    public T event() {
        return event;
    }

    public EventKey key() {
        return key;
    }

    public long executionTimeNs() {
        return System.nanoTime() - startNs;
    }
}
