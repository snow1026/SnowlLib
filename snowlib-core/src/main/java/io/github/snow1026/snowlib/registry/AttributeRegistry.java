package io.github.snow1026.snowlib.registry;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.attribute.SnowAttribute;
import io.github.snow1026.snowlib.util.reflect.enchants.AttributeRegister;

import java.util.HashMap;
import java.util.Map;

/**
 * 서버에 생성된 커스텀  어트리뷰트들을 등록하고 조회하는 중앙 레지스트리 관리 클래스입니다.
 */
public class AttributeRegistry {
    private static final Map<SnowKey, SnowAttribute> registeredAttributes = new HashMap<>();

    /**
     * 고유 키를 사용하여 등록된 어트리뷰트를 가져옵니다.
     * @param key 조회할 키
     * @return 해당되는 {@link SnowAttribute}, 없으면 null
     */
    public static SnowAttribute getByKey(SnowKey key) {
        return registeredAttributes.get(key);
    }

    /**
     * 커스텀 어트리뷰트를 시스템 및 서버 레지스트리에 등록합니다.
     * @param target 등록할 어트리뷰트 객체
     */
    public static void register(SnowAttribute target) {
        if (target == null || target.key() == null) return;
        AttributeRegister.register(target);
        registeredAttributes.put(target.key(), target);
    }

    /**
     * 등록된 어트리뷰트를 해제합니다.
     * @param target 해제할 어트리뷰트 객체
     */
    public static void unregister(SnowAttribute target) {
        if (target == null || target.key() == null) return;
        AttributeRegister.unregister(target.key());
        registeredAttributes.remove(target.key());
    }

    /**
     * 어트리뷰트가 현재 등록되어 있는지 확인합니다.
     * @param target 확인할 어트리뷰트 객체
     * @return 등록 여부
     */
    public static boolean isRegistered(SnowAttribute target) {
        if (target == null || target.key() == null) return false;
        return AttributeRegister.isRegistered(target.key());
    }

    /** @return 현재 등록된 모든 어트리뷰트의 복사본 맵 */
    public Map<SnowKey, SnowAttribute> getRegisteredAttributes() {
        return Map.copyOf(registeredAttributes);
    }
}
