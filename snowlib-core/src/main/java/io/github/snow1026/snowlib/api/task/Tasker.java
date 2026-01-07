package io.github.snow1026.snowlib.api.task;

import io.github.snow1026.snowlib.internal.task.SnowTasker;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Bukkit 스케줄러 작업을 쉽고 직관적으로 생성하기 위한 진입점 클래스입니다.
 * 이 클래스를 통해 동기(Sync) 또는 비동기(Async) 작업을 체이닝 방식으로 시작할 수 있습니다.
 */
public interface Tasker {

    /**
     * 메인 스레드에서 실행될 동기 작업을 생성합니다.
     * 마인크래프트 API(인벤토리, 엔티티 조작 등)를 다룰 때 사용합니다.
     *
     * @return 동기 작업을 위한 {@link Tasker} 객체
     */
    static Tasker sync() {
        return new SnowTasker(false);
    }

    /**
     * 별도의 스레드에서 실행될 비동기 작업을 생성합니다.
     * DB 연결, 파일 입출력, 복잡한 연산 등 메인 스레드 부하를 줄여야 할 때 사용합니다.
     *
     * @return 비동기 작업을 위한 {@link Tasker} 객체
     */
    static Tasker async() {
        return new SnowTasker(true);
    }

    /**
     * 작업 시작 전 대기할 시간을 설정합니다.
     * @param ticks 지연 시간 (20틱 = 1초)
     * @return 컨텍스트 인스턴스 (체이닝용)
     */
    Tasker delay(long ticks);

    /**
     * 작업의 반복 주기를 설정합니다.
     * @param period 반복 간격 (20틱 = 1초). -1일 경우 반복하지 않습니다.
     * @return 컨텍스트 인스턴스 (체이닝용)
     */
    Tasker repeat(long period);

    /**
     * 반복 작업의 최대 실행 횟수를 제한합니다.
     * @param times 최대 실행 횟수
     * @return 컨텍스트 인스턴스 (체이닝용)
     */
    Tasker limit(int times);

    /**
     * 작업 실행 전 검사할 조건을 추가합니다.
     * 반복 작업의 경우, 매 실행 전마다 검사하며 조건이 false일 경우 작업이 취소됩니다.
     * @param condition 실행 조건 (true를 반환해야 실행됨)
     * @return 컨텍스트 인스턴스 (체이닝용)
     */
    Tasker filter(BooleanSupplier condition);

    /**
     * 작업 실행 중 예외가 발생했을 때 처리할 핸들러를 설정합니다.
     * 기본값은 StackTrace를 출력하고 작업을 중단합니다.
     * @param handler 발생한 예외를 처리할 Consumer
     * @return 컨텍스트 인스턴스 (체이닝용)
     */
    Tasker onError(Consumer<Throwable> handler);

    /**
     * 정의된 조건에 따라 작업을 실행합니다.
     * @param taskConsumer {@link BukkitTask} 인스턴스를 사용하는 작업 내용
     */
    void run(Consumer<BukkitTask> taskConsumer);

    /**
     * 단순 Runnable을 사용하여 작업을 실행합니다.
     * @param runnable 실행할 코드 블록
     */
    void run(Runnable runnable);

    /**
     * 현재 작업이 완료된 후(단일 작업) 또는 종료된 후(반복 작업)
     * 메인 스레드에서 실행될 다음 작업을 예약합니다.
     * * @param nextRunnable 다음에 실행할 코드 블록
     */
    void thenSync(Runnable nextRunnable);

    /**
     * 현재 작업이 완료된 후(단일 작업) 또는 종료된 후(반복 작업)
     * 비동기 스레드에서 실행될 다음 작업을 예약합니다.
     * * @param nextRunnable 다음에 실행할 코드 블록
     */
    void thenAsync(Runnable nextRunnable);
}
