package io.github.snow1026.snowlib.registry.internal;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.api.attribute.SnowAttribute;
import io.github.snow1026.snowlib.internal.attribute.AttributeRegister;
import io.github.snow1026.snowlib.registry.MappedRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 서버에 생성된 커스텀 어트리뷰트들을 등록하고 조회하는 레지스트리입니다.
 */
public final class AttributeRegistry implements MappedRegistry<SnowAttribute> {
    private final Map<SnowKey, SnowAttribute> registeredAttributes = new ConcurrentHashMap<>();

    private AttributeRegistry() {}

    @Override
    public void register(SnowKey key, SnowAttribute target) {
        if (target == null) return;
        AttributeRegister.register(target);
        registeredAttributes.put(key, target);
    }

    @Override
    public void unregister(SnowKey key) {
        if (key == null) return;
        AttributeRegister.unregister(key);
        registeredAttributes.remove(key);
    }

    @Override
    public SnowAttribute get(SnowKey key) {
        return registeredAttributes.get(key);
    }

    @Override
    public Collection<SnowAttribute> getAll() {
        return Collections.unmodifiableCollection(registeredAttributes.values());
    }

    @Override
    public Map<SnowKey, SnowAttribute> getEntries() {
        return Collections.unmodifiableMap(registeredAttributes);
    }
}
