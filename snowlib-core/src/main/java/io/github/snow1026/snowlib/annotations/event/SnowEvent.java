package io.github.snow1026.snowlib.annotations.event;

import io.github.snow1026.snowlib.registry.normal.EventRegistry;
import org.bukkit.event.EventPriority;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SnowLib의 자동 이벤트 스캔을 위한 어노테이션입니다.
 * <p>
 * {@link EventRegistry}를 통해 이 어노테이션이 붙은 메서드를 자동으로 찾아
 * Bukkit 이벤트 리스너로 등록합니다.
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SnowEvent {
    /** 이벤트 우선순위 (기본값: NORMAL) */
    EventPriority priority() default EventPriority.NORMAL;

    /** 취소된 이벤트 무시 여부 (기본값: false) */
    boolean ignoreCancelled() default false;

    /** 실행 후 이벤트 강제 취소 여부 (기본값: false) */
    boolean cancel() default false;

    /** 디버그 모드 활성화 여부 */
    boolean debug() default false;

    /** 디버그 시 표시될 소스 이름 (비어있을 경우 클래스:메서드명 사용) */
    String debugSource() default "";

    /** 최대 실행 횟수 (-1은 무제한) */
    int limit() default -1;

    /** 만료 시간 (초 단위, 0은 만료 없음) */
    long expireSeconds() default 0;
}
