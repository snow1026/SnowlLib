package io.github.snow1026.snowlib.task;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class TaskContext {
    private final Plugin plugin;
    private final boolean async;
    private long delay = 0L;
    private long period = -1L;
    private int limit = -1;
    private int count = 0;
    private BooleanSupplier condition = () -> true;
    private Consumer<Throwable> errorHandler = Throwable::printStackTrace;

    protected TaskContext(Plugin plugin, boolean async) {
        this.plugin = plugin;
        this.async = async;
    }

    public TaskContext delay(long ticks) { this.delay = ticks; return this; }
    public TaskContext repeat(long period) { this.period = period; return this; }
    public TaskContext limit(int times) { this.limit = times; return this; }
    public TaskContext filter(BooleanSupplier condition) { this.condition = condition; return this; }
    public TaskContext onError(Consumer<Throwable> handler) { this.errorHandler = handler; return this; }

    public void run(Consumer<BukkitTask> taskConsumer) {
        Consumer<BukkitTask> wrappedAction = task -> {
            try {
                if (!condition.getAsBoolean()) {
                    task.cancel();
                    return;
                }

                taskConsumer.accept(task);
                count++;

                if (limit > 0 && count >= limit) {
                    task.cancel();
                }
            } catch (Exception e) {
                errorHandler.accept(e);
                task.cancel();
            }
        };

        if (async) {
            if (period == -1L) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, wrappedAction, delay);
                return;
            }
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, wrappedAction, delay, period);
        } else {
            if (period == -1L) {
                Bukkit.getScheduler().runTaskLater(plugin, wrappedAction, delay);
                return;
            }
            Bukkit.getScheduler().runTaskTimer(plugin, wrappedAction, delay, period);
        }
    }

    public void run(Runnable runnable) {
        run(task -> runnable.run());
    }

    public void thenSync(Runnable nextRunnable) {
        run(task -> {
            if (period == -1L || (limit > 0 && count >= limit)) {
                Tasker.sync().run(nextRunnable);
            }
        });
    }

    public void thenAsync(Runnable nextRunnable) {
        run(task -> {
            if (period == -1L || (limit > 0 && count >= limit)) {
                Tasker.async().run(nextRunnable);
            }
        });
    }
}
