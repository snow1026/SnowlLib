package io.github.snow1026.snowlib.api.gui.event;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public record GUICloseEvent(Player player, InventoryCloseEvent event) {}
