package io.github.snow1026.snowlib.api.event;

import io.github.snow1026.snowlib.internal.event.EventImpl;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * SnowLib 이벤트 시스템의 메인 진입점입니다.
 *
 * <p>
 * 이 인터페이스는 Bukkit 이벤트 리스너를 람다 기반 DSL 형태로 선언할 수 있도록 설계되었습니다.
 * 모든 옵션은 체이닝 방식으로 설정되며, {@link #register()} 호출 시 실제 이벤트가 등록됩니다.
 * </p>
 *
 * <pre>{@code
 * Events.listen(PlayerJoinEvent.class, event -> {
 *     event.getPlayer().sendMessage("Hello!");
 * })
 * .priority(EventPriority.HIGH)
 * .once()
 * .register();
 * }</pre>
 *
 * @param <T> 이벤트 타입
 */
public interface Events<T extends Event> {

    /**
     * 특정 이벤트 타입에 대한 리스너 작성을 시작합니다.
     *
     * @param type    이벤트 클래스
     * @param handler 이벤트 처리 로직
     * @param <T>     이벤트 타입
     * @return 이벤트 설정을 위한 {@link Events} 인스턴스
     */
    static <T extends Event> Events<T> listen(Class<T> type, Consumer<T> handler) {
        return new EventImpl<>(type, handler);
    }

    /** 이벤트 우선순위를 설정합니다. */
    Events<T> priority(EventPriority priority);

    /**
     * 이미 취소된 이벤트를 무시할지 여부를 설정합니다.
     *
     * @param value true일 경우 취소된 이벤트는 전달되지 않음
     */
    Events<T> ignoreCancelled(boolean value);

    /**
     * 핸들러 실행 이후 이벤트를 강제로 취소합니다.
     *
     * @param value true일 경우 handler 실행 후 setCancelled(true) 호출
     */
    Events<T> cancel(boolean value);

    /** 이벤트를 단 한 번만 실행되도록 설정합니다. */
    Events<T> once();

    /** 이벤트 단발 실행 여부를 명시적으로 설정합니다. */
    Events<T> once(boolean value);

    /**
     * 이벤트의 최대 실행 횟수를 제한합니다.
     *
     * @param count 최대 실행 횟수
     */
    Events<T> limit(int count);

    /**
     * 일정 시간 이후 이벤트 리스너를 자동 해제합니다.
     *
     * @param duration 만료 시간
     */
    Events<T> expireAfter(Duration duration);

    /** 실행 시간을 로그로 출력하는 디버그 모드를 활성화합니다. */
    Events<T> debug();

    /** 실행 시간 디버그 모드를 명시적으로 설정합니다. */
    Events<T> debug(boolean value);

    /**
     * 특정 조건을 만족할 경우에만 핸들러가 실행되도록 필터를 추가합니다.
     *
     * @param filter 이벤트 필터
     */
    Events<T> filter(Predicate<T> filter);

    /** 기본 플러그인을 사용하여 이벤트를 등록합니다. */
    EventHandle register();

    /**
     * 특정 플러그인을 지정하여 이벤트를 등록합니다.
     *
     * @param plugin Bukkit 플러그인
     */
    EventHandle register(Plugin plugin);
}
