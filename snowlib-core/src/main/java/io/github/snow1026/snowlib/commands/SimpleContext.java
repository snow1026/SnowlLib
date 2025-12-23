package io.github.snow1026.snowlib.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@link SommandContext}의 간단하고 불변적인(immutable) 구현체.
 * <p>
 * 이 클래스는 다음 정보를 보유합니다:
 * <ul>
 * <li>명령어 발신자 (CommandSender)</li>
 * <li>사용된 명령어 레이블 (label)</li>
 * <li>가공되지 않은 문자열 인자 (raw arguments)</li>
 * <li>파싱된 인자 이름과 값의 맵 (대소문자 구분 없음)</li>
 * </ul>
 */
public final class SimpleContext implements SommandContext {
    private final CommandSender sender;
    private final String label;
    private final String[] rawArgs;
    private final Map<String, Object> parsedArguments;

    /**
     * 새로운 {@link SimpleContext}를 생성합니다.
     *
     * @param sender  명령어를 실행한 발신자.
     * @param label   사용된 명령어 레이블.
     * @param rawArgs 가공되지 않은 명령어 인자.
     */
    public SimpleContext(CommandSender sender, String label, String[] rawArgs) {
        this.sender = sender;
        this.label = label;
        this.rawArgs = rawArgs;
        this.parsedArguments = new HashMap<>();
    }

    /**
     * 파싱된 인자를 컨텍스트에 추가합니다.
     * <p>
     * 인자 이름은 대소문자를 구분하지 않고 접근할 수 있도록 소문자로 저장됩니다.
     *
     * @param name  인자의 이름.
     * @param value 인자의 값.
     */
    public void addArgument(String name, @Nullable Object value) {
        this.parsedArguments.put(name.toLowerCase(Locale.ROOT), value);
    }

    /**
     * 명령어를 실행한 발신자를 가져옵니다.
     *
     * @return 명령어 발신자.
     */
    @Override
    @NotNull
    public CommandSender sender() {
        return sender;
    }

    /**
     * 사용된 명령어 레이블(별칭 포함)을 가져옵니다.
     *
     * @return 명령어 레이블.
     */
    @Override
    @NotNull
    public String label() {
        return label;
    }

    /**
     * 명령어에 전달된 가공되지 않은 인자들을 가져옵니다.
     *
     * @return 문자열 배열 형태의 가공되지 않은 인자.
     */
    @Override
    @NotNull
    public String[] rawArgs() {
        return rawArgs;
    }

    /**
     * 이름으로 파싱된 인자의 값을 가져옵니다.
     * <p>
     * 인자가 존재하지 않으면 예외가 발생합니다.
     *
     * @param name 인자의 이름.
     * @param <T>  인자의 예상 타입.
     * @return 인자의 값.
     * @throws IllegalArgumentException 인자가 발견되지 않은 경우.
     */
    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> T getArgument(@NotNull String name) {
        // Optional arguments can be null, we must check containment first
        if (!parsedArguments.containsKey(name.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("No argument found with name: " + name);
        }
        Object value = parsedArguments.get(name.toLowerCase(Locale.ROOT));
        if (value == null) {
            // 이 경우는 Optional Argument이지만 값이 제공되지 않은 경우
            throw new IllegalArgumentException("Required argument was not provided, but context was accessed without checking for null: " + name);
        }
        return (T) value;
    }

    /**
     * 이름으로 파싱된 인자의 값을 가져오며, 인자가 존재하지 않거나 파싱되지 않았으면 기본값을 반환합니다.
     *
     * @param name         인자의 이름.
     * @param defaultValue 인자가 발견되지 않거나 null인 경우 반환할 값.
     * @param <T>          인자의 예상 타입.
     * @return 인자 값, 또는 기본값.
     */
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getArgument(@NotNull String name, @Nullable T defaultValue) {
        return (T) parsedArguments.getOrDefault(name.toLowerCase(Locale.ROOT), defaultValue);
    }
}
