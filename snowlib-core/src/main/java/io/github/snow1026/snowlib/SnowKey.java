package io.github.snow1026.snowlib;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 마인크래프트의 NamespacedKey를 간편하게 다루기 위한 불변(Immutable) 레코드(Record)입니다.
 * <p>
 * Bukkit의 {@link NamespacedKey}와 유사하지만, 플러그인 객체나 문자열을 통해
 * 더 직관적으로 키를 생성하고 관리할 수 있습니다.
 * </p>
 *
 * @param root 네임스페이스 (보통 플러그인 이름 또는 'minecraft')
 * @param path 키의 고유 식별자 이름
 */
public record SnowKey(String root, String path) {

    /**
     * 플러그인 인스턴스를 사용하여 SnowKey를 생성합니다.
     * <p>
     * 플러그인의 이름을 소문자로 변환하여 네임스페이스(root)로 사용합니다.
     * </p>
     *
     * @param plugin 네임스페이스로 사용할 플러그인 객체
     * @param path   키 이름
     */
    public SnowKey(Plugin plugin, String path) {
        this(plugin.getName().toLowerCase(), path);
    }

    /**
     * 'minecraft' 네임스페이스를 사용하는 키를 생성하는 팩토리 메서드입니다.
     *
     * @param path 키 이름 (예: "diamond_sword")
     * @return minecraft 네임스페이스를 가진 SnowKey (minecraft:path)
     */
    public static SnowKey minecraft(String path) {
        return new SnowKey("minecraft", path);
    }

    /**
     * 전체 키 문자열을 반환합니다.
     *
     * @return "root:path" 형식의 문자열 (예: "myplugin:custom_item")
     */
    public String getKey() {
        return root() + ":" + path();
    }

    /**
     * 이 키를 Bukkit API에서 사용하는 {@link NamespacedKey}로 변환합니다.
     *
     * @return 변환된 NamespacedKey 객체
     */
    public NamespacedKey bukkit() {
        return new NamespacedKey(root(), path());
    }

    /**
     * 다른 객체와 동등성을 비교합니다.
     * <p>
     * 키의 문자열 표현(root:path)이 같으면 같은 객체로 간주합니다.
     * </p>
     *
     * @param o 비교할 객체
     * @return 두 객체가 같으면 true
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SnowKey other)) return false;
        return Objects.equals(getKey(), other.getKey());
    }

    /**
     * 객체의 해시 코드를 생성합니다.
     * <p>
     * 키 문자열을 기반으로 생성됩니다.
     * </p>
     *
     * @return 해시 코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    /**
     * 객체의 문자열 표현을 반환합니다.
     *
     * @return "root:path" 형식의 문자열
     */
    @Override
    public @NotNull String toString() {
        return getKey();
    }
}
