package io.github.snow1026.snowlib.events

import io.github.snow1026.snowlib.annotations.scanner.EventSubscriberScanner
import io.github.snow1026.snowlib.lifecycle.EventRegistry
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.plugin.Plugin
import java.time.Duration

/**
 * 1. 플러그인 메인 클래스나 어디서든 이벤트를 여는 진입점
 */
fun Plugin.snowEvents(block: SnowEventScope.() -> Unit) {
    SnowEventScope(this).apply(block)
}

/**
 * 2. 애노테이션 기반 스캔 확장 함수 (MethodHandle & Reflection 유틸 사용)
 */
fun Any.registerListeners() {
    EventSubscriberScanner.scan(this)
}

/**
 * 3. 전역 인터셉터 등록을 위한 DSL
 */
fun globalInterceptor(interceptor: EventInterceptor) {
    EventRegistry.addGlobalInterceptor(interceptor)
}

/**
 * 4. DSL 스코프 클래스
 */
class SnowEventScope(val plugin: Plugin) {

    /**
     * 가장 기본적인 형태: subscribe<T> { ... }
     */
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

    /**
     * 모든 기능을 세밀하게 설정하는 빌더 형태 DSL
     */
    inline fun <reified T : Event> on(
        noinline handler: (T) -> Unit,
        crossinline builderBlock: EventBuilder<T>.() -> Unit
    ): EventHandle {
        val builder = Events.listen(T::class.java, handler)
        builder.builderBlock() // 모든 Java EventBuilder 메서드 사용 가능
        return builder.register(plugin)
    }
}

/**
 * Kotlin을 위한 추가 편의 확장 함수들
 */
fun <T : Event> EventBuilder<T>.filterIf(predicate: (T) -> Boolean): EventBuilder<T> = this.filter(predicate)
fun <T : Event> EventBuilder<T>.expireAfter(seconds: Long): EventBuilder<T> = this.expireAfter(Duration.ofSeconds(seconds))
fun <T : Event> EventBuilder<T>.once(): EventBuilder<T> = this.once(true)
