package dev.hearthsmp.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class EconomyMenu {
    private EconomyMenu() {}

    public static void open(HearthEconomyPlugin plugin, Player player) {
        int rows = Math.max(1, Math.min(6, plugin.getConfig().getInt("gui.rows", 3)));
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.title", "&8🔥 Hearth Economy"));

        Inventory inventory = Bukkit.createInventory(null, rows * 9, title);

        ItemStack balance = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = balance.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Your Balance");
        meta.setLore(java.util.List.of(
                ChatColor.GRAY + plugin.getEconomyService().format(
                        plugin.getEconomyService().getBalance(player.getUniqueId())),
                "",
                ChatColor.DARK_GRAY + "Use /pay <player> <amount> to send money."
        ));
        balance.setItemMeta(meta);

        ItemStack info = new ItemStack(Material.EMERALD);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GREEN + "Economy");
        infoMeta.setLore(java.util.List.of(
                ChatColor.GRAY + "Currency: " + plugin.getConfig().getString("economy.currency-name", "Hearth Coins"),
                ChatColor.GRAY + "Your money is saved automatically."
        ));
        info.setItemMeta(infoMeta);

        inventory.setItem(11, balance);
        inventory.setItem(15, info);

        ItemStack filler = new ItemStack(Material.BROWN_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(filler);
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, filler);
        }

        player.openInventory(inventory);
    }
}
