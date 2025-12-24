// io.github.snow1026.snowlib.events.Filters
package io.github.snow1026.snowlib.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import java.util.function.Predicate;

public final class Filters {
    private Filters() {}

    public static <T extends Event> Predicate<T> ignoreCancelled() {
        return e -> !(e instanceof Cancellable c) || !c.isCancelled();
    }

    public static <T extends Event> Predicate<T> isPlayer() {
        return e -> e instanceof PlayerEvent || (e instanceof EntityEvent ee && ee.getEntity() instanceof Player);
    }

    public static <T extends Event> Predicate<T> hasPermission(String perm) {
        return e -> {
            if (e instanceof PlayerEvent pe) return pe.getPlayer().hasPermission(perm);
            return false;
        };
    }
}
