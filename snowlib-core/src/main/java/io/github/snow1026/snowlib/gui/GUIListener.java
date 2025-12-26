package io.github.snow1026.snowlib.gui;

import io.github.snow1026.snowlib.event.Events;
import io.github.snow1026.snowlib.gui.event.*;
import io.github.snow1026.snowlib.internal.gui.GUIImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;

/**
 * Bukkit의 인벤토리 이벤트를 감지하여 {@link GUI} 시스템으로 전달하는 리스너 클래스입니다.
 */
public final class GUIListener {

    /**
     * 라이브러리 작동에 필요한 인벤토리 이벤트 리스너들을 등록합니다.
     * 플러그인 활성화 시 한 번 호출되어야 합니다.
     */
    public static void setup() {

        // 1. 클릭 이벤트 처리 (쿨타임 체크 및 슬롯별 핸들러 실행)
        Events.listen(InventoryClickEvent.class, event -> {
            if (event.getInventory().getHolder() instanceof GUIImpl gui) {
                Player player = (Player) event.getWhoClicked();

                // 쿨타임 중이면 이벤트 취소
                if (!gui.checkCooldown(player)) {
                    event.setCancelled(true);
                    return;
                }

                GUIClickEvent guiEvent = new GUIClickEvent(player, event);

                // 전체 GUI 클릭 핸들러 실행
                if (gui.getClickHandler() != null) {
                    gui.getClickHandler().accept(guiEvent);
                }

                // 개별 슬롯 클릭 핸들러 실행
                GUISlot slot = gui.getSlots().get(event.getRawSlot());
                if (slot != null && slot.getClickHandler() != null) {
                    slot.getClickHandler().accept(guiEvent);
                }
            }
        }).register();

        // 2. GUI 열기 이벤트
        Events.listen(InventoryOpenEvent.class, event -> {
            if (event.getInventory().getHolder() instanceof GUIImpl gui) {
                if (gui.getOpenHandler() != null) {
                    gui.getOpenHandler().accept(new GUIOpenEvent((Player) event.getPlayer(), event));
                }
            }
        }).register();

        // 3. GUI 닫기 이벤트 (업데이트 태스크 중지 포함)
        Events.listen(InventoryCloseEvent.class, event -> {
            if (event.getInventory().getHolder() instanceof GUIImpl gui) {
                gui.stopUpdateTask();

                if (gui.getCloseHandler() != null) {
                    gui.getCloseHandler().accept(new GUICloseEvent((Player) event.getPlayer(), event));
                }
            }
        }).register();

        // 4. 드래그 이벤트
        Events.listen(InventoryDragEvent.class, event -> {
            if (event.getInventory().getHolder() instanceof GUIImpl gui) {
                if (gui.getDragHandler() != null) {
                    gui.getDragHandler().accept(new GUIDragEvent((Player) event.getWhoClicked(), event));
                }
            }
        }).register();

        // 5. 기타 상호작용 이벤트
        Events.listen(InventoryInteractEvent.class, event -> {
            if (event.getInventory().getHolder() instanceof GUIImpl gui) {
                if (gui.getInteractHandler() != null) {
                    gui.getInteractHandler().accept(new GUIInteractEvent((Player) event.getWhoClicked(), event));
                }
            }
        }).register();

        // 6. 아이템 이동 이벤트 (깔때기 등 외부 요인에 의한 이동 감지)
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
