package io.github.snow1026.snowlib.component.text;

import org.jetbrains.annotations.NotNull;
import java.util.Map;

/**
 * 재사용 가능한 메시지 템플릿(Record)입니다.
 * 반복적으로 사용되는 메시지 형식을 정의하고 캐싱하는 데 유용합니다.
 */
public record TextTemplate(@NotNull String template, boolean isMiniMessage) {

    public static TextTemplate of(String template) {
        return new TextTemplate(template, true);
    }

    /**
     * 템플릿을 기반으로 새로운 TextComponent를 생성합니다.
     */
    public TextComponent apply() {
        TextComponent tc = TextComponent.of(template);
        if (isMiniMessage) tc.mm();
        return tc;
    }

    /**
     * 태그를 적용하여 TextComponent를 생성합니다.
     */
    public TextComponent apply(@NotNull Map<String, Object> tags) {
        return apply().tags(tags);
    }
}
