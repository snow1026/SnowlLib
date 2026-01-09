package io.github.snow1026.snowlib.registry.internal;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.api.enchantment.SnowEnchantment;
import io.github.snow1026.snowlib.internal.enchant.EnchantmentRegister;
import io.github.snow1026.snowlib.registry.SnowRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 서버에 생성된 커스텀 인챈트들을 등록하고 조회하는 레지스트리입니다.
 */
public final class EnchantmentRegistry implements SnowRegistry<SnowEnchantment> {
    private final Map<SnowKey, SnowEnchantment> registeredEnchants = new ConcurrentHashMap<>();

    private EnchantmentRegistry() {}

    @Override
    public void register(SnowKey key, SnowEnchantment target) {
        if (target == null) return;
        EnchantmentRegister.register(target);
        registeredEnchants.put(key, target);
    }

    @Override
    public void unregister(SnowKey key) {
        if (key == null) return;
        EnchantmentRegister.unregister(key);
        registeredEnchants.remove(key);
    }

    @Override
    public SnowEnchantment get(SnowKey key) {
        return registeredEnchants.get(key);
    }

    @Override
    public Collection<SnowEnchantment> getAll() {
        return Collections.unmodifiableCollection(registeredEnchants.values());
    }

    @Override
    public Map<SnowKey, SnowEnchantment> getEntries() {
        return Collections.unmodifiableMap(registeredEnchants);
    }
}
