package io.github.snow1026.snowlib.internals.event;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

public final class InternalEventUtil {

    private InternalEventUtil() {}

    public static String debugName(Event event) {
        return event.getEventName() + "@" + Integer.toHexString(event.hashCode());
    }

    public static int priorityWeight(EventPriority priority) {
        return switch (priority) {
            case LOWEST -> 0;
            case LOW -> 1;
            case NORMAL -> 2;
            case HIGH -> 3;
            case HIGHEST -> 4;
            case MONITOR -> 5;
        };
    }
}
