package io.github.snow1026.snowlib.config.parsers.primitive;

import io.github.snow1026.snowlib.config.parsers.ConfigParser;
import org.bukkit.Material;

public class MaterialParser implements ConfigParser<Material> {

    @Override
    public Class<Material> type() {
        return Material.class;
    }

    public Material parse(Object raw) {
        return Material.getMaterial(raw.toString());
    }
}
