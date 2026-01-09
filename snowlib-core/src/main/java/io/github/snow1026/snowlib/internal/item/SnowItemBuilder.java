package io.github.snow1026.snowlib.internal.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.github.snow1026.snowlib.api.component.text.TextComponent;
import io.github.snow1026.snowlib.api.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class SnowItemBuilder implements ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    private TextComponent name;
    private final List<TextComponent> lore = new ArrayList<>();
    private final Map<String, Object> pdc = new HashMap<>();

    public SnowItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    public SnowItemBuilder(ItemStack itemStack) {
        this.item = itemStack.clone();
        this.meta = item.getItemMeta();
    }

    @Override
    public ItemBuilder name(@Nullable TextComponent name) {
        this.name = name;
        return this;
    }

    @Override
    public ItemBuilder name(@NotNull String text) {
        return name(TextComponent.of(text).mm());
    }

    @Override
    public ItemBuilder lore(@NotNull TextComponent... lines) {
        lore.clear();
        lore.addAll(Arrays.asList(lines));
        return this;
    }

    @Override
    public ItemBuilder lore(@NotNull List<TextComponent> lines) {
        lore.clear();
        lore.addAll(lines);
        return this;
    }

    @Override
    public ItemBuilder lore(@NotNull String... lines) {
        lore.clear();
        for (String line : lines) {
            lore.add(TextComponent.of(line).mm());
        }
        return this;
    }

    @Override
    public ItemBuilder addLore(@NotNull TextComponent line) {
        lore.add(line);
        return this;
    }

    @Override
    public ItemBuilder addLore(@NotNull String line) {
        return addLore(TextComponent.of(line).mm());
    }

    @Override
    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    @Override
    public ItemBuilder flag(@NotNull ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    @Override
    public ItemBuilder hideAllFlags() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    @Override
    public ItemBuilder unbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    @Override
    public ItemBuilder glow() {
        meta.setEnchantmentGlintOverride(true);
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemBuilder modelData(int data) {
        meta.setCustomModelData(data);
        return this;
    }

    @Override
    public ItemBuilder enchant(@NotNull Enchantment enchant, int level) {
        meta.addEnchant(enchant, level, true);
        return this;
    }

    @Override
    public ItemBuilder modify(@NotNull Consumer<ItemBuilder> consumer) {
        consumer.accept(this);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends ItemMeta> ItemBuilder editMeta(@NotNull Class<M> metaClass, @NotNull Consumer<M> consumer) {
        if (metaClass.isInstance(meta)) {
            consumer.accept((M) meta);
        }
        return this;
    }

    @Override
    public ItemBuilder damage(int damage) {
        return editMeta(Damageable.class, m -> m.setDamage(damage));
    }

    @Override
    public ItemBuilder armorColor(@NotNull Color color) {
        return editMeta(LeatherArmorMeta.class, m -> m.setColor(color));
    }

    @Override
    public ItemBuilder skullOwner(@NotNull OfflinePlayer player) {
        return editMeta(SkullMeta.class, m -> m.setOwningPlayer(player));
    }

    @Override
    public ItemBuilder skullTexture(@NotNull String base64) {
        return editMeta(SkullMeta.class, m -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", base64));
            m.setPlayerProfile(profile);
        });
    }

    @Override
    public ItemBuilder pdc(@NotNull String key, @NotNull Object value) {
        pdc.put(key, value);
        return this;
    }

    @Override
    public ItemStack build() {
        ItemMetaApplier.apply(meta, name, lore);
        PDCUtil.apply(meta, pdc);
        item.setItemMeta(meta);
        return item;
    }
}
