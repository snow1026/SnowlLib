package io.github.snow1026.snowlib.gui.events;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public record GUIClickEvent(Player player, InventoryClickEvent event) {}
