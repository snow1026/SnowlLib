package io.github.snow1026.snowlib.event;

import io.github.snow1026.snowlib.SnowHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public final class EventHandle extends SnowHandler {
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
