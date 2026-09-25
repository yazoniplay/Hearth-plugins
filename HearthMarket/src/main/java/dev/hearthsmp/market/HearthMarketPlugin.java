package dev.hearthsmp.market;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class HearthMarketPlugin extends JavaPlugin implements Listener {
    private static final String TITLE = ChatColor.DARK_GRAY + "🔥 Hearth Market";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("HearthMarket is open.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        openMarket(player);
        return true;
    }

    private void openMarket(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, TITLE);
        inventory.setItem(11, item(Material.EMERALD, ChatColor.GREEN + "Player Listings", "Player-to-player listings are coming next."));
        inventory.setItem(13, item(Material.GOLD_INGOT, ChatColor.GOLD + "Sell Items", "Create a listing from your held item."));
        inventory.setItem(15, item(Material.BOOK, ChatColor.YELLOW + "Market Info", "A safe trading market for Hearth."));
        player.openInventory(inventory);
    }

    private ItemStack item(Material material, String name, String lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(ChatColor.GRAY + lore));
        stack.setItemMeta(meta);
        return stack;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (TITLE.equals(event.getView().getTitle())) {
            event.setCancelled(true);
        }
    }
}
