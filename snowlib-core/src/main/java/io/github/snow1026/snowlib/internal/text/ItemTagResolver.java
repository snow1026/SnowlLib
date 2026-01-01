package io.github.snow1026.snowlib.internal.text;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 아이템을 Component로 변환하는 내부 유틸리티입니다.
 */
final class ItemTagResolver {

    private ItemTagResolver() {}

    public static Component resolve(@NotNull ItemStack item) {
        if (item.getType().isAir()) {
            return Component.text("Air");
        }
        return item.displayName().hoverEvent(item.asHoverEvent());
    }
}
