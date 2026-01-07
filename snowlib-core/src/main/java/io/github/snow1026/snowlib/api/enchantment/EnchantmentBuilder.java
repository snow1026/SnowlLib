package io.github.snow1026.snowlib.api.enchantment;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.api.component.enchantment.EnchantmentComponent;
import io.github.snow1026.snowlib.api.component.enchantment.EnchantmentCost;
import io.github.snow1026.snowlib.api.component.enchantment.EnchantmentItemSet;
import io.github.snow1026.snowlib.api.component.enchantment.EnchantmentSlot;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@link SnowEnchantment} 객체를 생성하기 위한 빌더 클래스입니다.
 */
public class EnchantmentBuilder {
    private final SnowKey key;
    private String name;
    private Set<Material> supportedItems = Set.of();
    private EnchantmentItemSet primaryItems = null;
    private int weight = 10;
    private int maxLevel = 1;
    private EnchantmentCost minCost = new EnchantmentCost(1, 1);
    private EnchantmentCost maxCost = new EnchantmentCost(20, 1);
    private int anvilCost = 4;
    private final List<EnchantmentSlot> slots = new ArrayList<>();
    private boolean isTreasure = false;
    private boolean isCurse = false;
    private boolean isTradeable = true;
    private boolean isDiscoverable = true;
    private boolean isEnchantable = true;

    public EnchantmentBuilder(SnowKey key) {
        this.key = key;
        this.name = key.path();
    }

    /** 인챈트의 표시 이름을 설정합니다. */
    public EnchantmentBuilder display(String name) {
        this.name = name;
        return this;
    }

    /** 인챈트가 적용 가능한 모든 아이템 재질을 설정합니다. */
    public EnchantmentBuilder supportedItems(Material... materials) {
        this.supportedItems = Set.of(materials);
        return this;
    }

    /** 인챈트가 주로 적용되는 아이템 재질을 설정합니다. */
    public EnchantmentBuilder primaryItems(Material... materials) {
        this.primaryItems = EnchantmentItemSet.of(materials);
        return this;
    }

    /** 인챈트의 등장 확률 가중치를 설정합니다. (기본값: 10) */
    public EnchantmentBuilder weight(int weight) {
        this.weight = weight;
        return this;
    }

    /** 인챈트의 최대 레벨을 설정합니다. (기본값: 1) */
    public EnchantmentBuilder maxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    /**
     * 인챈트 비용(Level Cost)을 설정합니다.
     * @param minBase 최소 기본 비용
     * @param minPerLevel 레벨당 최소 증가 비용
     * @param maxBase 최대 기본 비용
     * @param maxPerLevel 레벨당 최대 증가 비용
     */
    public EnchantmentBuilder cost(int minBase, int minPerLevel, int maxBase, int maxPerLevel) {
        this.minCost = new EnchantmentCost(minBase, minPerLevel);
        this.maxCost = new EnchantmentCost(maxBase, maxPerLevel);
        return this;
    }

    /** 모루 작업 시의 비용 가중치를 설정합니다. */
    public EnchantmentBuilder anvilCost(int anvilCost) {
        this.anvilCost = anvilCost;
        return this;
    }

    /** 이 인챈트의 효과가 활성화될 장비 슬롯을 설정합니다. */
    public EnchantmentBuilder slots(EquipmentSlot... slots) {
        this.slots.add(EnchantmentSlot.of(slots));
        return this;
    }

    /** 보물 인챈트 여부를 설정합니다. */
    public EnchantmentBuilder treasure(boolean val) {
        this.isTreasure = val;
        return this;
    }

    /** 저주 인챈트 여부를 설정합니다. */
    public EnchantmentBuilder curse(boolean val) {
        this.isCurse = val;
        return this;
    }

    /** 주민 거래 가능 여부를 설정합니다. */
    public EnchantmentBuilder tradeable(boolean val) {
        this.isTradeable = val;
        return this;
    }

    /** 세계 탐험 중 발견 가능 여부를 설정합니다. */
    public EnchantmentBuilder discoverable(boolean val) {
        this.isDiscoverable = val;
        return this;
    }

    /** 마법 부여 가능 여부를 설정합니다. */
    public EnchantmentBuilder enchantable(boolean val) {
        this.isEnchantable = val;
        return this;
    }

    /**
     * 설정된 속성들을 바탕으로 {@link SnowEnchantment} 인스턴스를 생성합니다.
     * @return 생성된 인챈트 객체
     */
    public SnowEnchantment build() {
        EnchantmentComponent component = new EnchantmentComponent(name, new EnchantmentItemSet(supportedItems), Optional.ofNullable(primaryItems), weight, maxLevel, minCost, maxCost, anvilCost, slots.isEmpty() ? List.of(EnchantmentSlot.of(EquipmentSlot.values())) : slots, isTreasure, isCurse, isTradeable, isDiscoverable, isEnchantable);
        return new SnowEnchantment(key, component) {};
    }
}
