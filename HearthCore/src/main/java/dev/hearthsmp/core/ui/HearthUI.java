package dev.hearthsmp.core.ui;

import dev.hearthsmp.core.HearthCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class HearthUI {
    public static final TextColor HEARTH = TextColor.color(0xE58A4E);
    public static final TextColor CREAM = TextColor.color(0xF2E2C4);
    public static final TextColor BROWN = TextColor.color(0x6E4B35);
    public static final TextColor CHARCOAL = TextColor.color(0x211B17);
    public static final TextColor MUTED = TextColor.color(0x9A8978);

    private final HearthCorePlugin plugin;

    public HearthUI(HearthCorePlugin plugin) {
        this.plugin = plugin;
    }

    public Component title(String text) {
        return Component.text(text).color(CREAM);
    }

    public ItemStack item(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).color(HEARTH));
        item.setItemMeta(meta);
        return item;
    }
}
