package io.github.snow1026.snowlib.registry;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.SnowRegistry;
import io.github.snow1026.snowlib.enchantment.SnowEnchantment;
import io.github.snow1026.snowlib.util.reflect.enchants.EnchantmentRegister;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentRegistry extends SnowRegistry<SnowEnchantment> {
    private final Map<SnowKey, SnowEnchantment> registeredEnchants = new HashMap<>();

    @Override
    public SnowEnchantment getByKey(SnowKey key) {
        return registeredEnchants.get(key);
    }

    @Override
    public void register(SnowEnchantment target) {
        if (target == null || target.key() == null) return;
        EnchantmentRegister.register(target);
        registeredEnchants.put(target.key(), target);
    }

    @Override
    public void unRegister(SnowEnchantment target) {
        if (target == null || target.key() == null) return;
        EnchantmentRegister.register(target);
        registeredEnchants.remove(target.key());
    }

    @Override
    public boolean isRegistered(SnowEnchantment target) {
        if (target == null || target.key() == null) return false;
        return EnchantmentRegister.isRegistered(target.key());
    }

    public Map<SnowKey, SnowEnchantment> getRegisteredEnchants() {
        return Map.copyOf(registeredEnchants);
    }
}
