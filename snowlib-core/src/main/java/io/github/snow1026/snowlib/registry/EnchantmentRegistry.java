package io.github.snow1026.snowlib.registry;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.enchantment.SnowEnchantment;
import io.github.snow1026.snowlib.util.reflect.enchants.EnchantmentRegister;

import java.util.HashMap;
import java.util.Map;

/**
 * 서버에 생성된 커스텀 인챈트들을 등록하고 조회하는 중앙 레지스트리 관리 클래스입니다.
 */
public class EnchantmentRegistry {
    private static final Map<SnowKey, SnowEnchantment> registeredEnchants = new HashMap<>();

    /**
     * 고유 키를 사용하여 등록된 인챈트를 가져옵니다.
     * @param key 조회할 키
     * @return 해당되는 {@link SnowEnchantment}, 없으면 null
     */
    public static SnowEnchantment getByKey(SnowKey key) {
        return registeredEnchants.get(key);
    }

    /**
     * 커스텀 인챈트를 시스템 및 서버 레지스트리에 등록합니다.
     * @param target 등록할 인챈트 객체
     */
    public static void register(SnowEnchantment target) {
        if (target == null || target.key() == null) return;
        EnchantmentRegister.register(target);
        registeredEnchants.put(target.key(), target);
    }

    /**
     * 등록된 인챈트를 해제합니다.
     * @param target 해제할 인챈트 객체
     */
    public static void unregister(SnowEnchantment target) {
        if (target == null || target.key() == null) return;
        EnchantmentRegister.unregister(target.key());
        registeredEnchants.remove(target.key());
    }

    /**
     * 인챈트가 현재 등록되어 있는지 확인합니다.
     * @param target 확인할 인챈트 객체
     * @return 등록 여부
     */
    public static boolean isRegistered(SnowEnchantment target) {
        if (target == null || target.key() == null) return false;
        return EnchantmentRegister.isRegistered(target.key());
    }

    /** @return 현재 등록된 모든 인챈트의 복사본 맵 */
    public Map<SnowKey, SnowEnchantment> getRegisteredEnchants() {
        return Map.copyOf(registeredEnchants);
    }
}
