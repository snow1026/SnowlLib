package io.github.snow1026.snowlib;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record SnowKey(String root, String path) {

    public SnowKey(Plugin plugin, String path) {
        this(plugin.getName(), path);
    }

    public static SnowKey minecraft(String path) {
        return new SnowKey("minecraft", path);
    }

    public String getKey() {
        return root() + ":" + path();
    }

    public NamespacedKey bukkit() {
        return new NamespacedKey(root(), path());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SnowKey other)) return false;
        return Objects.equals(getKey(), other.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    @Override
    public @NotNull String toString() {
        return getKey();
    }
}
