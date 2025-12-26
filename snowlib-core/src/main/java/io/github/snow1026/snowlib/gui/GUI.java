package io.github.snow1026.snowlib.gui;

import io.github.snow1026.snowlib.gui.event.*;
import io.github.snow1026.snowlib.internal.gui.GUIImpl;
import io.github.snow1026.snowlib.util.reflect.Reflection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.function.Consumer;

/**
 * 사용자 인터페이스(GUI)를 정의하는 메인 인터페이스입니다.
 * 가독성 좋은 빌더 패턴 스타일의 메서드를 제공합니다.
 */
public interface GUI {

    /**
     * 새로운 GUI 인스턴스를 생성합니다.
     * * @param rows  GUI의 줄 수 (1~6)
     * @param title GUI 상단에 표시될 제목
     * @return 생성된 GUI 인스턴스
     */
    static GUI create(int rows, String title) {
        return (GUI) Reflection.getConstructor(GUIImpl.class).invoke(rows, title);
    }

    /**
     * 특정 인덱스(슬롯)에 대한 설정을 수행합니다.
     * * @param indexes 설정할 슬롯 번호들 (0부터 시작)
     * @return 해당 슬롯 설정을 위한 {@link GUISlot} 객체
     */
    GUISlot slot(int... indexes);

    /**
     * GUI의 모든 빈 슬롯을 특정 아이템으로 채웁니다.
     * * @param item 채울 아이템
     * @return GUI 인스턴스 (체이닝용)
     */
    GUI fill(ItemStack item);

    /** GUI 클릭 시 실행할 핸들러를 등록합니다. */
    GUI onClick(Consumer<GUIClickEvent> handler);
    /** GUI가 열릴 때 실행할 핸들러를 등록합니다. */
    GUI onOpen(Consumer<GUIOpenEvent> handler);
    /** GUI가 닫힐 때 실행할 핸들러를 등록합니다. */
    GUI onClose(Consumer<GUICloseEvent> handler);
    /** GUI 내에서 아이템 드래그 시 실행할 핸들러를 등록합니다. */
    GUI onDrag(Consumer<GUIDragEvent> handler);
    /** GUI 내 상호작용 발생 시 실행할 핸들러를 등록합니다. */
    GUI onInteract(Consumer<GUIInteractEvent> handler);
    /** GUI 내에서 아이템 이동 발생 시 실행할 핸들러를 등록합니다. */
    GUI onMoveItem(Consumer<GUIMoveItemEvent> handler);

    /**
     * 플레이어에게 GUI를 엽니다.
     * * @param player GUI를 보여줄 플레이어
     */
    void open(Player player);

    /** @return GUI의 총 줄(Row) 수 */
    int getRows();

    /**
     * 문자열 배열을 이용해 GUI의 레이아웃을 설정합니다.
     * * @param layout   문자열 배열 (예: {"#####", "#A#B#"})
     * @param bindings 각 문자에 대응하는 슬롯 설정 매핑
     * @return GUI 인스턴스 (체이닝용)
     */
    GUI applyLayout(String[] layout, java.util.Map<Character, Consumer<GUISlot>> bindings);

    /**
     * 클릭 방지를 위한 쿨타임을 설정합니다.
     * * @param millis 밀리초 단위 쿨타임
     * @return GUI 인스턴스 (체이닝용)
     */
    GUI cooldown(long millis);

    /**
     * 플레이어에게 GUI를 열며, 이전 GUI 기록 저장 여부를 선택합니다.
     * * @param player      GUI를 보여줄 플레이어
     * @param saveHistory true일 경우 현재 GUI를 히스토리에 저장하여 '뒤로가기'가 가능하게 함
     */
    void open(Player player, boolean saveHistory);

    /**
     * 플레이어를 이전 GUI 화면으로 되돌립니다.
     * * @param player 뒤로 이동할 플레이어
     */
    static void back(Player player) { GUIManager.back(player); }

    /**
     * 일정 주기마다 반복 실행될 업데이트 작업을 설정합니다.
     * * @param ticks      반복 주기 (틱 단위, 20틱 = 1초)
     * @param updateTask 실행할 작업
     * @return GUI 인스턴스 (체이닝용)
     */
    GUI updateInterval(long ticks, Consumer<GUI> updateTask);

    /** @return 설정된 클릭 쿨타임 (밀리초) */
    long getCooldown();
}
