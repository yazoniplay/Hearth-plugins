package dev.hearthsmp.essentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WarpCommand implements CommandExecutor, Listener {
    private static final String TITLE = ChatColor.DARK_GRAY + "🔥 Hearth Warps";
    private final HearthEssentialsPlugin plugin;
    private final Map<String, Location> warps = new LinkedHashMap<>();
    private final Map<Integer, String> slots = new HashMap<>();

    public WarpCommand(HearthEssentialsPlugin plugin) {
        this.plugin = plugin;
        load();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "warp" -> {
                if (args.length == 0) {
                    openMenu(player);
                } else {
                    teleport(player, args[0]);
                }
            }
            case "setwarp" -> setWarp(player, args);
            case "delwarp" -> deleteWarp(player, args);
        }
        return true;
    }

    private void setWarp(Player player, String[] args) {
        if (!player.hasPermission("hearth.essentials.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission.");
            return;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.GRAY + "Usage: /setwarp <name>");
            return;
        }

        String name = args[0].toLowerCase();
        if (!name.matches("[a-z0-9_-]{1,24}")) {
            player.sendMessage(ChatColor.RED + "Warp names may only use letters, numbers, _ and -.");
            return;
        }

        warps.put(name, player.getLocation().clone());
        save();
        player.sendMessage(ChatColor.GREEN + "Warp '" + name + "' set.");
    }

    private void deleteWarp(Player player, String[] args) {
        if (!player.hasPermission("hearth.essentials.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission.");
            return;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.GRAY + "Usage: /delwarp <name>");
            return;
        }

        if (warps.remove(args[0].toLowerCase()) == null) {
            player.sendMessage(ChatColor.RED + "Warp not found.");
            return;
        }

        save();
        player.sendMessage(ChatColor.YELLOW + "Warp '" + args[0] + "' deleted.");
    }

    private void teleport(Player player, String name) {
        Location location = warps.get(name.toLowerCase());
        if (location == null) {
            player.sendMessage(ChatColor.RED + "Warp not found.");
            return;
        }

        player.teleport(location);
        player.sendMessage(ChatColor.GREEN + "Teleported to warp " + name + ".");
    }

    private void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, TITLE);
        slots.clear();

        int slot = 0;
        for (String name : warps.keySet()) {
            if (slot >= 45) break;
            inventory.setItem(slot, item(Material.ENDER_EYE, ChatColor.GREEN + name, "Click to teleport"));
            slots.put(slot, name);
            slot++;
        }

        if (warps.isEmpty()) {
            inventory.setItem(22, item(Material.BARRIER, ChatColor.RED + "No Warps", "No server warps have been created."));
        }

        inventory.setItem(49, item(Material.BARRIER, ChatColor.GRAY + "Close"));
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!TITLE.equals(event.getView().getTitle())) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRawSlot() == 49) {
            player.closeInventory();
            return;
        }

        String name = slots.get(event.getRawSlot());
        if (name != null) {
            teleport(player, name);
            player.closeInventory();
        }
    }

    private ItemStack item(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(lore));
        stack.setItemMeta(meta);
        return stack;
    }

    private void load() {
        if (!plugin.getConfig().isConfigurationSection("warps")) return;

        for (String name : plugin.getConfig().getConfigurationSection("warps").getKeys(false)) {
            String path = "warps." + name;
            var world = Bukkit.getWorld(plugin.getConfig().getString(path + ".world"));
            if (world == null) continue;

            warps.put(name.toLowerCase(), new Location(
                    world,
                    plugin.getConfig().getDouble(path + ".x"),
                    plugin.getConfig().getDouble(path + ".y"),
                    plugin.getConfig().getDouble(path + ".z"),
                    (float) plugin.getConfig().getDouble(path + ".yaw"),
                    (float) plugin.getConfig().getDouble(path + ".pitch")
            ));
        }
    }

    private void save() {
        plugin.getConfig().set("warps", null);

        for (Map.Entry<String, Location> entry : warps.entrySet()) {
            String path = "warps." + entry.getKey();
            Location location = entry.getValue();

            plugin.getConfig().set(path + ".world", location.getWorld().getName());
            plugin.getConfig().set(path + ".x", location.getX());
            plugin.getConfig().set(path + ".y", location.getY());
            plugin.getConfig().set(path + ".z", location.getZ());
            plugin.getConfig().set(path + ".yaw", location.getYaw());
            plugin.getConfig().set(path + ".pitch", location.getPitch());
        }

        plugin.saveConfig();
    }
}
