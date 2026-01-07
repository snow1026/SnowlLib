package io.github.snow1026.snowlib.api.gui.event;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;

public record GUIOpenEvent(Player player, InventoryOpenEvent event) {}
