package io.github.snow1026.snowlib.internals.gui;

import io.github.snow1026.snowlib.gui.GUI;
import io.github.snow1026.snowlib.gui.GUISlot;
import io.github.snow1026.snowlib.gui.events.*;
import io.github.snow1026.snowlib.utils.Adventure;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GUIImpl implements GUI, InventoryHolder {

    private final int rows;
    private final Inventory inventory;
    private final Map<Integer, GUISlot> slots = new HashMap<>();

    private Consumer<GUIClickEvent> clickHandler;
    private Consumer<GUIOpenEvent> openHandler;
    private Consumer<GUICloseEvent> closeHandler;
    private Consumer<GUIDragEvent> dragHandler;
    private Consumer<GUIInteractEvent> interactHandler;
    private Consumer<GUIMoveItemEvent> moveItemHandler;

    public GUIImpl(int rows, String title) {
        this.rows = rows;
        this.inventory = Bukkit.createInventory(this, rows * 9, Adventure.mm(title));
    }

    @Override
    public GUISlot slot(int... indexes) {
        GUISlot slot = new GUISlot(this, indexes);
        for (int index : indexes) {
            slots.put(index, slot);
        }
        return slot;
    }

    @Override
    public GUI fill(ItemStack item) {
        for (int i = 0; i < rows * 9; i++) {
            inventory.setItem(i, item);
        }
        return this;
    }

    @Override
    public GUI onClick(Consumer<GUIClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    @Override
    public GUI onOpen(Consumer<GUIOpenEvent> handler) {
        this.openHandler = handler;
        return this;
    }

    @Override
    public GUI onClose(Consumer<GUICloseEvent> handler) {
        this.closeHandler = handler;
        return this;
    }

    @Override
    public GUI onDrag(Consumer<GUIDragEvent> handler) {
        this.dragHandler = handler;
        return this;
    }

    @Override
    public GUI onInteract(Consumer<GUIInteractEvent> handler) {
        this.interactHandler = handler;
        return this;
    }

    @Override
    public GUI onMoveItem(Consumer<GUIMoveItemEvent> handler) {
        this.moveItemHandler = handler;
        return this;
    }

    @Override
    public void open(Player player) {
        slots.forEach((index, guiSlot) -> {
            if (index >= 0 && index < inventory.getSize()) {
                inventory.setItem(index, guiSlot.getItem());
            }
        });
        player.openInventory(inventory);
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public Map<Integer, GUISlot> getSlots() {
        return slots;
    }

    public Consumer<GUIClickEvent> getClickHandler() {
        return clickHandler;
    }

    public Consumer<GUIOpenEvent> getOpenHandler() {
        return openHandler;
    }

    public Consumer<GUICloseEvent> getCloseHandler() {
        return closeHandler;
    }

    public Consumer<GUIDragEvent> getDragHandler() {
        return dragHandler;
    }

    public Consumer<GUIInteractEvent> getInteractHandler() {
        return interactHandler;
    }

    public Consumer<GUIMoveItemEvent> getMoveItemHandler() {
        return moveItemHandler;
    }
}
