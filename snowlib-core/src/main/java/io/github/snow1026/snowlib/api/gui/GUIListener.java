package io.github.snow1026.snowlib.api.gui;

import io.github.snow1026.snowlib.api.gui.event.*;
import io.github.snow1026.snowlib.internal.gui.SnowGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;

public final class GUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof SnowGUI gui) {
            Player player = (Player) event.getWhoClicked();
            if (!gui.checkCooldown(player)) {
                event.setCancelled(true);
                return;
            }
            GUIClickEvent guiEvent = new GUIClickEvent(player, event);
            if (gui.getClickHandler() != null) {
                gui.getClickHandler().accept(guiEvent);
            }
            GUISlot slot = gui.getSlots().get(event.getRawSlot());
            if (slot != null && slot.getClickHandler() != null) {
                slot.getClickHandler().accept(guiEvent);
            }
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof SnowGUI gui) {
            if (gui.getOpenHandler() != null) {
                gui.getOpenHandler().accept(new GUIOpenEvent((Player) event.getPlayer(), event));
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof SnowGUI gui) {
            gui.stopUpdateTask();

            if (gui.getCloseHandler() != null) {
                gui.getCloseHandler().accept(new GUICloseEvent((Player) event.getPlayer(), event));
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SnowGUI gui) {
            if (gui.getDragHandler() != null) {
                gui.getDragHandler().accept(new GUIDragEvent((Player) event.getWhoClicked(), event));
            }
        }
    }

    @EventHandler
    public void onInteract(InventoryInteractEvent event) {
        if (event.getInventory().getHolder() instanceof SnowGUI gui) {
            if (gui.getInteractHandler() != null) {
                gui.getInteractHandler().accept(new GUIInteractEvent((Player) event.getWhoClicked(), event));
            }
        }
    }

    @EventHandler
    public void onMoveItem(InventoryMoveItemEvent event) {
        Inventory holderInv = event.getSource().getHolder() instanceof SnowGUI ? event.getSource() :
                (event.getDestination().getHolder() instanceof SnowGUI ? event.getDestination() : null);

        if (holderInv != null && holderInv.getHolder() instanceof SnowGUI gui) {
            if (gui.getMoveItemHandler() != null) {
                gui.getMoveItemHandler().accept(new GUIMoveItemEvent(null, event));
            }
        }
    }
}
