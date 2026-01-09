package io.github.snow1026.snowlib.api.item;

import io.github.snow1026.snowlib.api.component.text.TextComponent;
import io.github.snow1026.snowlib.internal.item.PDCUtil;
import io.github.snow1026.snowlib.internal.item.SnowItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * {@link ItemStack}을 생성하고 수정하기 위한 플루언트(Fluent) API 빌더입니다.
 * <p>
 * 이 클래스는 Adventure 컴포넌트, PersistentDataContainer(PDC), 그리고
 * 가죽 갑옷 색상이나 머리 텍스처와 같은 특정 메타 데이터 수정을 지원합니다.
 */
public interface ItemBuilder {

    /**
     * 지정된 재질(Material)로 수량이 1개인 새 ItemBuilder를 생성합니다.
     *
     * @param material 아이템의 재질
     * @return 새로운 ItemBuilder 인스턴스
     */
    static ItemBuilder of(@NotNull Material material) {
        return new SnowItemBuilder(material, 1);
    }

    /**
     * 지정된 재질(Material)과 수량으로 새 ItemBuilder를 생성합니다.
     *
     * @param material 아이템의 재질
     * @param amount   아이템 수량
     * @return 새로운 ItemBuilder 인스턴스
     */
    static ItemBuilder of(@NotNull Material material, int amount) {
        return new SnowItemBuilder(material, amount);
    }

    /**
     * 기존 {@link ItemStack}을 기반으로 ItemBuilder를 생성합니다.
     * <p>
     * 원본 아이템이 수정되는 것을 방지하기 위해 아이템은 복제(clone)됩니다.
     *
     * @param itemStack 원본 ItemStack
     * @return 복제된 아이템을 감싸는 새로운 ItemBuilder 인스턴스
     */
    static ItemBuilder from(@NotNull ItemStack itemStack) {
        return new SnowItemBuilder(itemStack);
    }

    /**
     * 아이템의 표시 이름(Display Name)을 설정합니다.
     *
     * @param name 설정할 이름 (TextComponent), null일 경우 이름 없음
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder name(@Nullable TextComponent name);

    /**
     * 아이템의 표시 이름을 일반 문자열로 설정합니다.
     * <p>
     * 문자열은 자동으로 MiniMessage 포맷으로 파싱되어 적용됩니다.
     *
     * @param text 설정할 이름 문자열
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder name(@NotNull String text);

    /**
     * 아이템의 설명(Lore)을 설정합니다. 기존 Lore는 초기화됩니다.
     *
     * @param lines 설정할 Lore 줄 목록 (가변 인자)
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder lore(@NotNull TextComponent... lines);

    /**
     * 아이템의 설명(Lore)을 설정합니다. 기존 Lore는 초기화됩니다.
     *
     * @param lines 설정할 Lore 리스트
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder lore(@NotNull List<TextComponent> lines);

    /**
     * 아이템의 설명(Lore)을 문자열 배열로 설정합니다. 기존 Lore는 초기화됩니다.
     * <p>
     * 각 문자열은 MiniMessage 포맷으로 파싱됩니다.
     *
     * @param lines 설정할 Lore 문자열 배열
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder lore(@NotNull String... lines);

    /**
     * 기존 설명(Lore)의 끝에 한 줄을 추가합니다.
     *
     * @param line 추가할 Lore (TextComponent)
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder addLore(@NotNull TextComponent line);

    /**
     * 기존 설명(Lore)의 끝에 문자열 한 줄을 추가합니다.
     * <p>
     * 문자열은 MiniMessage 포맷으로 파싱됩니다.
     *
     * @param line 추가할 문자열
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder addLore(@NotNull String line);

    /**
     * 아이템의 수량을 설정합니다.
     *
     * @param amount 설정할 수량
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder amount(int amount);

    /**
     * 아이템에 플래그(ItemFlag)를 추가합니다.
     *
     * @param flags 추가할 플래그 목록
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder flag(@NotNull ItemFlag... flags);

    /**
     * 아이템의 모든 플래그를 숨깁니다 (인챈트, 속성, 물약 효과 등).
     *
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder hideAllFlags();

    /**
     * 아이템의 파괴 불가(Unbreakable) 여부를 설정합니다.
     *
     * @param unbreakable true면 파괴 불가, false면 내구도 적용
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder unbreakable(boolean unbreakable);

    /**
     * 아이템에 인챈트 광택(Glow) 효과를 강제로 부여합니다.
     * <p>
     * 실제 인챈트 기능은 없지만 시각적으로 빛나게 만듭니다.
     *
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder glow();

    /**
     * 아이템의 커스텀 모델 데이터(CustomModelData)를 설정합니다.
     * <p>
     * 리소스팩에서 텍스처를 매핑할 때 사용됩니다.
     *
     * @param data 모델 데이터 정수 값
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder modelData(int data);

    /**
     * 아이템에 인챈트를 추가합니다.
     *
     * @param enchant 추가할 인챈트 종류
     * @param level   인챈트 레벨
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder enchant(@NotNull Enchantment enchant, int level);

    /**
     * 빌더 체인 중간에 임의의 작업을 수행합니다.
     * <p>
     * 조건부 로직이나 복잡한 설정을 빌더 흐름을 끊지 않고 적용할 때 유용합니다.
     *
     * @param consumer 현재 빌더 인스턴스를 사용하는 Consumer
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder modify(@NotNull Consumer<ItemBuilder> consumer);

    /**
     * 특정 {@link ItemMeta} 하위 타입을 안전하게 수정합니다.
     * <p>
     * 현재 아이템의 메타가 해당 클래스의 인스턴스일 경우에만 Consumer가 실행됩니다.
     *
     * @param metaClass 수정할 메타 클래스 (예: LeatherArmorMeta.class)
     * @param consumer  메타를 수정하는 로직
     * @param <M>       메타 타입
     * @return 이 ItemBuilder 인스턴스
     */
    <M extends ItemMeta> ItemBuilder editMeta(@NotNull Class<M> metaClass, @NotNull Consumer<M> consumer);

    /**
     * 아이템의 손상도(Damage)를 설정합니다. 내구도가 있는 아이템에 적용됩니다.
     *
     * @param damage 손상도 값
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder damage(int damage);

    /**
     * 가죽 갑옷의 색상을 설정합니다.
     *
     * @param color 적용할 Bukkit Color
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder armorColor(@NotNull Color color);

    /**
     * 플레이어 머리(Skull) 아이템의 주인을 설정합니다.
     *
     * @param player 머리의 주인이 될 플레이어
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder skullOwner(@NotNull OfflinePlayer player);

    /**
     * Base64 인코딩된 텍스처 값을 사용하여 머리 스킨을 설정합니다.
     * <p>
     * 랜덤 UUID를 가진 프로필을 생성하여 텍스처를 적용합니다.
     *
     * @param base64 Base64 텍스처 문자열
     * @return 이 ItemBuilder 인스턴스
     */
    ItemBuilder skullTexture(@NotNull String base64);

    /**
     * PersistentDataContainer(PDC)에 데이터를 저장합니다.
     *
     * @param key   데이터 키 (네임스페이스 키의 키 부분)
     * @param value 저장할 값 (Integer, String, Long, Double, Float, Boolean 지원)
     * @return 이 ItemBuilder 인스턴스
     * @see PDCUtil#apply(ItemMeta, Map)
     */
    ItemBuilder pdc(@NotNull String key, @NotNull Object value);

    /**
     * 설정된 모든 속성을 적용하여 최종 {@link ItemStack}을 생성합니다.
     *
     * @return 생성된 ItemStack
     */
    ItemStack build();
}
