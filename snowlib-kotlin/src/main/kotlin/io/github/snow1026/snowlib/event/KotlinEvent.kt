package io.github.snow1026.snowlib.event

import io.github.snow1026.snowlib.annotation.scanner.EventSubscriberScanner
import io.github.snow1026.snowlib.lifecycle.EventRegistry
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.plugin.Plugin
import java.time.Duration

fun Plugin.snowEvents(block: SnowEventScope.() -> Unit) {
    SnowEventScope(this).apply(block)
}

fun Any.registerListeners() {
    EventSubscriberScanner.scan(this)
}

fun globalInterceptor(interceptor: EventInterceptor) {
    EventRegistry.addGlobalInterceptor(interceptor)
}

class SnowEventScope(val plugin: Plugin) {

    inline fun <reified T : Event> subscribe(
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        noinline handler: (T) -> Unit
    ): EventHandle {
        return Events.listen(T::class.java, handler)
            .priority(priority)
            .ignoreCancelled(ignoreCancelled)
            .register(plugin)
    }

    inline fun <reified T : Event> on(
        noinline handler: (T) -> Unit,
        crossinline builderBlock: EventBuilder<T>.() -> Unit
    ): EventHandle {
        val builder = Events.listen(T::class.java, handler)
        builder.builderBlock() // 모든 Java EventBuilder 메서드 사용 가능
        return builder.register(plugin)
    }
}

fun <T : Event> EventBuilder<T>.filterIf(predicate: (T) -> Boolean): EventBuilder<T> = this.filter(predicate)
fun <T : Event> EventBuilder<T>.expireAfter(seconds: Long): EventBuilder<T> = this.expireAfter(Duration.ofSeconds(seconds))
fun <T : Event> EventBuilder<T>.once(): EventBuilder<T> = this.once(true)
