package dev.hearthsmp.market;

import dev.hearthsmp.economy.EconomyService;
import dev.hearthsmp.economy.HearthEconomyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class HearthMarketPlugin extends JavaPlugin implements Listener {
    private static final String TITLE = ChatColor.DARK_GRAY + "🔥 Hearth Market";
    private int nextId = 1;
    private EconomyService economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        HearthEconomyPlugin economyPlugin = (HearthEconomyPlugin) Bukkit.getPluginManager().getPlugin("HearthEconomy");
        if (economyPlugin == null || !economyPlugin.isEnabled()) {
            getLogger().severe("HearthEconomy is required. Disabling HearthMarket.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economy = economyPlugin.getEconomyService();
        ConfigurationSection listings = getConfig().getConfigurationSection("listings");
        if (listings != null) {
            for (String key : listings.getKeys(false)) {
                try { nextId = Math.max(nextId, Integer.parseInt(key) + 1); } catch (NumberFormatException ignored) { }
            }
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("HearthMarket is open.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0) {
            openMarket(player);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "sell" -> sell(player, args);
            case "buy" -> buy(player, args);
            case "cancel" -> cancel(player, args);
            default -> player.sendMessage(ChatColor.GRAY + "Usage: /market [sell <price>|buy <id>|cancel <id>]");
        }
        return true;
    }

    private void sell(Player player, String[] args) {
        if (args.length != 2) { player.sendMessage(ChatColor.GRAY + "Usage: /market sell <price>"); return; }
        double price;
        try { price = Double.parseDouble(args[1]); } catch (NumberFormatException ex) { player.sendMessage(ChatColor.RED + "Enter a valid price."); return; }
        if (!Double.isFinite(price) || price <= 0) { player.sendMessage(ChatColor.RED + "Price must be positive."); return; }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR || hand.getAmount() <= 0) { player.sendMessage(ChatColor.RED + "Hold an item in your main hand."); return; }
        int id = nextId++;
        String path = "listings." + id;
        getConfig().set(path + ".seller", player.getUniqueId().toString());
        getConfig().set(path + ".seller-name", player.getName());
        getConfig().set(path + ".price", price);
        getConfig().set(path + ".item", hand.clone());
        getConfig().set(path + ".created", System.currentTimeMillis());
        saveConfig();
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(ChatColor.GREEN + "Listed item #" + id + " for " + economy.format(price) + ".");
    }

    private void buy(Player player, String[] args) {
        if (args.length != 2) { player.sendMessage(ChatColor.GRAY + "Usage: /market buy <id>"); return; }
        String path = "listings." + args[1];
        if (!getConfig().isConfigurationSection(path)) { player.sendMessage(ChatColor.RED + "That listing does not exist."); return; }
        UUID seller;
        try { seller = UUID.fromString(getConfig().getString(path + ".seller", "")); } catch (IllegalArgumentException ex) { player.sendMessage(ChatColor.RED + "This listing is invalid."); return; }
        if (seller.equals(player.getUniqueId())) { player.sendMessage(ChatColor.RED + "You cannot buy your own listing."); return; }
        double price = getConfig().getDouble(path + ".price");
        ItemStack item = getConfig().getItemStack(path + ".item");
        if (item == null || item.getType() == Material.AIR) { player.sendMessage(ChatColor.RED + "This listing has an invalid item."); return; }
        if (player.getInventory().firstEmpty() == -1) { player.sendMessage(ChatColor.RED + "Make room in your inventory first."); return; }
        if (!economy.withdraw(player.getUniqueId(), price)) { player.sendMessage(ChatColor.RED + "You do not have enough money."); return; }
        economy.deposit(seller, price);
        player.getInventory().addItem(item);
        getConfig().set(path, null);
        saveConfig();
        player.sendMessage(ChatColor.GREEN + "Purchased listing #" + args[1] + " for " + economy.format(price) + ".");
        Player sellerPlayer = Bukkit.getPlayer(seller);
        if (sellerPlayer != null) sellerPlayer.sendMessage(ChatColor.GREEN + player.getName() + " bought your listing #" + args[1] + ".");
    }

    private void cancel(Player player, String[] args) {
        if (args.length != 2) { player.sendMessage(ChatColor.GRAY + "Usage: /market cancel <id>"); return; }
        String path = "listings." + args[1];
        if (!getConfig().isConfigurationSection(path)) { player.sendMessage(ChatColor.RED + "That listing does not exist."); return; }
        if (!player.getUniqueId().toString().equals(getConfig().getString(path + ".seller"))) { player.sendMessage(ChatColor.RED + "You do not own this listing."); return; }
        ItemStack item = getConfig().getItemStack(path + ".item");
        if (item != null) player.getInventory().addItem(item);
        getConfig().set(path, null);
        saveConfig();
        player.sendMessage(ChatColor.YELLOW + "Listing #" + args[1] + " cancelled.");
    }

    private void openMarket(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, TITLE);
        ConfigurationSection listings = getConfig().getConfigurationSection("listings");
        int slot = 0;
        if (listings != null) {
            for (String id : listings.getKeys(false)) {
                if (slot >= 45) break;
                ItemStack item = listings.getItemStack(id + ".item");
                if (item == null) continue;
                ItemStack display = item.clone();
                ItemMeta meta = display.getItemMeta();
                List<String> lore = meta != null && meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(ChatColor.GRAY + "Listing: #" + id);
                lore.add(ChatColor.GRAY + "Seller: " + listings.getString(id + ".seller-name", "Unknown"));
                lore.add(ChatColor.GOLD + "Price: " + economy.format(listings.getDouble(id + ".price")));
                lore.add(ChatColor.YELLOW + "/market buy " + id);
                if (meta != null) { meta.setLore(lore); display.setItemMeta(meta); }
                inventory.setItem(slot++, display);
            }
        }
        inventory.setItem(49, item(Material.GOLD_INGOT, ChatColor.GOLD + "Sell Held Item", "Use /market sell <price>"));
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
        if (TITLE.equals(event.getView().getTitle())) event.setCancelled(true);
    }
}
