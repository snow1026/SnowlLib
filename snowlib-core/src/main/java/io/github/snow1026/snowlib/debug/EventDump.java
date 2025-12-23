package io.github.snow1026.snowlib.debug;

import org.bukkit.event.Event;

public final class EventDump {

    public static void dump(Event e, long timeNs) {
        System.out.println("[SnowLib] " + e.getEventName() + " executed in " + timeNs + "ns");
    }
}
