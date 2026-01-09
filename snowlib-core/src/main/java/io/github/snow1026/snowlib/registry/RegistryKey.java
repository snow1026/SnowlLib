package io.github.snow1026.snowlib.registry;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.SnowLibrary;

/**
 * 특정 레지스트리를 식별하고, 해당 레지스트리가 담고 있는 타입을 정의하는 키입니다.
 */
public record RegistryKey(SnowKey key) {
    public static final RegistryKey ATTRIBUTE = new RegistryKey("attribute");
    public static final RegistryKey COMMAND = new RegistryKey("command");
    public static final RegistryKey ENCHANTMENT = new RegistryKey("enchantment");

    private RegistryKey(String path) {
        this(new SnowKey(SnowLibrary.snowlibrary().getName(), path));
    }

    /**
     * 이 레지스트리 키의 식별자를 반환합니다.
     *
     * @return SnowKey 식별자
     */
    @Override
    public SnowKey key() {
        return this.key;
    }

    /**
     * 새로운 레지스트리 키를 생성합니다.
     *
     * @param key 식별자
     * @param <E> 레지스트리 타입
     * @return 생성된 RegistryKey
     */
    public static <E extends Registrable> RegistryKey create(SnowKey key) {
        return new RegistryKey(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistryKey(SnowKey key1))) return false;
        return key.equals(key1);
    }
}
