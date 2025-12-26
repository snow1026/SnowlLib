package io.github.snow1026.snowlib.task;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * 작업의 실행 환경과 조건을 정의하는 컨텍스트 클래스입니다.
 * 빌더 패턴을 사용하여 작업의 지연, 반복, 필터링 등을 설정할 수 있습니다.
 */
public class TaskContext {
    private final Plugin plugin;
    private final boolean async;
    private long delay = 0L;
    private long period = -1L;
    private int limit = -1;
    private int count = 0;
    private BooleanSupplier condition = () -> true;
    private Consumer<Throwable> errorHandler = Throwable::printStackTrace;

    /**
     * 새로운 작업 컨텍스트를 생성합니다.
     * @param plugin 작업을 실행할 플러그인
     * @param async 비동기 실행 여부 (true일 경우 비동기)
     */
    protected TaskContext(Plugin plugin, boolean async) {
        this.plugin = plugin;
        this.async = async;
    }

    /**
     * 작업 시작 전 대기할 시간을 설정합니다.
     * @param ticks 지연 시간 (20틱 = 1초)
     * @return 컨텍스트 인스턴스 (체이닝용)
     */
    public TaskContext delay(long ticks) { this.delay = ticks; return this; }

    /**
     * 작업의 반복 주기를 설정합니다.
     * @param period 반복 간격 (20틱 = 1초). -1일 경우 반복하지 않습니다.
     * @return 컨텍스트 인스턴스 (체이닝용)
     */
    public TaskContext repeat(long period) { this.period = period; return this; }

    /**
     * 반복 작업의 최대 실행 횟수를 제한합니다.
     * @param times 최대 실행 횟수
     * @return 컨텍스트 인스턴스 (체이닝용)
     */
    public TaskContext limit(int times) { this.limit = times; return this; }

    /**
     * 작업 실행 전 검사할 조건을 추가합니다.
     * 반복 작업의 경우, 매 실행 전마다 검사하며 조건이 false일 경우 작업이 취소됩니다.
     * @param condition 실행 조건 (true를 반환해야 실행됨)
     * @return 컨텍스트 인스턴스 (체이닝용)
     */
    public TaskContext filter(BooleanSupplier condition) { this.condition = condition; return this; }

    /**
     * 작업 실행 중 예외가 발생했을 때 처리할 핸들러를 설정합니다.
     * 기본값은 StackTrace를 출력하고 작업을 중단합니다.
     * @param handler 발생한 예외를 처리할 Consumer
     * @return 컨텍스트 인스턴스 (체이닝용)
     */
    public TaskContext onError(Consumer<Throwable> handler) { this.errorHandler = handler; return this; }

    /**
     * 정의된 조건에 따라 작업을 실행합니다.
     * @param taskConsumer {@link BukkitTask} 인스턴스를 사용하는 작업 내용
     */
    public void run(Consumer<BukkitTask> taskConsumer) {
        Consumer<BukkitTask> wrappedAction = task -> {
            try {
                // 실행 전 조건 검사
                if (!condition.getAsBoolean()) {
                    task.cancel();
                    return;
                }

                taskConsumer.accept(task);
                count++;

                // 제한 횟수 도달 시 취소
                if (limit > 0 && count >= limit) {
                    task.cancel();
                }
            } catch (Exception e) {
                errorHandler.accept(e);
                task.cancel(); // 예외 발생 시 안전을 위해 작업 중단
            }
        };

        // Bukkit 스케줄러에 작업 등록
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

    /**
     * 단순 Runnable을 사용하여 작업을 실행합니다.
     * @param runnable 실행할 코드 블록
     */
    public void run(Runnable runnable) {
        run(task -> runnable.run());
    }

    /**
     * 현재 작업이 완료된 후(단일 작업) 또는 종료된 후(반복 작업)
     * 메인 스레드에서 실행될 다음 작업을 예약합니다.
     * * @param nextRunnable 다음에 실행할 코드 블록
     */
    public void thenSync(Runnable nextRunnable) {
        run(task -> {
            if (period == -1L || (limit > 0 && count >= limit)) {
                Tasker.sync().run(nextRunnable);
            }
        });
    }

    /**
     * 현재 작업이 완료된 후(단일 작업) 또는 종료된 후(반복 작업)
     * 비동기 스레드에서 실행될 다음 작업을 예약합니다.
     * * @param nextRunnable 다음에 실행할 코드 블록
     */
    public void thenAsync(Runnable nextRunnable) {
        run(task -> {
            if (period == -1L || (limit > 0 && count >= limit)) {
                Tasker.async().run(nextRunnable);
            }
        });
    }
}
