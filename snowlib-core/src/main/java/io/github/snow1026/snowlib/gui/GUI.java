package io.github.snow1026.snowlib.gui;

import io.github.snow1026.snowlib.gui.events.*;
import io.github.snow1026.snowlib.internals.gui.GUIImpl;
import io.github.snow1026.snowlib.utils.reflect.Reflection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.function.Consumer;

public interface GUI {
    static GUI create(int rows, String title) {
        return (GUI) Reflection.getConstructor(GUIImpl.class).invoke(rows, title);
    }

    GUISlot slot(int... indexes);

    GUI fill(ItemStack item);

    GUI onClick(Consumer<GUIClickEvent> handler);
    GUI onOpen(Consumer<GUIOpenEvent> handler);
    GUI onClose(Consumer<GUICloseEvent> handler);
    GUI onDrag(Consumer<GUIDragEvent> handler);
    GUI onInteract(Consumer<GUIInteractEvent> handler);
    GUI onMoveItem(Consumer<GUIMoveItemEvent> handler);

    void open(Player player);

    int getRows();
}
