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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public final class HearthEssentialsPlugin extends JavaPlugin implements Listener {
    private static final String MENU = ChatColor.DARK_GRAY + "🔥 HearthEssentials";
    private static final String HOMES_MENU = ChatColor.DARK_GRAY + "🔥 Hearth Homes";
    private static final String TELEPORT_MENU = ChatColor.DARK_GRAY + "🔥 Hearth Teleport";
    private static final long TPA_TIMEOUT_MILLIS = 60_000L;

    private final Map<UUID, Location> homes = new HashMap<>();
    private final Map<UUID, PendingTeleport> teleportRequests = new HashMap<>();
    private final Map<UUID, UUID> lastMessaged = new HashMap<>();
    private final Map<UUID, Map<Integer, UUID>> teleportMenuPlayers = new HashMap<>();
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Set<UUID> afkPlayers = new HashSet<>();
    private final Map<UUID, Set<UUID>> ignoredPlayers = new HashMap<>();
    private Location spawn;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSpawn();
        loadHomes();
        loadIgnoredPlayers();
        Bukkit.getPluginManager().registerEvents(this, this);
        WarpCommand warpCommand = new WarpCommand(this);
        getCommand("warp").setExecutor(warpCommand);
        getCommand("setwarp").setExecutor(warpCommand);
        getCommand("delwarp").setExecutor(warpCommand);
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
            case "back" -> teleportBack(player);
            case "afk" -> toggleAfk(player);
            case "ignore" -> ignorePlayer(player, args);
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
        recordBack(player);
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
        recordBack(player);
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
        recordBack(requester);
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
        if (ignoredPlayers.getOrDefault(recipient.getUniqueId(), Set.of()).contains(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + recipient.getName() + " is ignoring you.");
            return;
        }
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
        Inventory inventory = Bukkit.createInventory(null, 36, MENU);
        inventory.setItem(10, item(Material.RED_BED, ChatColor.GOLD + "Homes", "Open home management"));
        inventory.setItem(12, item(Material.COMPASS, ChatColor.GOLD + "Spawn", "Click to teleport to spawn"));
        inventory.setItem(14, item(Material.ENDER_PEARL, ChatColor.GOLD + "Teleport", "Choose an online player"));
        inventory.setItem(16, item(Material.ENDER_EYE, ChatColor.GOLD + "Warps", "Open server warps"));
        inventory.setItem(21, item(Material.COMPASS, ChatColor.GOLD + "Back", "Return to your last location"));
        inventory.setItem(23, item(Material.CLOCK, ChatColor.GOLD + "AFK", afkPlayers.contains(player.getUniqueId()) ? "You are currently AFK" : "Toggle AFK mode"));
        inventory.setItem(31, item(Material.BOOK, ChatColor.YELLOW + "Help", "Hearth everyday utilities"));
        player.openInventory(inventory);
    }

    private void openHomesMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, HOMES_MENU);
        inventory.setItem(11, item(Material.RED_BED, ChatColor.GREEN + "Go Home", homes.containsKey(player.getUniqueId()) ? "Click to teleport home" : "No home set"));
        inventory.setItem(13, item(Material.WRITABLE_BOOK, ChatColor.YELLOW + "Set Home", "Save your current location"));
        inventory.setItem(15, item(Material.BARRIER, ChatColor.RED + "Delete Home", homes.containsKey(player.getUniqueId()) ? "Remove your home" : "No home to delete"));
        inventory.setItem(22, item(Material.ARROW, ChatColor.GRAY + "Back", "Return to HearthEssentials"));
        player.openInventory(inventory);
    }

    private void openTeleportMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, TELEPORT_MENU);
        Map<Integer, UUID> players = new HashMap<>();
        int slot = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(player)) continue;
            if (slot >= 45) break;
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName(ChatColor.GREEN + target.getName());
            meta.setLore(List.of(ChatColor.GRAY + "Click to send a teleport request"));
            head.setItemMeta(meta);
            inventory.setItem(slot, head);
            players.put(slot, target.getUniqueId());
            slot++;
        }
        if (players.isEmpty()) inventory.setItem(22, item(Material.BARRIER, ChatColor.RED + "No Other Players", "Nobody else is online"));
        inventory.setItem(49, item(Material.ARROW, ChatColor.GRAY + "Back", "Return to HearthEssentials"));
        teleportMenuPlayers.put(player.getUniqueId(), players);
        player.openInventory(inventory);
    }

    private ItemStack item(Material material, String name, String... loreLines) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(loreLines));
        stack.setItemMeta(meta);
        return stack;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (MENU.equals(title)) {
            event.setCancelled(true);
            if (event.getRawSlot() == 10) openHomesMenu(player);
            else if (event.getRawSlot() == 12) teleportSpawn(player);
            else if (event.getRawSlot() == 14) openTeleportMenu(player);
            else if (event.getRawSlot() == 16) player.performCommand("warp");
            else if (event.getRawSlot() == 21) teleportBack(player);
            else if (event.getRawSlot() == 23) toggleAfk(player);
        } else if (HOMES_MENU.equals(title)) {
            event.setCancelled(true);
            if (event.getRawSlot() == 11) {
                teleportHome(player);
            } else if (event.getRawSlot() == 13) {
                setHome(player);
                openHomesMenu(player);
            } else if (event.getRawSlot() == 15) {
                deleteHome(player);
                openHomesMenu(player);
            } else if (event.getRawSlot() == 22) {
                openMenu(player);
            }
        } else if (TELEPORT_MENU.equals(title)) {
            event.setCancelled(true);
            if (event.getRawSlot() == 49) {
                openMenu(player);
                return;
            }
            UUID targetId = teleportMenuPlayers.getOrDefault(player.getUniqueId(), Map.of()).get(event.getRawSlot());
            if (targetId != null) {
                Player target = Bukkit.getPlayer(targetId);
                if (target != null) requestTeleport(player, new String[]{target.getName()});
                else player.sendMessage(ChatColor.RED + "That player is no longer online.");
                openTeleportMenu(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        teleportRequests.entrySet().removeIf(entry -> entry.getKey().equals(playerId) || entry.getValue().requester().equals(playerId));
        lastMessaged.remove(playerId);
        lastMessaged.values().removeIf(id -> id.equals(playerId));
        teleportMenuPlayers.remove(playerId);
        lastLocations.remove(playerId);
        afkPlayers.remove(playerId);
    }

    private void recordBack(Player player) {
        lastLocations.put(player.getUniqueId(), player.getLocation().clone());
    }

    private void teleportBack(Player player) {
        Location location = lastLocations.get(player.getUniqueId());
        if (location == null) {
            player.sendMessage(ChatColor.RED + "You have no previous location.");
            return;
        }
        Location current = player.getLocation().clone();
        player.teleport(location);
        lastLocations.put(player.getUniqueId(), current);
        player.sendMessage(ChatColor.GREEN + "Teleported back.");
    }

    private void toggleAfk(Player player) {
        UUID id = player.getUniqueId();
        if (afkPlayers.remove(id)) {
            player.sendMessage(ChatColor.GREEN + "You are no longer AFK.");
        } else {
            afkPlayers.add(id);
            player.sendMessage(ChatColor.YELLOW + "You are now AFK.");
        }
    }

    private void ignorePlayer(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.GRAY + "Usage: /ignore <player>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }
        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot ignore yourself.");
            return;
        }
        Set<UUID> ignored = ignoredPlayers.computeIfAbsent(player.getUniqueId(), key -> new HashSet<>());
        if (!ignored.add(target.getUniqueId())) {
            ignored.remove(target.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "You are no longer ignoring " + target.getName() + ".");
        } else {
            player.sendMessage(ChatColor.GREEN + "You are now ignoring " + target.getName() + ".");
        }
        saveIgnoredPlayers();
    }

    private void loadIgnoredPlayers() {
        if (!getConfig().isConfigurationSection("ignored")) return;
        for (String key : getConfig().getConfigurationSection("ignored").getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(key);
                Set<UUID> ignored = new HashSet<>();
                for (String target : getConfig().getStringList("ignored." + key)) {
                    try { ignored.add(UUID.fromString(target)); } catch (IllegalArgumentException ignoredTarget) {}
                }
                ignoredPlayers.put(playerId, ignored);
            } catch (IllegalArgumentException ignoredPlayer) {}
        }
    }

    private void saveIgnoredPlayers() {
        getConfig().set("ignored", null);
        for (Map.Entry<UUID, Set<UUID>> entry : ignoredPlayers.entrySet()) {
            getConfig().set("ignored." + entry.getKey(), entry.getValue().stream().map(UUID::toString).toList());
        }
        saveConfig();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        if (afkPlayers.remove(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(ChatColor.YELLOW + "You are no longer AFK.");
        }
    }

    private record PendingTeleport(UUID requester, long createdAt) {}
}
