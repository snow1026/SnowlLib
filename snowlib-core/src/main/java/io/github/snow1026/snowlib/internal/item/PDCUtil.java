package io.github.snow1026.snowlib.internal.item;

import io.github.snow1026.snowlib.SnowLibrary;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public final class PDCUtil {
    private PDCUtil() {}

    public static void apply(ItemMeta meta, Map<String, Object> data) {
        if (data.isEmpty()) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            NamespacedKey key = new NamespacedKey(SnowLibrary.snowlibrary(), entry.getKey());
            Object value = entry.getValue();

            if (value instanceof Integer i) {
                container.set(key, PersistentDataType.INTEGER, i);
            } else if (value instanceof String s) {
                container.set(key, PersistentDataType.STRING, s);
            } else if (value instanceof Long l) {
                container.set(key, PersistentDataType.LONG, l);
            } else if (value instanceof Double d) {
                container.set(key, PersistentDataType.DOUBLE, d);
            } else if (value instanceof Float f) {
                container.set(key, PersistentDataType.FLOAT, f);
            } else if (value instanceof Boolean b) {
                container.set(key, PersistentDataType.BYTE, (byte) (b ? 1 : 0));
            }
        }
    }
}
