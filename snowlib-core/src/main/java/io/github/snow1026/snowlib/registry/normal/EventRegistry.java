package io.github.snow1026.snowlib.registry.normal;

import io.github.snow1026.snowlib.SnowLibrary;
import io.github.snow1026.snowlib.annotations.event.SnowEvent;
import io.github.snow1026.snowlib.api.event.Events;
import io.github.snow1026.snowlib.utils.reflect.Reflection;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * 클래스 내의 {@link SnowEvent} 어노테이션을 찾아 자동으로 등록하는 스캐너입니다.
 * <p>
 * 이 클래스는 {@link Reflection} 유틸리티를 사용하여 안전하게 메서드에 접근합니다.
 * </p>
 */
public final class EventRegistry {

    private EventRegistry() {}

    public static void register(Object listener) {
        register(SnowLibrary.snowlibrary(), listener);
    }

    /**
     * 지정된 객체 인스턴스 내의 모든 @SnowEvent 메서드를 등록합니다.
     *
     * @param plugin   이벤트를 소유할 플러그인
     * @param listener 어노테이션이 포함된 클래스의 인스턴스
     */
    public static void register(Plugin plugin, Object listener) {
        // Reflection 유틸리티를 사용하여 모든 메서드 순회 (상속된 메서드 포함 가능)
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SnowEvent.class)) continue;

            SnowEvent anno = method.getAnnotation(SnowEvent.class);

            // 파라미터 확인 (하나여야 하며 Event를 상속받아야 함)
            if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                plugin.getLogger().warning("Invalid @SnowEvent method: " + method.getName());
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventType = (Class<? extends Event>) method.getParameterTypes()[0];

            // Events 빌더 구성
            Events<? extends Event> builder = Events.listen(eventType, event -> {
                        // 직접적인 method.invoke 대신 Reflection 유틸리티를 사용
                        // setAccessible(true) 및 예외 처리를 내부적으로 수행합니다.
                        Reflection.on(listener).call(method.getName(), event);
                    }).plugin(plugin).priority(anno.priority()).ignoreCancelled(anno.ignoreCancelled()).cancel(anno.cancel()).limit(anno.limit());

            // Duration 임포트 없이 Reflection으로 처리
            // builder.expireAfter(Duration.ofSeconds(anno.expireSeconds()))와 동일한 로직
            if (anno.expireSeconds() > 0) {
                Object duration = Reflection.on(java.time.Duration.class).call("ofSeconds", anno.expireSeconds()).get();

                // Reflection으로 builder의 expireAfter 메서드 호출
                Reflection.on(builder).call("expireAfter", duration);
            }

            // 디버그 설정
            if (anno.debug()) {
                String source = anno.debugSource().isEmpty() ? listener.getClass().getSimpleName() + "#" + method.getName() : anno.debugSource();
                builder.debug(source);
            }

            builder.register();
        }
    }
}
