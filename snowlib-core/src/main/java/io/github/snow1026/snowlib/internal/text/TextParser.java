package io.github.snow1026.snowlib.internal.text;

import io.github.snow1026.snowlib.component.text.TextComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 내부적인 텍스트 파싱 및 변환 로직을 담당하는 유틸리티 클래스입니다.
 */
public final class TextParser {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private static final Pattern KEY_SANITIZER = Pattern.compile("[{}<>]");

    private TextParser() {}

    public static Component parseMiniMessage(@NotNull String input, @NotNull List<TagResolver> resolvers) {
        try {
            return MM.deserialize(input, TagResolver.resolver(resolvers));
        } catch (Exception e) {
            return Component.text(input);
        }
    }

    public static Component parseSimple(@NotNull String input, @NotNull List<TagResolver> resolvers) {
        return MM.deserialize(MM.escapeTags(input), TagResolver.resolver(resolvers));
    }

    /**
     * 입력된 객체 타입에 맞춰 가장 적절한 TagResolver를 생성합니다.
     */
    public static TagResolver createSmartResolver(@NotNull String key, @Nullable Object value) {
        String safeKey = KEY_SANITIZER.matcher(key).replaceAll("");

        if (value == null) return Placeholder.unparsed(safeKey, "null");

        return switch (value) {
            case ComponentLike cl -> Placeholder.component(safeKey, cl);
            case TextComponent tc -> Placeholder.component(safeKey, tc.build());
            case ItemStack item -> Placeholder.component(safeKey, ItemTagResolver.resolve(item)); // 아이템 처리
            case Number num -> Placeholder.unparsed(safeKey, String.valueOf(num));
            default -> Placeholder.unparsed(safeKey, String.valueOf(value));
        };
    }

    public static String toLegacy(Component c) { return LEGACY.serialize(c); }
    public static String toPlain(Component c) { return PLAIN.serialize(c); }
    public static String toMiniMessage(Component c) { return MM.serialize(c); }
}
