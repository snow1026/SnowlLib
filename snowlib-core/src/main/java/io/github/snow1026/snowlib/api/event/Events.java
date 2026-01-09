package io.github.snow1026.snowlib.api.event;

import io.github.snow1026.snowlib.internal.event.SnowEvents;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * SnowLib 이벤트 시스템의 메인 진입점입니다.
 *
 * <p>
 * 이 인터페이스는 Bukkit의 표준 이벤트 리스너 시스템을 람다 기반의 DSL(Domain Specific Language) 형태로
 * 래핑하여 제공합니다. 메서드 체이닝을 통해 필터, 제한, 만료 조건 등을 직관적으로 설정할 수 있습니다.
 * </p>
 *
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * Events.listen(PlayerJoinEvent.class, event -> {
 * event.getPlayer().sendMessage("환영합니다!");
 * })
 * .plugin(myPluginInstance) // 필수: 플러그인 인스턴스 지정
 * .priority(EventPriority.HIGH)
 * .filter(Filters.hasPermission("admin.join"))
 * .limit(10)
 * .register();
 * }</pre>
 *
 * @param <T> 처리할 Bukkit 이벤트 타입
 */
public interface Events<T extends Event> {

    /**
     * 특정 이벤트 타입에 대한 리스너 빌더를 생성합니다.
     *
     * @param type    구독할 이벤트 클래스 (예: {@code PlayerMoveEvent.class})
     * @param handler 이벤트 발생 시 실행할 로직
     * @param <T>     이벤트 제네릭 타입
     * @return 설정을 위한 {@link Events} 빌더 인스턴스
     */
    static <T extends Event> Events<T> listen(Class<T> type, Consumer<T> handler) {
        return new SnowEvents<>(type, handler);
    }

    /**
     * 이벤트를 등록할 플러그인 인스턴스를 설정합니다.
     * <p>
     * 라이브러리 사용 시, 이벤트를 소유할 플러그인을 명시해야 합니다.
     * </p>
     *
     * @param plugin 이벤트를 등록하는 주체 플러그인
     * @return 빌더 인스턴스
     */
    Events<T> plugin(Plugin plugin);

    /**
     * 이벤트 실행 우선순위를 설정합니다.
     * 기본값은 {@link EventPriority#NORMAL} 입니다.
     *
     * @param priority Bukkit 이벤트 우선순위
     * @return 빌더 인스턴스
     */
    Events<T> priority(EventPriority priority);

    /**
     * 이미 취소된(Cancelled) 이벤트도 무시하지 않고 수신할지 설정합니다.
     *
     * @param ignore true일 경우 취소된 이벤트는 핸들러로 전달되지 않습니다. (기본값: false)
     * @return 빌더 인스턴스
     */
    Events<T> ignoreCancelled(boolean ignore);

    /**
     * 핸들러 로직 실행 후 이벤트를 강제로 취소 상태로 변경합니다.
     *
     * @param value true일 경우 {@code setCancelled(true)}를 호출합니다.
     * @return 빌더 인스턴스
     */
    Events<T> cancel(boolean value);

    /**
     * 이벤트를 단 한 번만 실행하고 자동으로 등록을 해제합니다.
     * {@code limit(1)}과 동일합니다.
     *
     * @return 빌더 인스턴스
     */
    Events<T> once();

    /**
     * 이벤트의 최대 실행 횟수를 제한합니다.
     * 지정된 횟수만큼 실행된 후 리스너는 자동으로 등록 해제됩니다.
     *
     * @param count 최대 실행 횟수
     * @return 빌더 인스턴스
     */
    Events<T> limit(int count);

    /**
     * 이벤트 등록 후 일정 시간이 지나면 자동으로 등록을 해제합니다.
     *
     * @param duration 유효 기간
     * @return 빌더 인스턴스
     */
    Events<T> expireAfter(Duration duration);

    /**
     * 이벤트 실행 간의 쿨다운(대기 시간)을 설정합니다.
     * 쿨다운 중에 발생한 이벤트는 무시됩니다.
     *
     * @param duration 쿨다운 시간
     * @return 빌더 인스턴스
     */
    Events<T> cooldown(Duration duration);

    /**
     * 이벤트 실행 중 예외가 발생했을 때 처리할 핸들러를 설정합니다.
     * 설정하지 않을 경우 기본적으로 스택 트레이스를 콘솔에 출력합니다.
     *
     * @param exceptionHandler 예외 처리 컨슈머 (이벤트와 예외 객체 전달)
     * @return 빌더 인스턴스
     */
    Events<T> exceptionHandler(BiConsumer<T, Throwable> exceptionHandler);

    /**
     * 특정 조건을 만족할 경우에만 핸들러가 실행되도록 필터를 추가합니다.
     * 여러 번 호출하여 다중 필터를 적용할 수 있습니다.
     *
     * @param filter true를 반환해야 핸들러가 실행되는 조건식
     * @return 빌더 인스턴스
     */
    Events<T> filter(Predicate<T> filter);

    /**
     * 디버그 모드를 활성화합니다.
     * 활성화 시 실행 시간이 로그로 출력되며, 호출 소스가 추적 시스템에 기록됩니다.
     */
    Events<T> debug();

    /**
     * 특정 식별자와 함께 디버그 모드를 설정합니다.
     * @param source 등록 주체를 나타내는 이름 (예: "QuestSystem", "LoginHandler")
     */
    Events<T> debug(String source);

    /**
     * 설정된 옵션을 바탕으로 이벤트를 실제 Bukkit 시스템에 등록합니다.
     *
     * @return 등록된 리스너를 제어(해제)할 수 있는 구독 객체
     * @throws IllegalStateException 플러그인이 설정되지 않은 경우
     */
    Subscription register();
}