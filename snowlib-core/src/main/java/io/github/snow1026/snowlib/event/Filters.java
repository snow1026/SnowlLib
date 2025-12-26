package io.github.snow1026.snowlib.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import java.util.function.Predicate;

public final class Filters {
    private Filters() {}

    /** 취소된 이벤트를 걸러내는 필터 */
    public static <T extends Event> Predicate<T> ignoreCancelled() {
        return e -> !(e instanceof Cancellable c) || !c.isCancelled();
    }

    /** 이벤트 주체가 플레이어인지 확인하는 필터 */
    public static <T extends Event> Predicate<T> isPlayer() {
        return e -> e instanceof PlayerEvent || (e instanceof EntityEvent ee && ee.getEntity() instanceof Player);
    }

    /** 플레이어가 특정 권한을 가지고 있는지 확인하는 필터 */
    public static <T extends Event> Predicate<T> hasPermission(String perm) {
        return e -> {
            if (e instanceof PlayerEvent pe) return pe.getPlayer().hasPermission(perm);
            return false;
        };
    }
}
