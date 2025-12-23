package io.github.snow1026.snowlib.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

public final class GUIManager {
    private static final Map<UUID, Stack<GUI>> history = new HashMap<>();

    public static void saveHistory(Player player, GUI gui) {
        history.computeIfAbsent(player.getUniqueId(), k -> new Stack<>()).push(gui);
    }

    public static void back(Player player) {
        Stack<GUI> stack = history.get(player.getUniqueId());
        if (stack == null || stack.size() <= 1) {
            player.closeInventory();
            return;
        }
        stack.pop(); // 현재 GUI 제거
        GUI previous = stack.peek();
        previous.open(player, false);
    }
}
