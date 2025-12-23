package io.github.snow1026.snowlib.gui.events;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public record GUIMoveItemEvent(Player player, InventoryMoveItemEvent event) {}
