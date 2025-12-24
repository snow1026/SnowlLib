package io.github.snow1026.snowlib.event;

import org.bukkit.event.Event;

public final class EventDebug {

    private EventDebug() {}

    public static void log(Event event, long timeNs) {
        System.out.println("[SnowLib] Event " + event.getEventName() + " executed in " + timeNs + " ns");
    }

    public static void handleException(Throwable t, Event event) {
        System.err.println("[SnowLib] Exception in event " + event.getEventName());
        throw new RuntimeException(t);
    }
}
