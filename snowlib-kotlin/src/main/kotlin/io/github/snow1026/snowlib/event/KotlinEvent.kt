package io.github.snow1026.snowlib.event

import io.github.snow1026.snowlib.api.event.Events
import io.github.snow1026.snowlib.api.event.Subscription
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.plugin.Plugin

/**
 * 코틀린 사용자를 위한 이벤트 리스너 DSL 생성기입니다.
 */
inline fun <reified T : Event> Plugin.subscribe(noinline handler: (T) -> Unit): Events<T> {
    return Events.listen(T::class.java, handler).plugin(this)
}

/**
 * 설정을 커스터마이징하고 바로 등록하는 Kotlin DSL 확장 함수입니다.
 */
inline fun <reified T : Event> Plugin.listen(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, crossinline builder: Events<T>.() -> Unit = {}, noinline handler: (T) -> Unit): Subscription {
    val events = Events.listen(T::class.java, handler).plugin(this).priority(priority).ignoreCancelled(ignoreCancelled)

    events.apply(builder)
    return events.register()
}

/**
 * 한번만 실행되는 이벤트를 위한 간축형 함수
 */
inline fun <reified T : Event> Plugin.listenOnce(noinline handler: (T) -> Unit): Subscription = subscribe(handler).once().register()
