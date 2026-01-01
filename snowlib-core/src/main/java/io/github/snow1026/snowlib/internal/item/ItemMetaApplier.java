package io.github.snow1026.snowlib.internal.item;

import io.github.snow1026.snowlib.component.text.TextComponent;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public final class ItemMetaApplier {
    private ItemMetaApplier() {}

    public static void apply(ItemMeta meta, TextComponent name, List<TextComponent> lore) {
        if (name != null) {
            meta.displayName(name.build());
        }

        if (lore != null && !lore.isEmpty()) {
            List<Component> components = lore.stream().map(TextComponent::build).collect(Collectors.toList());
            meta.lore(components);
        }
    }
}
