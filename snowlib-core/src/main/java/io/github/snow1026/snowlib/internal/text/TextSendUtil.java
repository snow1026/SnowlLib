package io.github.snow1026.snowlib.internal.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * 실제 패킷 전송을 담당하는 유틸리티입니다.
 */
public final class TextSendUtil {
    private TextSendUtil() {}

    public static void send(@NotNull CommandSender sender, @NotNull Component component) {
        sender.sendMessage(component);
    }

    public static void sendActionBar(@NotNull CommandSender sender, @NotNull Component component) {
        sender.sendActionBar(component);
    }

    public static void sendTitle(@NotNull CommandSender sender, @NotNull Component main, @NotNull Component sub, int in, int stay, int out) {
        Title.Times times = Title.Times.times(Duration.ofMillis(in * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(out * 50L));
        Title title = Title.title(main, sub, times);
        sender.showTitle(title);
    }
}
