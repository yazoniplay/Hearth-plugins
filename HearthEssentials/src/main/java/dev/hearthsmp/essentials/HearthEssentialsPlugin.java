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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class HearthEssentialsPlugin extends JavaPlugin implements Listener {
    private static final String MENU = ChatColor.DARK_GRAY + "🔥 HearthEssentials";
    private static final long TPA_TIMEOUT_MILLIS = 60_000L;

    private final Map<UUID, Location> homes = new HashMap<>();
    private final Map<UUID, PendingTeleport> teleportRequests = new HashMap<>();
    private final Map<UUID, UUID> lastMessaged = new HashMap<>();
    private Location spawn;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSpawn();
        loadHomes();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("HearthEssentials enabled.");
    }

    @Override
    public void onDisable() {
        saveHomes();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "essentials" -> openMenu(player);
            case "sethome" -> setHome(player);
            case "home" -> teleportHome(player);
            case "delhome" -> deleteHome(player);
            case "setspawn" -> setSpawn(player);
            case "spawn" -> teleportSpawn(player);
            case "tpa" -> requestTeleport(player, args);
            case "tpaccept" -> acceptTeleport(player);
            case "tpdeny" -> denyTeleport(player);
            case "msg" -> privateMessage(player, args);
            case "r" -> reply(player, args);
            default -> { return false; }
        }
        return true;
    }

    private void setHome(Player player) {
        homes.put(player.getUniqueId(), player.getLocation().clone());
        saveHomes();
        player.sendMessage(ChatColor.GREEN + "Home set.");
    }

    private void teleportHome(Player player) {
        Location home = homes.get(player.getUniqueId());
        if (home == null) {
            player.sendMessage(ChatColor.RED + "You have no home set.");
            return;
        }
        player.teleport(home);
        player.sendMessage(ChatColor.GREEN + "Teleported home.");
    }

    private void deleteHome(Player player) {
        if (homes.remove(player.getUniqueId()) == null) {
            player.sendMessage(ChatColor.RED + "You do not have a home set.");
            return;
        }
        saveHomes();
        player.sendMessage(ChatColor.YELLOW + "Home deleted.");
    }

    private void setSpawn(Player player) {
        if (!player.hasPermission("hearth.essentials.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission.");
            return;
        }
        spawn = player.getLocation().clone();
        saveSpawn();
        player.sendMessage(ChatColor.GREEN + "Spawn set.");
    }

    private void teleportSpawn(Player player) {
        if (spawn == null) {
            player.sendMessage(ChatColor.RED + "Spawn has not been set.");
            return;
        }
        player.teleport(spawn);
        player.sendMessage(ChatColor.GREEN + "Teleported to spawn.");
    }

    private void requestTeleport(Player requester, String[] args) {
        if (args.length != 1) {
            requester.sendMessage(ChatColor.GRAY + "Usage: /tpa <player>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            requester.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }
        if (target.equals(requester)) {
            requester.sendMessage(ChatColor.RED + "You cannot request teleportation to yourself.");
            return;
        }
        teleportRequests.put(target.getUniqueId(), new PendingTeleport(requester.getUniqueId(), System.currentTimeMillis()));
        target.sendMessage(ChatColor.YELLOW + requester.getName() + " wants to teleport to you. Use /tpaccept or /tpdeny within 60 seconds.");
        requester.sendMessage(ChatColor.GREEN + "Teleport request sent.");
    }

    private void acceptTeleport(Player target) {
        PendingTeleport pending = teleportRequests.remove(target.getUniqueId());
        if (pending == null || expired(pending)) {
            target.sendMessage(ChatColor.RED + "No valid pending request.");
            return;
        }
        Player requester = Bukkit.getPlayer(pending.requester());
        if (requester == null) {
            target.sendMessage(ChatColor.RED + "That player is no longer online.");
            return;
        }
        requester.teleport(target.getLocation());
        requester.sendMessage(ChatColor.GREEN + "Teleport request accepted.");
        target.sendMessage(ChatColor.GREEN + "Teleport request accepted.");
    }

    private void denyTeleport(Player target) {
        PendingTeleport pending = teleportRequests.remove(target.getUniqueId());
        if (pending == null || expired(pending)) {
            target.sendMessage(ChatColor.RED + "No valid pending request.");
            return;
        }
        Player requester = Bukkit.getPlayer(pending.requester());
        if (requester != null) requester.sendMessage(ChatColor.RED + target.getName() + " denied your teleport request.");
        target.sendMessage(ChatColor.YELLOW + "Teleport request denied.");
    }

    private boolean expired(PendingTeleport pending) {
        return System.currentTimeMillis() - pending.createdAt() > TPA_TIMEOUT_MILLIS;
    }

    private void privateMessage(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "Usage: /msg <player> <message>");
            return;
        }
        Player recipient = Bukkit.getPlayerExact(args[0]);
        if (recipient == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }
        sendPrivateMessage(sender, recipient, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
    }

    private void reply(Player sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "Usage: /r <message>");
            return;
        }
        UUID recipientId = lastMessaged.get(sender.getUniqueId());
        Player recipient = recipientId == null ? null : Bukkit.getPlayer(recipientId);
        if (recipient == null) {
            sender.sendMessage(ChatColor.RED + "Nobody is available to reply to.");
            return;
        }
        sendPrivateMessage(sender, recipient, String.join(" ", args));
    }

    private void sendPrivateMessage(Player sender, Player recipient, String message) {
        lastMessaged.put(sender.getUniqueId(), recipient.getUniqueId());
        lastMessaged.put(recipient.getUniqueId(), sender.getUniqueId());
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "To " + recipient.getName() + ": " + message);
        recipient.sendMessage(ChatColor.LIGHT_PURPLE + "From " + sender.getName() + ": " + message);
    }

    private void loadSpawn() {
        if (!getConfig().contains("spawn.world")) return;
        var world = Bukkit.getWorld(getConfig().getString("spawn.world"));
        if (world == null) return;
        spawn = new Location(world, getConfig().getDouble("spawn.x"), getConfig().getDouble("spawn.y"), getConfig().getDouble("spawn.z"), (float) getConfig().getDouble("spawn.yaw"), (float) getConfig().getDouble("spawn.pitch"));
    }

    private void saveSpawn() {
        getConfig().set("spawn.world", spawn.getWorld().getName());
        getConfig().set("spawn.x", spawn.getX());
        getConfig().set("spawn.y", spawn.getY());
        getConfig().set("spawn.z", spawn.getZ());
        getConfig().set("spawn.yaw", spawn.getYaw());
        getConfig().set("spawn.pitch", spawn.getPitch());
        saveConfig();
    }

    private void loadHomes() {
        if (!getConfig().isConfigurationSection("homes")) return;
        for (String key : getConfig().getConfigurationSection("homes").getKeys(false)) {
            UUID uuid;
            try { uuid = UUID.fromString(key); } catch (IllegalArgumentException ignored) { continue; }
            String path = "homes." + key;
            var world = Bukkit.getWorld(getConfig().getString(path + ".world"));
            if (world == null) continue;
            homes.put(uuid, new Location(world, getConfig().getDouble(path + ".x"), getConfig().getDouble(path + ".y"), getConfig().getDouble(path + ".z"), (float) getConfig().getDouble(path + ".yaw"), (float) getConfig().getDouble(path + ".pitch")));
        }
    }

    private void saveHomes() {
        getConfig().set("homes", null);
        for (Map.Entry<UUID, Location> entry : homes.entrySet()) {
            String path = "homes." + entry.getKey();
            Location location = entry.getValue();
            getConfig().set(path + ".world", location.getWorld().getName());
            getConfig().set(path + ".x", location.getX());
            getConfig().set(path + ".y", location.getY());
            getConfig().set(path + ".z", location.getZ());
            getConfig().set(path + ".yaw", location.getYaw());
            getConfig().set(path + ".pitch", location.getPitch());
        }
        saveConfig();
    }

    private void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, MENU);
        inventory.setItem(11, item(Material.RED_BED, ChatColor.GOLD + "Homes", "Use /sethome, /home and /delhome"));
        inventory.setItem(13, item(Material.COMPASS, ChatColor.GOLD + "Spawn", "Teleport to server spawn"));
        inventory.setItem(15, item(Material.ENDER_PEARL, ChatColor.GOLD + "Teleport", "Use /tpa to request teleportation"));
        inventory.setItem(22, item(Material.BOOK, ChatColor.YELLOW + "Help", "Everyday Hearth utilities"));
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
    public void onMenuClick(InventoryClickEvent event) {
        if (MENU.equals(event.getView().getTitle())) event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        teleportRequests.entrySet().removeIf(entry -> entry.getKey().equals(playerId) || entry.getValue().requester().equals(playerId));
        lastMessaged.remove(playerId);
        lastMessaged.values().removeIf(id -> id.equals(playerId));
    }

    private record PendingTeleport(UUID requester, long createdAt) {}
}
