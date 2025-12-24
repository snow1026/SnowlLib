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

public final class EventSubscriberScanner {

    @SuppressWarnings("unchecked")
    public static List<EventHandle> scan(Object instance) {
        List<EventHandle> registeredHandles = new ArrayList<>();
        Class<?> checkClass = instance.getClass();

        while (checkClass != null && checkClass != Object.class) {
            for (Method m : checkClass.getDeclaredMethods()) {

                SubscribeEvent sub = m.getAnnotation(SubscribeEvent.class);
                if (sub == null) continue;

                if (Modifier.isStatic(m.getModifiers())) continue;

                if (m.getParameterCount() != 1 || !Event.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    continue;
                }

                Class<? extends Event> eventClass = (Class<? extends Event>) m.getParameterTypes()[0];

                try {
                    Reflection.MethodInvoker invoker = Reflection.getTypedMethod(checkClass, m.getName(), m.getReturnType(), m.getParameterTypes());

                    EventHandle handle = Events.listen(eventClass, e -> {invoker.invoke(instance, e);})
                            .priority(sub.priority())
                            .ignoreCancelled(sub.ignoreCancelled())
                            .once(sub.once())
                            .cancel(sub.forceCancel())
                            .debug(sub.debug())
                            .register();

                    registeredHandles.add(handle);

                } catch (Exception e) {
                    throw new RuntimeException("SnowLib Reflection failed to bind event: " + m.getName(), e);
                }
            }
            checkClass = checkClass.getSuperclass();
        }

        return registeredHandles;
    }
}
