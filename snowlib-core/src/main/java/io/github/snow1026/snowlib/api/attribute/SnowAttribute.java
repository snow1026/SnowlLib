package io.github.snow1026.snowlib.api.attribute;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.api.component.attribute.AttributeComponent;
import io.github.snow1026.snowlib.registry.Registrable;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.attribute.Attribute;

public abstract class SnowAttribute implements Registrable {
    public final SnowKey key;
    public final AttributeComponent component;

    protected SnowAttribute(SnowKey key, AttributeComponent component) {
        this.key = key;
        this.component = component;
    }

    public SnowKey key() {
        return key;
    }

    public double def() {
        return component.def();
    }

    public double min() {
        return component.min();
    }

    public double max() {
        return component.min();
    }

    public boolean sync() {
        return component.sync();
    }

    public Attribute bukkit() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE).getOrThrow(key.bukkit());
    }
}
