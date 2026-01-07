package io.github.snow1026.snowlib.api.gui;

import io.github.snow1026.snowlib.api.gui.event.GUIClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * GUI 내의 특정 슬롯(칸)에 대한 설정과 동작을 정의하는 클래스입니다.
 */
public final class GUISlot {
    private final GUI parent;
    private final int[] indexes;
    private final List<Consumer<GUISlot>> modifications = new ArrayList<>();
    private Predicate<Player> visibilityCondition = p -> true;

    private ItemStack item;
    private Consumer<GUIClickEvent> clickHandler;

    /**
     * 새로운 슬롯 설정 객체를 생성합니다.
     * * @param parent  이 슬롯이 속한 상위 GUI
     * @param indexes 슬롯 번호들
     */
    public GUISlot(GUI parent, int... indexes) {
        this.parent = parent;
        this.indexes = indexes;
    }

    /**
     * 해당 슬롯에 표시될 아이템을 설정합니다.
     * * @param item 표시할 아이템
     */
    public void item(ItemStack item) {
        this.item = item;
    }

    /**
     * 이 슬롯을 클릭했을 때 실행할 동작을 등록합니다.
     * * @param handler 실행할 핸들러
     * @return 슬롯 인스턴스 (체이닝용)
     */
    public GUISlot onClick(Consumer<GUIClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    /** @return 상위 GUI 객체 반환 */
    public GUI gui() { return parent; }

    /** @return 설정된 슬롯 인덱스 배열 */
    public int[] getIndexes() { return indexes; }
    /** @return 설정된 아이템 */
    public ItemStack getItem() { return item; }
    /** @return 설정된 클릭 핸들러 */
    public Consumer<GUIClickEvent> getClickHandler() { return clickHandler; }

    /**
     * 특정 조건이 충족될 때만 플레이어에게 이 슬롯이 보이도록 설정합니다.
     * * @param condition 플레이어 객체를 조건으로 하는 조건식
     * @return 슬롯 인스턴스 (체이닝용)
     */
    public GUISlot visibleIf(Predicate<Player> condition) {
        this.visibilityCondition = condition;
        return this;
    }

    /**
     * 특정 플레이어가 이 슬롯을 볼 수 있는지 확인합니다.
     * * @param player 확인할 플레이어
     * @return 노출 가능 여부
     */
    public boolean canSee(Player player) {
        return visibilityCondition.test(player);
    }
}
