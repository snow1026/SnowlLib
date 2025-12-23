package io.github.snow1026.snowlib.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.function.Predicate;

public final class Conditions {

    private Conditions() {}

    public static <T extends Event> Predicate<T> alwaysTrue() {
        return e -> true;
    }

    public static <T extends Event> Predicate<T> alwaysFalse() {
        return e -> false;
    }

    public static Predicate<Player> hasPermission(String permission) {
        return player -> player.hasPermission(permission);
    }
}
