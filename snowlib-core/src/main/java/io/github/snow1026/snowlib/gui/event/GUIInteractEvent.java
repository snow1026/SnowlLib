package io.github.snow1026.snowlib.gui.event;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryInteractEvent;

public record GUIInteractEvent(Player player, InventoryInteractEvent event) {}
