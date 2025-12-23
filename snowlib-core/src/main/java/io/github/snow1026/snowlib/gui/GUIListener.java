package io.github.snow1026.snowlib.gui;

import io.github.snow1026.snowlib.events.Events;
import io.github.snow1026.snowlib.gui.events.*;
import io.github.snow1026.snowlib.internals.gui.GUIImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;

public final class GUIListener {

    public static void setup() {
        Events.listen(InventoryClickEvent.class, event -> {
            if (event.getInventory().getHolder() instanceof GUIImpl gui) {
                Player player = (Player) event.getWhoClicked();
                GUIClickEvent guiEvent = new GUIClickEvent(player, event);

                if (gui.getClickHandler() != null) gui.getClickHandler().accept(guiEvent);

                GUISlot slot = gui.getSlots().get(event.getRawSlot());
                if (slot != null && slot.getClickHandler() != null) {
                    slot.getClickHandler().accept(guiEvent);
                }
            }
        }).register();

        Events.listen(InventoryOpenEvent.class, event -> {
            if (event.getInventory().getHolder() instanceof GUIImpl gui) {
                if (gui.getOpenHandler() != null) {
                    gui.getOpenHandler().accept(new GUIOpenEvent((Player) event.getPlayer(), event));
                }
            }
        }).register();

        Events.listen(InventoryCloseEvent.class, event -> {
            if (event.getInventory().getHolder() instanceof GUIImpl gui) {
                if (gui.getCloseHandler() != null) {
                    gui.getCloseHandler().accept(new GUICloseEvent((Player) event.getPlayer(), event));
                }
            }
        }).register();

        Events.listen(InventoryDragEvent.class, event -> {
            if (event.getInventory().getHolder() instanceof GUIImpl gui) {
                if (gui.getDragHandler() != null) {
                    gui.getDragHandler().accept(new GUIDragEvent((Player) event.getWhoClicked(), event));
                }
            }
        }).register();

        Events.listen(InventoryInteractEvent.class, event -> {
            if (event.getInventory().getHolder() instanceof GUIImpl gui) {
                if (gui.getInteractHandler() != null) {
                    gui.getInteractHandler().accept(new GUIInteractEvent((Player) event.getWhoClicked(), event));
                }
            }
        }).register();

        Events.listen(InventoryMoveItemEvent.class, event -> {
            Inventory holderInv = event.getSource().getHolder() instanceof GUIImpl ? event.getSource() :
                    (event.getDestination().getHolder() instanceof GUIImpl ? event.getDestination() : null);

            if (holderInv != null && holderInv.getHolder() instanceof GUIImpl gui) {
                if (gui.getMoveItemHandler() != null) {
                    gui.getMoveItemHandler().accept(new GUIMoveItemEvent(null, event));
                }
            }
        }).register();
    }
}
