package io.github.snow1026.snowlib.component.text;

import io.github.snow1026.snowlib.component.text.resolver.TranslationProvider;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SnowLib 텍스트 프레임워크의 전역 설정(Singleton)입니다.
 * 공통 접두사(Prefix)나 번역 시스템을 등록할 때 사용합니다.
 */
public final class TextConfig {
    private static final TextConfig INSTANCE = new TextConfig();
    private final List<TagResolver> globalResolvers = Collections.synchronizedList(new ArrayList<>());
    private TranslationProvider translationProvider = key -> key;

    private TextConfig() {}

    public static TextConfig get() { return INSTANCE; }

    /**
     * 모든 메시지에 적용될 전역 태그를 추가합니다.
     * 예: {@code <prefix>}
     *
     * @param resolver 추가할 태그 리졸버
     * @return 체이닝을 위한 this
     */
    public TextConfig addGlobalTag(@NotNull TagResolver resolver) {
        this.globalResolvers.add(resolver);
        return this;
    }

    /**
     * 다국어 처리를 위한 공급자를 설정합니다.
     *
     * @param provider 번역 공급자 인터페이스 구현체
     * @return 체이닝을 위한 this
     */
    public TextConfig setTranslationProvider(@NotNull TranslationProvider provider) {
        this.translationProvider = provider;
        return this;
    }

    public List<TagResolver> getGlobalResolvers() {
        synchronized (globalResolvers) {
            return new ArrayList<>(globalResolvers);
        }
    }

    public TranslationProvider getTranslationProvider() {
        return translationProvider;
    }
}
