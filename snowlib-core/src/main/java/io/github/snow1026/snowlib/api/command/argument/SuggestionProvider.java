package io.github.snow1026.snowlib.api.command.argument;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 동적인 탭 자동 완성 제안을 제공하기 위한 함수형 인터페이스.
 */
@FunctionalInterface
public interface SuggestionProvider {

    /**
     * 현재 발신자(sender)와 입력된 인자를 기반으로 제안 목록을 가져옵니다.
     *
     * @param sender      제안을 요청한 명령어 발신자.
     * @param currentInput 현재까지 입력된 부분 인자.
     * @return 제안된 완성 목록.
     */
    @NotNull
    List<String> getSuggestions(@NotNull CommandSender sender, @NotNull String currentInput);
}
