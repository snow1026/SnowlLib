package io.github.snow1026.snowlib.annotations.event;

import org.bukkit.event.EventPriority;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscribeEvent {
    EventPriority priority() default EventPriority.NORMAL;
    boolean ignoreCancelled() default false;
    boolean once() default false;
    boolean debug() default false;
    boolean forceCancel() default false;
}
