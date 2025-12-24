package io.github.snow1026.snowlib.event;

import org.bukkit.event.Event;

public interface EventPipeline<T extends Event> {

    boolean pre(EventContext<T> ctx);

    void post(EventContext<T> ctx);
}
