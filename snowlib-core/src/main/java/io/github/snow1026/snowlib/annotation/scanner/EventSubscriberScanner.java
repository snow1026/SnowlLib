package io.github.snow1026.snowlib.annotation.scanner;

import io.github.snow1026.snowlib.annotation.event.SubscribeEvent;
import io.github.snow1026.snowlib.event.EventHandle;
import io.github.snow1026.snowlib.event.Events;
import io.github.snow1026.snowlib.util.reflect.Reflection;
import org.bukkit.event.Event;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 클래스 내의 @SubscribeEvent 어노테이션을 찾아 자동으로 이벤트를 등록합니다.
 */
public final class EventSubscriberScanner {
    @SuppressWarnings("unchecked")
    public static List<EventHandle> scan(Object instance) {
        List<EventHandle> registeredHandles = new ArrayList<>();
        Class<?> checkClass = instance.getClass();

        // 상위 클래스까지 탐색
        while (checkClass != null && checkClass != Object.class) {
            for (Method m : checkClass.getDeclaredMethods()) {
                SubscribeEvent sub = m.getAnnotation(SubscribeEvent.class);
                if (sub == null || Modifier.isStatic(m.getModifiers())) continue;

                // 파라미터 유효성 검사 (1개이며 Event의 하위 클래스여야 함)
                if (m.getParameterCount() != 1 || !Event.class.isAssignableFrom(m.getParameterTypes()[0])) continue;

                Class<? extends Event> eventClass = (Class<? extends Event>) m.getParameterTypes()[0];

                try {
                    Reflection.MethodInvoker invoker = Reflection.getTypedMethod(checkClass, m.getName(), m.getReturnType(), m.getParameterTypes());

                    // 리플렉션을 통해 Events.listen API에 바인딩
                    EventHandle handle = Events.listen(eventClass, e -> invoker.invoke(instance, e))
                            .priority(sub.priority())
                            .ignoreCancelled(sub.ignoreCancelled())
                            .once(sub.once())
                            .cancel(sub.forceCancel())
                            .debug(sub.debug())
                            .register();

                    registeredHandles.add(handle);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to bind event method: " + m.getName(), e);
                }
            }
            checkClass = checkClass.getSuperclass();
        }
        return registeredHandles;
    }
}
