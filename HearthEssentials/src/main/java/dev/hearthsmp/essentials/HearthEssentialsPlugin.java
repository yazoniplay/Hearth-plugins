package dev.hearthsmp.essentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HearthEssentialsPlugin extends JavaPlugin implements Listener {
    private final Map<UUID, Location> homes = new HashMap<>();
    private final Map<UUID, UUID> teleportRequests = new HashMap<>();
    private Location spawn;
    private static final String MENU = ChatColor.DARK_GRAY + "🔥 HearthEssentials";

    @Override public void onEnable() {
        saveDefaultConfig();
        if (getConfig().contains("spawn.world")) {
            var world = Bukkit.getWorld(getConfig().getString("spawn.world"));
            if (world != null) spawn = new Location(world, getConfig().getDouble("spawn.x"), getConfig().getDouble("spawn.y"), getConfig().getDouble("spawn.z"), (float)getConfig().getDouble("spawn.yaw"), (float)getConfig().getDouble("spawn.pitch"));
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("HearthEssentials enabled.");
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        switch (command.getName().toLowerCase()) {
            case "essentials": openMenu(p); return true;
            case "sethome": homes.put(p.getUniqueId(), p.getLocation().clone()); p.sendMessage(ChatColor.GREEN + "Home set."); return true;
            case "home": { Location home = homes.get(p.getUniqueId()); if (home == null) p.sendMessage(ChatColor.RED + "You have no home set."); else p.teleport(home); return true; }
            case "delhome": homes.remove(p.getUniqueId()); p.sendMessage(ChatColor.YELLOW + "Home deleted."); return true;
            case "setspawn": if (!p.hasPermission("hearth.essentials.admin")) return true; spawn = p.getLocation().clone(); getConfig().set("spawn.world", spawn.getWorld().getName()); getConfig().set("spawn.x", spawn.getX()); getConfig().set("spawn.y", spawn.getY()); getConfig().set("spawn.z", spawn.getZ()); getConfig().set("spawn.yaw", spawn.getYaw()); getConfig().set("spawn.pitch", spawn.getPitch()); saveConfig(); p.sendMessage(ChatColor.GREEN + "Spawn set."); return true;
            case "spawn": if (spawn == null) p.sendMessage(ChatColor.RED + "Spawn has not been set."); else p.teleport(spawn); return true;
            case "tpa": if (args.length != 1) { p.sendMessage(ChatColor.GRAY + "Usage: /tpa <player>"); return true; } Player target = Bukkit.getPlayerExact(args[0]); if (target == null) { p.sendMessage(ChatColor.RED + "Player not found."); return true; } teleportRequests.put(target.getUniqueId(), p.getUniqueId()); target.sendMessage(ChatColor.YELLOW + p.getName() + " wants to teleport to you. Use /tpaccept or /tpdeny."); p.sendMessage(ChatColor.GREEN + "Teleport request sent."); return true;
            case "tpaccept": { UUID requester = teleportRequests.remove(p.getUniqueId()); if (requester == null) { p.sendMessage(ChatColor.RED + "No pending request."); return true; } Player from = Bukkit.getPlayer(requester); if (from != null) { from.teleport(p.getLocation()); from.sendMessage(ChatColor.GREEN + "Teleport request accepted."); } return true; }
            case "tpdeny": teleportRequests.remove(p.getUniqueId()); p.sendMessage(ChatColor.YELLOW + "Teleport request denied."); return true;
            case "msg": if (args.length < 2) { p.sendMessage(ChatColor.GRAY + "Usage: /msg <player> <message>"); return true; } Player recipient = Bukkit.getPlayerExact(args[0]); if (recipient == null) { p.sendMessage(ChatColor.RED + "Player not found."); return true; } String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)); p.sendMessage(ChatColor.LIGHT_PURPLE + "To " + recipient.getName() + ": " + message); recipient.sendMessage(ChatColor.LIGHT_PURPLE + "From " + p.getName() + ": " + message); return true;
            default: return false;
        }
    }

    private void openMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU);
        inv.setItem(11, item(Material.RED_BED, ChatColor.GOLD + "Homes", "Set and visit your home"));
        inv.setItem(13, item(Material.COMPASS, ChatColor.GOLD + "Spawn", "Teleport to server spawn"));
        inv.setItem(15, item(Material.ENDER_PEARL, ChatColor.GOLD + "Teleport", "Use /tpa to request teleportation"));
        inv.setItem(22, item(Material.BOOK, ChatColor.YELLOW + "Help", "Everyday Hearth utilities"));
        p.openInventory(inv);
    }

    private ItemStack item(Material material, String name, String lore) { ItemStack stack = new ItemStack(material); ItemMeta meta = stack.getItemMeta(); meta.setDisplayName(name); meta.setLore(java.util.List.of(ChatColor.GRAY + lore)); stack.setItemMeta(meta); return stack; }
    @EventHandler public void onMenuClick(InventoryClickEvent event) { if (MENU.equals(event.getView().getTitle())) event.setCancelled(true); }
    @EventHandler public void onQuit(PlayerQuitEvent event) { teleportRequests.values().removeIf(id -> id.equals(event.getPlayer().getUniqueId())); }
}
