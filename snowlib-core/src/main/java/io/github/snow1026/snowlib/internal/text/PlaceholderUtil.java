package io.github.snow1026.snowlib.internal.text;

import io.github.snow1026.snowlib.api.component.text.TextComponent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 문자열 내의 {key} 형태를 값으로 치환하는 유틸리티입니다.
 * <p>
 * 이 클래스는 Adventure의 TagResolver가 아닌, 순수 문자열 치환(String.replace)을 수행합니다.
 * </p>
 */
public final class PlaceholderUtil {

    private PlaceholderUtil() {}

    /**
     * 텍스트 내의 {key}를 맵의 value로 치환합니다.
     *
     * @param input        원본 문자열
     * @param placeholders 치환할 키-값 맵
     * @return 치환된 문자열
     */
    public static String resolve(@NotNull String input, @NotNull Map<String, Object> placeholders) {
        String result = input;

        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String key = "{" + entry.getKey() + "}"; // {key} 형태
            result = result.replace(key, stringify(entry.getValue()));
        }

        return result;
    }

    /**
     * 객체를 문자열로 변환합니다.
     * Component나 TextComponent가 들어올 경우 MiniMessage 포맷으로 직렬화합니다.
     */
    private static String stringify(@Nullable Object value) {
        return switch (value) {
            case null -> "null";
            case TextComponent text -> text.toMiniMessage();
            case Component component -> TextParser.toMiniMessage(component);
            case Player player -> player.getName(); // 플레이어는 이름으로 치환
            default -> String.valueOf(value);
        };
    }
}
