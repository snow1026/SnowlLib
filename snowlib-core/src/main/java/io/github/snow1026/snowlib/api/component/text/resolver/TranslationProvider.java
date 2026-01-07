package io.github.snow1026.snowlib.api.component.text.resolver;

import org.jetbrains.annotations.NotNull;

/**
 * 키(Key)를 기반으로 번역된 문자열을 제공하는 함수형 인터페이스입니다.
 * YAML 파일이나 데이터베이스 등 외부 소스와 연동할 때 구현합니다.
 */
@FunctionalInterface
public interface TranslationProvider {
    /**
     * 키에 해당하는 메시지를 반환합니다.
     *
     * @param key 식별 키
     * @return 번역된 문자열 (없을 경우 키 자체 반환 권장)
     */
    @NotNull String translate(@NotNull String key);
}
