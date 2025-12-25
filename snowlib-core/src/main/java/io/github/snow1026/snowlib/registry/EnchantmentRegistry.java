package io.github.snow1026.snowlib.registry;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.enchantment.SnowEnchantment;
import io.github.snow1026.snowlib.util.reflect.enchants.EnchantmentRegister;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentRegistry {
    private static final Map<SnowKey, SnowEnchantment> registeredEnchants = new HashMap<>();

    public static SnowEnchantment getByKey(SnowKey key) {
        return registeredEnchants.get(key);
    }

    public static void register(SnowEnchantment target) {
        if (target == null || target.key() == null) return;
        EnchantmentRegister.register(target);
        registeredEnchants.put(target.key(), target);
    }

    public static void unregister(SnowEnchantment target) {
        if (target == null || target.key() == null) return;
        EnchantmentRegister.unregister(target.key());
        registeredEnchants.remove(target.key());
    }

    public static boolean isRegistered(SnowEnchantment target) {
        if (target == null || target.key() == null) return false;
        return EnchantmentRegister.isRegistered(target.key());
    }

    public Map<SnowKey, SnowEnchantment> getRegisteredEnchants() {
        return Map.copyOf(registeredEnchants);
    }
}
