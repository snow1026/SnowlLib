package io.github.snow1026.snowlib.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public final class EventHandle {

    private final Listener listener;
    private boolean active = true;

    public EventHandle(Listener listener) {
        this.listener = listener;
    }

    public void unregister() {
        if (!active) return;
        HandlerList.unregisterAll(listener);
        active = false;
    }

    public boolean isActive() {
        return active;
    }
}
