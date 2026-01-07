package io.github.snow1026.snowlib.api.command;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.SnowLibrary;
import io.github.snow1026.snowlib.api.command.argument.SuggestionProvider;
import io.github.snow1026.snowlib.internal.command.SnowSommand;
import io.github.snow1026.snowlib.internal.command.CommandRegister;
import io.github.snow1026.snowlib.registry.MappedRegistry;
import io.github.snow1026.snowlib.registry.Registrable;
import io.github.snow1026.snowlib.registry.RegistryKey;
import io.github.snow1026.snowlib.registry.internal.CommandRegistry;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * SnowLib 커맨드 시스템의 최상위 제어 클래스입니다.
 * <p>
 * 이 클래스는 명령어를 정의하고 최종적으로 NMS(Brigadier)에 등록하는 역할을 수행합니다.
 * 기존의 정적 register 방식 대신 생성자 또는 {@link #create(String)}를 통해 인스턴스를 생성합니다.
 */
public interface Sommand extends Registrable {

    /**
     * 새로운 최상위 명령어를 생성하기 위한 정적 팩토리 메서드입니다.
     *
     * @param name 명령어의 기본 이름.
     * @return 생성된 Sommand 인스턴스.
     */
    static Sommand create(@NotNull String name) {
        return new SnowSommand(name);
    }


    /**
     * 구성된 명령어 트리를 NMS 디스패처에 최종적으로 등록합니다.
     */
    default void register() {
        MappedRegistry<Sommand> registry = SnowLibrary.registryAccess().lookup(RegistryKey.COMMAND);
        registry.register(new SnowKey(SnowLibrary.snowlibrary(), this.getName()), this);
    }

    /**
     * 이 노드에 리터럴 서브 명령어를 추가합니다.
     *
     * @param name    리터럴 값.
     * @param sommand 노드 구성 빌더.
     * @return 현재 노드 인스턴스.
     */
    Sommand sub(@NotNull String name, @NotNull Consumer<Sommand> sommand);

    Sommand sub(@NotNull String name);

    /**
     * 이 노드에 필수 인자를 추가합니다.
     */
    <T> Sommand sub(@NotNull String name, @NotNull Class<T> type, @NotNull Consumer<Sommand> sommand);

    <T> Sommand sub(@NotNull String name, @NotNull Class<T> type);

    <T> Sommand subOptional(@NotNull String name, @NotNull Class<T> type, @NotNull Consumer<Sommand> sommand);

    <T> Sommand subOptional(@NotNull String name, @NotNull Class<T> type);

    Sommand alias(@NotNull String... aliases);

    Sommand description(@NotNull String description);

    Sommand usage(@NotNull String usage);

    Sommand executes(@NotNull Consumer<SommandContext> executor);

    Sommand requires(@NotNull String permission);

    Sommand requires(@NotNull Predicate<CommandSender> requirement, @NotNull String failMessage);

    Sommand playerOnly();

    void suggests(@NotNull SuggestionProvider provider);

    Map<String, SnowSommand> getChildren();

    /**
     * 명령어의 이름을 가져옵니다.
     *
     * @return 명령어 이름.
     */
    @NotNull
    String getName();

    String getPermission();

    List<String> getAliases();

    String getDescription();
}
