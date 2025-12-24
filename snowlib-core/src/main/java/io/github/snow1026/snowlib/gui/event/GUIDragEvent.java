package io.github.snow1026.snowlib.gui.event;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryDragEvent;

public record GUIDragEvent(Player player, InventoryDragEvent event) {}
