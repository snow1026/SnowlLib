package io.github.snow1026.snowlib.events;

import org.bukkit.event.Event;

public record EventKey(Class<? extends Event> type) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventKey(Class<? extends Event> type1))) return false;
        return type.equals(type1);
    }
}
