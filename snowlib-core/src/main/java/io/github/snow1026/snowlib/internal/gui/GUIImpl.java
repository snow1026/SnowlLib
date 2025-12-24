package io.github.snow1026.snowlib.internal.gui;

import io.github.snow1026.snowlib.gui.GUI;
import io.github.snow1026.snowlib.gui.GUIManager;
import io.github.snow1026.snowlib.gui.GUISlot;
import io.github.snow1026.snowlib.gui.event.*;
import io.github.snow1026.snowlib.lifecycle.EventRegistry;
import io.github.snow1026.snowlib.util.Adventure;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

    private long cooldownMillis = 0;
    private final Map<UUID, Long> lastClickMap = new HashMap<>();
    private BukkitTask updateTask;

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
    public GUI applyLayout(String[] layout, Map<Character, Consumer<GUISlot>> bindings) {
        for (int r = 0; r < layout.length && r < rows; r++) {
            String rowStr = layout[r];
            for (int c = 0; c < rowStr.length() && c < 9; c++) {
                char symbol = rowStr.charAt(c);
                if (bindings.containsKey(symbol)) {
                    bindings.get(symbol).accept(this.slot(r * 9 + c));
                }
            }
        }
        return this;
    }

    @Override
    public GUI cooldown(long millis) {
        this.cooldownMillis = millis;
        return this;
    }

    @Override
    public GUI updateInterval(long ticks, Consumer<GUI> task) {
        if (this.updateTask != null) this.updateTask.cancel();
        this.updateTask = Bukkit.getScheduler().runTaskTimer(EventRegistry.getLifecycle().plugin(), () -> task.accept(this), ticks, ticks);
        return this;
    }

    @Override
    public long getCooldown() {
        return this.cooldownMillis;
    }

    @Override
    public void open(Player player, boolean saveHistory) {
        if (saveHistory) GUIManager.saveHistory(player, this);

        getSlots().forEach((index, slot) -> {
            if (slot.canSee(player)) {
                getInventory().setItem(index, slot.getItem());
            } else {
                getInventory().setItem(index, null);
            }
        });
        player.openInventory(getInventory());
    }

    public boolean checkCooldown(Player player) {
        if (cooldownMillis <= 0) return true;
        long now = System.currentTimeMillis();
        long last = lastClickMap.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < cooldownMillis) return false;
        lastClickMap.put(player.getUniqueId(), now);
        return true;
    }

    public void stopUpdateTask() {
        if (updateTask != null) updateTask.cancel();
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
