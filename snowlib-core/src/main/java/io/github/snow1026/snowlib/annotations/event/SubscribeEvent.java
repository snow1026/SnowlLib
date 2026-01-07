package io.github.snow1026.snowlib.annotations.event;

import org.bukkit.event.EventPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드를 이벤트 리스너로 등록하기 위한 어노테이션입니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscribeEvent {
    EventPriority priority() default EventPriority.NORMAL;
    boolean ignoreCancelled() default false;
    boolean once() default false;
    boolean debug() default false;
    boolean forceCancel() default false;
}
