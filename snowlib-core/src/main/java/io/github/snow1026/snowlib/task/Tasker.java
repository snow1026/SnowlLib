package io.github.snow1026.snowlib.task;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit 스케줄러 작업을 쉽고 직관적으로 생성하기 위한 진입점 클래스입니다.
 * 이 클래스를 통해 동기(Sync) 또는 비동기(Async) 작업을 체이닝 방식으로 시작할 수 있습니다.
 */
public class Tasker {
    private static Plugin plugin;

    /**
     * Tasker에서 사용할 플러그인 인스턴스를 초기화합니다.
     * 플러그인 활성화(onEnable) 시점에 호출되어야 합니다.
     *
     * @param plugin 작업을 등록할 플러그인 인스턴스
     */
    public static void init(@NotNull Plugin plugin) {
        Tasker.plugin = plugin;
    }

    /**
     * 메인 스레드에서 실행될 동기 작업을 생성합니다.
     * 마인크래프트 API(인벤토리, 엔티티 조작 등)를 다룰 때 사용합니다.
     *
     * @return 동기 작업을 위한 {@link TaskContext} 객체
     */
    public static TaskContext sync() {
        return new TaskContext(plugin, false);
    }

    /**
     * 별도의 스레드에서 실행될 비동기 작업을 생성합니다.
     * DB 연결, 파일 입출력, 복잡한 연산 등 메인 스레드 부하를 줄여야 할 때 사용합니다.
     *
     * @return 비동기 작업을 위한 {@link TaskContext} 객체
     */
    public static TaskContext async() {
        return new TaskContext(plugin, true);
    }
}
