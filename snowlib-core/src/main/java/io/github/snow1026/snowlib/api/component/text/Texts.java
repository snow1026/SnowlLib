package io.github.snow1026.snowlib.api.component.text;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * SnowLib 텍스트 처리의 진입점(Entry Point)입니다.
 * <p>
 * 이 클래스는 {@link TextComponent} 빌더를 생성하거나 간단한 변환 작업을 수행하는 정적 팩토리 메서드를 제공합니다.
 * </p>
 *
 * @author Snow1026
 * @since 1.0.0
 */
public final class Texts {

    private Texts() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 새로운 TextComponent 빌더를 생성합니다.
     *
     * @param text 원본 텍스트 (MiniMessage 포맷 또는 일반 텍스트)
     * @return 빌더 인스턴스
     */
    public static TextComponent of(@NotNull String text) {
        return TextComponent.of(text);
    }

    /**
     * MiniMessage 문자열을 즉시 Component로 파싱합니다.
     * <p>
     * 별도의 태그(Tag)나 플레이스홀더가 필요 없는 정적 텍스트에 사용하기 적합합니다.
     * </p>
     *
     * @param text MiniMessage 포맷 텍스트
     * @return 파싱된 Component
     */
    public static Component mm(@NotNull String text) {
        return TextComponent.of(text).mm().build();
    }

    /**
     * 번역 키를 기반으로 TextComponent 빌더를 생성합니다.
     * {@link TextConfig#getTranslationProvider()}에 등록된 공급자를 통해 텍스트를 불러옵니다.
     *
     * @param key 번역 키 (예: "message.welcome")
     * @return 빌더 인스턴스
     */
    public static TextComponent translatable(@NotNull String key) {
        return TextComponent.translatable(key);
    }
}
