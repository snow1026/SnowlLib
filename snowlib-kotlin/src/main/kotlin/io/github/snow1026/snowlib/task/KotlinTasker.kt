package io.github.snow1026.snowlib.task

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.function.Consumer

val Int.ticks: Long get() = this.toLong()
val Int.seconds: Long get() = this.toLong() * 20L
val Int.minutes: Long get() = this.toLong() * 1200L

class KotlinTasker(val plugin: Plugin, val async: Boolean) {
    var delay: Long = 0L
    var period: Long = -1L
    var limit: Int = -1
    private var count = 0
    private var condition: () -> Boolean = { true }
    private var errorHandler: (Throwable) -> Unit = { it.printStackTrace() }

    private var nextSyncTask: (() -> Unit)? = null
    private var nextAsyncTask: (() -> Unit)? = null

    fun filter(condition: () -> Boolean) = apply { this.condition = condition }
    fun onError(handler: (Throwable) -> Unit) = apply { this.errorHandler = handler }

    fun limit(times: Int) = apply { this.limit = times }

    fun thenSync(block: () -> Unit) = apply { this.nextSyncTask = block }

    fun thenAsync(block: () -> Unit) = apply { this.nextAsyncTask = block }

    fun run(block: (BukkitTask) -> Unit) {
        val wrappedAction = Consumer<BukkitTask> { task ->
            try {
                if (!condition()) {
                    task.cancel()
                    handleCompletion()
                    return@Consumer
                }

                block(task)
                count++

                if (limit in 1..count) {
                    task.cancel()
                    handleCompletion()
                } else if (period == -1L) {
                    handleCompletion()
                }
            } catch (e: Exception) {
                errorHandler(e)
                task.cancel()
            }
        }

        return if (async) {
            if (period == -1L) Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, wrappedAction, delay)
            else Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, wrappedAction, delay, period)
        } else {
            if (period == -1L) Bukkit.getScheduler().runTaskLater(plugin, wrappedAction, delay)
            else Bukkit.getScheduler().runTaskTimer(plugin, wrappedAction, delay, period)
        }
    }

    private fun handleCompletion() {
        nextSyncTask?.let { Bukkit.getScheduler().runTask(plugin, it) }
        nextAsyncTask?.let { Bukkit.getScheduler().runTaskAsynchronously(plugin, it) }
    }
}

fun Plugin.syncTask(block: KotlinTasker.() -> Unit) = KotlinTasker(this, false).apply(block)
fun Plugin.asyncTask(block: KotlinTasker.() -> Unit) = KotlinTasker(this, true).apply(block)