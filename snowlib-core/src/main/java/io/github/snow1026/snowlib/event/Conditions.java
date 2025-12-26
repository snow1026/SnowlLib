package io.github.snow1026.snowlib.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.function.Predicate;

/**
 * 이벤트 필터링에 자주 사용되는 조건식들을 모아놓은 유틸리티 클래스입니다.
 */
public final class Conditions {

    private Conditions() {}

    /** 항상 true를 반환하는 조건입니다. */
    public static <T extends Event> Predicate<T> alwaysTrue() {
        return e -> true;
    }

    /** 항상 false를 반환하는 조건입니다. */
    public static <T extends Event> Predicate<T> alwaysFalse() {
        return e -> false;
    }

    /**
     * 플레이어가 특정 권한을 가지고 있는지 검사하는 조건을 생성합니다.
     * @param permission 확인할 권한 노드
     * @return 권한 확인 Predicate
     */
    public static Predicate<Player> hasPermission(String permission) {
        return player -> player.hasPermission(permission);
    }
}
