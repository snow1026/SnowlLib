package io.github.snow1026.snowlib.events;

import org.bukkit.event.Event;

public interface EventPipeline<T extends Event> {

    boolean pre(EventContext<T> ctx);

    void post(EventContext<T> ctx);
}
