package io.github.snow1026.snowlib.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 명령어 실행을 위한 컨텍스트(Context)를 제공하는 인터페이스.
 * <p>
 * 이 인터페이스는 명령어 발신자, 원본 인자, 그리고 이름으로 접근 가능한
 * 타입 안전한 파싱된 인자들에 대한 접근을 제공합니다.
 */
public interface SommandContext {

    /**
     * 명령어를 실행한 {@link CommandSender}를 가져옵니다.
     *
     * @return 명령어 발신자.
     */
    @NotNull
    CommandSender sender();

    /**
     * 발신자를 {@link Player}로 가져오는 편의 메서드.
     * <p>
     * 발신자가 플레이어가 아니면 {@code null}을 반환합니다.
     *
     * @return 플레이어 형태의 발신자, 또는 {@code null}.
     */
    @Nullable
    default Player getPlayer() {
        return sender() instanceof Player ? (Player) sender() : null;
    }

    /**
     * 명령어를 실행하는 데 사용된 별칭 또는 명령어 이름을 가져옵니다.
     *
     * @return 명령어 레이블.
     */
    @NotNull
    String label();

    /**
     * 가공되지 않은(raw), 파싱되지 않은 문자열 인자들을 가져옵니다.
     *
     * @return 원본 인자 배열.
     */
    @NotNull
    String[] rawArgs();

    /**
     * 이름으로 파싱된 인자를 가져옵니다.
     * <p>
     * **주의:** 선택적 인자의 경우 {@link #getArgument(String, Object)}를 사용하여 {@code null} 체크를 하는 것이 안전합니다.
     * 이 메서드는 인자가 반드시 존재하고 {@code null}이 아니라고 가정합니다.
     *
     * @param name {@link SommandNode}에 정의된 인자의 이름.
     * @param <T>  인자의 예상 타입.
     * @return 파싱된 인자 값.
     * @throws IllegalArgumentException 주어진 이름의 인자가 존재하지 않거나, 값이 {@code null}인 경우.
     */
    @NotNull
    <T> T getArgument(@NotNull String name);

    /**
     * 이름으로 파싱된 인자를 가져오며, 인자가 존재하지 않거나 {@code null}이면 기본값을 반환합니다.
     *
     * @param name         인자의 이름.
     * @param defaultValue 인자가 존재하지 않거나 파싱되지 않아 {@code null}인 경우 반환할 값.
     * @param <T>          인자의 예상 타입.
     * @return 파싱된 인자 값 또는 기본값.
     */
    @Nullable
    <T> T getArgument(@NotNull String name, @Nullable T defaultValue);
}
