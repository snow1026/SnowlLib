package io.github.snow1026.snowlib.api.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

/**
 * 플레이어별 GUI 열람 기록(History)을 관리하는 매니저 클래스입니다.
 */
public final class GUIManager {
    /** 플레이어 UUID별 GUI 스택 저장소 */
    private static final Map<UUID, Stack<GUI>> history = new HashMap<>();

    /**
     * 특정 플레이어의 GUI 히스토리에 새로운 GUI를 추가합니다.
     * * @param player 대상 플레이어
     * @param gui    추가할 GUI
     */
    public static void saveHistory(Player player, GUI gui) {
        history.computeIfAbsent(player.getUniqueId(), k -> new Stack<>()).push(gui);
    }

    /**
     * 플레이어를 직전에 열었던 GUI로 이동시킵니다.
     * 저장된 기록이 없거나 하나뿐이면 인벤토리를 닫습니다.
     * * @param player 뒤로 이동할 플레이어
     */
    public static void back(Player player) {
        Stack<GUI> stack = history.get(player.getUniqueId());
        if (stack == null || stack.size() <= 1) {
            player.closeInventory();
            return;
        }

        stack.pop(); // 현재 GUI 제거
        GUI previous = stack.peek(); // 이전 GUI 확인
        previous.open(player, false); // 기록 저장 없이 열기
    }
}
