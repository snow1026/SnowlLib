package io.github.snow1026.snowlib.api.gui.event;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public record GUIClickEvent(Player player, InventoryClickEvent event) {}
