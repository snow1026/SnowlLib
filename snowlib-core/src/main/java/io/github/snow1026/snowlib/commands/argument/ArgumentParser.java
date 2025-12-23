package io.github.snow1026.snowlib.commands.argument;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 문자열 인자를 특정 타입으로 파싱하기 위한 함수형 인터페이스.
 * <p>
 * 파싱에 실패하면 {@code null}을 반환해야 하며, 이 경우 프레임워크가 적절한 오류를 처리합니다.
 *
 * @param <T> 파싱할 대상 타입.
 */
@FunctionalInterface
public interface ArgumentParser<T> {

    /**
     * 문자열 입력을 대상 타입 {@code T}로 파싱합니다.
     *
     * @param sender 명령어를 실행한 발신자.
     * @param input  가공되지 않은 문자열 인자.
     * @return 파싱된 값. 파싱에 실패하면 {@code null}을 반환합니다.
     */
    @Nullable
    T parse(@NotNull CommandSender sender, @NotNull String input);

    /**
     * 부분 인자에 대한 탭 자동 완성(Tab-completion) 제안을 제공합니다.
     *
     * @param sender 제안을 요청한 발신자.
     * @param input  현재까지 입력된 부분 문자열 인자.
     * @return 가능한 완성 목록.
     */
    @NotNull
    default List<String> suggest(@NotNull CommandSender sender, @NotNull String input) {
        return Collections.emptyList();
    }

    /**
     * 이 파서가 파싱에 실패했을 때 사용자에게 보여줄 기본 오류 메시지를 반환합니다.
     * <p>
     * 예를 들어, Integer 파서의 경우 "정수"를 반환할 수 있습니다.
     *
     * @return 예상되는 타입/값에 대한 설명 문자열.
     */
    @NotNull
    default String getErrorMessage() {
        return "유효하지 않은 입력";
    }
}
