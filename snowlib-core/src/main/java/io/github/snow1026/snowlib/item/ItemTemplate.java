package io.github.snow1026.snowlib.item;

import io.github.snow1026.snowlib.component.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 아이템의 기본 형태를 정의하고 플레이스홀더를 통해 동적으로 생성하는 템플릿 클래스입니다.
 * <p>
 * 미리 정의된 재질, 이름 형식, 모델 데이터를 저장해두고 {@link #build(Map)} 호출 시
 * 데이터를 주입하여 아이템을 생성합니다.
 */
public final class ItemTemplate {
    private final Material material;
    private TextComponent name;
    private int customModelData = 0;

    private ItemTemplate(Material material) {
        this.material = material;
    }

    /**
     * 지정된 재질로 아이템 템플릿을 생성합니다.
     *
     * @param material 아이템 재질
     * @return 새로운 ItemTemplate 인스턴스
     */
    public static ItemTemplate of(Material material) {
        return new ItemTemplate(material);
    }

    /**
     * 템플릿의 이름 형식을 설정합니다.
     * <p>
     * 이 이름은 {@link #build(Map)} 시점에 플레이스홀더가 치환됩니다.
     *
     * @param name 이름 템플릿 (TextComponent)
     * @return 이 ItemTemplate 인스턴스
     */
    public ItemTemplate name(TextComponent name) {
        this.name = name;
        return this;
    }

    /**
     * 템플릿의 커스텀 모델 데이터를 설정합니다.
     *
     * @param data 모델 데이터 값
     * @return 이 ItemTemplate 인스턴스
     */
    public ItemTemplate modelData(int data) {
        this.customModelData = data;
        return this;
    }

    /**
     * 플레이스홀더 맵을 사용하여 실제 {@link ItemStack}을 생성합니다.
     * <p>
     * 템플릿에 설정된 이름의 플레이스홀더(예: {@code <player_name>})가 맵의 값으로 치환됩니다.
     *
     * @param placeholders 치환할 키-값 쌍이 담긴 맵
     * @return 생성된 ItemStack
     */
    public ItemStack build(@NotNull Map<String, Object> placeholders) {
        TextComponent resolvedName = null;
        if (this.name != null) {
            resolvedName = TextComponent.of(name.raw()).mm().placeholders(placeholders);
        }

        return ItemBuilder.of(material).name(resolvedName).modelData(customModelData).build();
    }
}
