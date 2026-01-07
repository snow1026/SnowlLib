package io.github.snow1026.snowlib.internal.task;

import io.github.snow1026.snowlib.api.task.Tasker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SnowTasker implements Tasker{
    private static Plugin plugin;
    private final boolean async;
    private long delay = 0L;
    private long period = -1L;
    private int limit = -1;
    private int count = 0;
    private BooleanSupplier condition = () -> true;
    private Consumer<Throwable> errorHandler = Throwable::printStackTrace;

    public SnowTasker(boolean async) {
        this.async = async;
    }

    public static void init(@NotNull Plugin instance) {
        plugin = instance;
    }

    public static Plugin plugin() {
        return plugin;
    }

    @Override
    public SnowTasker delay(long ticks) {
        this.delay = ticks;
        return this;
    }

    @Override
    public SnowTasker repeat(long period) {
        this.period = period;
        return this;
    }

    @Override
    public SnowTasker limit(int times) {
        this.limit = times;
        return this;
    }

    @Override
    public SnowTasker filter(BooleanSupplier condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public SnowTasker onError(Consumer<Throwable> handler) {
        this.errorHandler = handler;
        return this;
    }

    @Override
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

    @Override
    public void run(Runnable runnable) {
        run(task -> runnable.run());
    }

    @Override
    public void thenSync(Runnable nextRunnable) {
        run(task -> {
            if (period == -1L || (limit > 0 && count >= limit)) {
                Tasker.sync().run(nextRunnable);
            }
        });
    }

    @Override
    public void thenAsync(Runnable nextRunnable) {
        run(task -> {
            if (period == -1L || (limit > 0 && count >= limit)) {
                Tasker.async().run(nextRunnable);
            }
        });
    }
}
