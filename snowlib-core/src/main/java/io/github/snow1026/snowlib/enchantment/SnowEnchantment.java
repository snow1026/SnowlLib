package io.github.snow1026.snowlib.enchantment;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.component.enchantment.EnchantmentComponent;
import io.github.snow1026.snowlib.component.enchantment.EnchantmentCost;
import io.github.snow1026.snowlib.component.enchantment.EnchantmentItemSet;
import io.github.snow1026.snowlib.component.enchantment.EnchantmentSlot;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Optional;

/**
 * 시스템 내부에서 사용되는 커스텀 인챈트의 기본 추상 클래스입니다.
 * 인챈트의 속성 데이터(Component)를 관리하고 Bukkit API와의 연결을 담당합니다.
 */
public abstract class SnowEnchantment {
    public final SnowKey key;
    public final EnchantmentComponent component;

    protected SnowEnchantment(SnowKey key, EnchantmentComponent component) {
        this.key = key;
        this.component = component;
    }

    /** @return 인챈트의 고유 키 */
    public SnowKey key() {
        return key;
    }

    /** @return 인챈트의 세부 속성이 담긴 컴포넌트 */
    public EnchantmentComponent component() {
        return component;
    }

    /** @return 인챈트의 표시 이름 */
    public String name() {
        return component.name();
    }

    /** @return 이 인챈트를 부여할 수 있는 전체 아이템 목록 */
    public EnchantmentItemSet supportedItems() {
        return component.supportedItems();
    }

    /** @return 인챈트 테이블 등에서 우선적으로 나타나는 주요 아이템 목록 */
    public Optional<EnchantmentItemSet> primaryItems() {
        return component.primaryItems();
    }

    /** @return 인챈트의 희귀도 가중치 (높을수록 자주 등장) */
    public int weight() {
        return component.weight();
    }

    /** @return 인챈트의 최대 레벨 */
    public int maxLevel() {
        return component.maxLevel();
    }

    /** @return 인챈트 부여에 필요한 최소 비용 설정 */
    public EnchantmentCost minCost() {
        return component.minCost();
    }

    /** @return 인챈트 부여에 허용되는 최대 비용 설정 */
    public EnchantmentCost maxCost() {
        return component.maxCost();
    }

    /** @return 모루에서 합칠 때 레벨당 추가되는 비용 */
    public int anvilCost() {
        return component.anvilCost();
    }

    /** @return 인챈트가 적용되는 장비 슬롯 목록 (머리, 가슴, 손 등) */
    public List<EnchantmentSlot> slots() {
        return component.slots();
    }

    /** @return 보물 인챈트 여부 (전리품 상자나 낚시 등으로만 획득 가능) */
    public boolean isTreasure() {
        return component.isTreasure();
    }

    /** @return 저주 인챈트 여부 */
    public boolean isCurse() {
        return component.isCurse();
    }

    /** @return 주민 거래를 통해 획득 가능한지 여부 */
    public boolean isTradeable() {
        return component.isTradeable();
    }

    /** @return 마법 부여대 등에서 발견 가능한지 여부 */
    public boolean isDiscoverable() {
        return component.isDiscoverable();
    }

    /** @return 책 등을 통해 아이템에 부여 가능한지 여부 */
    public boolean isEnchantable() {
        return component.isEnchantable();
    }

    /**
     * 이 객체를 바탕으로 등록된 Bukkit {@link Enchantment} 인스턴스를 가져옵니다.
     * @return Bukkit 인챈트 객체
     * @throws RuntimeException 레지스트리에 키가 등록되어 있지 않은 경우 발생
     */
    public Enchantment bukkit() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(key.bukkit());
    }
}
