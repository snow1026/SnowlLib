package io.github.snow1026.snowlib.command;

import io.github.snow1026.snowlib.registry.CommandRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * SnowLib 커맨드 시스템의 최상위 제어 클래스입니다.
 * <p>
 * 이 클래스는 명령어를 정의하고 최종적으로 NMS(Brigadier)에 등록하는 역할을 수행합니다.
 * 기존의 정적 register 방식 대신 생성자 또는 {@link #create(String)}를 통해 인스턴스를 생성합니다.
 */
public class Sommand {

    private final String name;
    private final SommandNode root;

    /**
     * 새로운 최상위 명령어를 생성합니다.
     *
     * @param name 명령어의 기본 이름.
     */
    public Sommand(@NotNull String name) {
        this.name = name;
        this.root = new SommandNode(name, String.class, true);
    }

    /**
     * 새로운 최상위 명령어를 생성하기 위한 정적 팩토리 메서드입니다.
     *
     * @param name 명령어의 기본 이름.
     * @return 생성된 Sommand 인스턴스.
     */
    public static SommandNode create(@NotNull String name) {
        return new Sommand(name).root();
    }

    /**
     * 명령어 구성을 위한 루트 노드를 반환합니다.
     *
     * @return 유창한 API 구성을 위한 {@link SommandNode}.
     */
    public SommandNode root() {
        return root;
    }

    /**
     * 구성된 명령어 트리를 NMS 디스패처에 최종적으로 등록합니다.
     */
    public void register() {
        CommandRegistry.register(this);
    }

    /**
     * 명령어의 이름을 가져옵니다.
     *
     * @return 명령어 이름.
     */
    @NotNull
    public String getName() {
        return name;
    }
}
