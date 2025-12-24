package io.github.snow1026.snowlib.gui;

import io.github.snow1026.snowlib.gui.event.GUIClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class GUISlot {
    private final GUI parent;
    private final int[] indexes;
    private final List<Consumer<GUISlot>> modifications = new ArrayList<>();
    private Predicate<Player> visibilityCondition = p -> true;

    private ItemStack item;
    private Consumer<GUIClickEvent> clickHandler;

    public GUISlot(GUI parent, int... indexes) {
        this.parent = parent;
        this.indexes = indexes;
    }

    public void item(ItemStack item) {
        this.item = item;
    }

    public GUISlot onClick(Consumer<GUIClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    public GUI gui() { return parent; }

    public int[] getIndexes() { return indexes; }
    public ItemStack getItem() { return item; }
    public Consumer<GUIClickEvent> getClickHandler() { return clickHandler; }

    public GUISlot visibleIf(Predicate<Player> condition) {
        this.visibilityCondition = condition;
        return this;
    }

    public boolean canSee(Player player) {
        return visibilityCondition.test(player);
    }
}
