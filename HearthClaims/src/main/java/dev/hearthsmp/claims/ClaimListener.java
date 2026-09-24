package dev.hearthsmp.claims;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClaimListener implements Listener {
    private final HearthClaimsPlugin plugin;
    private final Map<UUID, UUID> lastClaim = new HashMap<>();
    private final Map<UUID, List<Location>> selectionVisualization = new HashMap<>();

    public ClaimListener(HearthClaimsPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getClickedBlock() == null) return;

        Material tool = Material.matchMaterial(plugin.getConfig().getString("claims.tool-material", "GOLDEN_SHOVEL"));
        if (tool == null) tool = Material.GOLDEN_SHOVEL;
        if (event.getItem() == null || event.getItem().getType() != tool) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("hearth.claims.use")) return;

        event.setCancelled(true);
        var selection = plugin.getClaimManager().getSelection(player.getUniqueId());

        if (event.getAction().isLeftClick()) {
            clearSelectionVisualization(player);
            selection[0] = event.getClickedBlock().getLocation();
            selection[1] = null;
            showSelection(player);
            player.sendMessage(msg("selection-start")
                    .replace("{x}", String.valueOf(selection[0].getBlockX()))
                    .replace("{z}", String.valueOf(selection[0].getBlockZ())));
        } else if (event.getAction().isRightClick()) {
            selection[1] = event.getClickedBlock().getLocation();
            showSelection(player);
            player.sendMessage(msg("selection-end")
                    .replace("{x}", String.valueOf(selection[1].getBlockX()))
                    .replace("{z}", String.valueOf(selection[1].getBlockZ())));
            tryCreate(player);
        }
    }

    private void showSelection(Player player) {
        clearSelectionVisualization(player);

        var selection = plugin.getClaimManager().getSelection(player.getUniqueId());
        if (selection[0] == null) return;

        var first = selection[0];
        var second = selection[1] == null ? first : selection[1];

        if (first.getWorld() == null || second.getWorld() == null
                || !first.getWorld().equals(second.getWorld())) return;

        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());

        int y = player.getLocation().getBlockY();
        int spacing = Math.max(1, plugin.getConfig().getInt("visualization.spacing", 3));

        List<Location> visualized = new ArrayList<>();

        for (int x = minX; x <= maxX; x += spacing) {
            addFakeBlock(player, new Location(first.getWorld(), x, y, minZ), Material.YELLOW_STAINED_GLASS, visualized);
            addFakeBlock(player, new Location(first.getWorld(), x, y, maxZ), Material.YELLOW_STAINED_GLASS, visualized);
        }

        for (int z = minZ; z <= maxZ; z += spacing) {
            addFakeBlock(player, new Location(first.getWorld(), minX, y, z), Material.YELLOW_STAINED_GLASS, visualized);
            addFakeBlock(player, new Location(first.getWorld(), maxX, y, z), Material.YELLOW_STAINED_GLASS, visualized);
        }

        // Always highlight the exact selected corners.
        addFakeBlock(player, new Location(first.getWorld(), minX, y, minZ), Material.GOLD_BLOCK, visualized);
        addFakeBlock(player, new Location(first.getWorld(), minX, y, maxZ), Material.GOLD_BLOCK, visualized);
        addFakeBlock(player, new Location(first.getWorld(), maxX, y, minZ), Material.GOLD_BLOCK, visualized);
        addFakeBlock(player, new Location(first.getWorld(), maxX, y, maxZ), Material.GOLD_BLOCK, visualized);

        selectionVisualization.put(player.getUniqueId(), visualized);
    }

    private void addFakeBlock(Player player, Location location, Material material, List<Location> visualized) {
        if (location.getWorld() == null) return;
        player.sendBlockChange(location, material.createBlockData());
        visualized.add(location);
    }

    private void clearSelectionVisualization(Player player) {
        List<Location> locations = selectionVisualization.remove(player.getUniqueId());
        if (locations == null) return;

        for (Location location : locations) {
            if (location.getWorld() != null) {
                player.sendBlockChange(location, location.getBlock().getBlockData());
            }
        }
    }

    private void tryCreate(Player player) {
        var selection = plugin.getClaimManager().getSelection(player.getUniqueId());
        if (selection[0] == null || selection[1] == null) return;

        var first = selection[0];
        var second = selection[1];

        if (!first.getWorld().equals(second.getWorld())) {
            player.sendMessage(ChatColor.RED + "Both corners must be in the same world.");
            return;
        }

        int blocks = (Math.abs(first.getBlockX() - second.getBlockX()) + 1)
                * (Math.abs(first.getBlockZ() - second.getBlockZ()) + 1);

        int minimum = plugin.getConfig().getInt("claims.minimum-size", 25);
        if (blocks < minimum) {
            player.sendMessage(msg("claim-too-small").replace("{minimum}", String.valueOf(minimum)));
            return;
        }

        int limit = claimBlockLimit(player);
        int used = plugin.getClaimManager().getOwned(player.getUniqueId())
                .stream().mapToInt(Claim::getBlocks).sum();

        if (used + blocks > limit) {
            player.sendMessage(msg("claim-limit"));
            player.sendMessage(ChatColor.GRAY + "Used: " + used + " / " + limit + " claim blocks.");
            return;
        }

        int maxClaims = plugin.getConfig().getInt("claims.maximum-claims-per-player", 10);
        if (plugin.getClaimManager().getOwned(player.getUniqueId()).size() >= maxClaims) {
            player.sendMessage(msg("too-many-claims"));
            return;
        }

        if (plugin.getClaimManager().overlaps(first, second)) {
            player.sendMessage(ChatColor.RED + "That area overlaps an existing claim.");
            return;
        }

        plugin.getClaimManager().create(player.getUniqueId(), first, second);
        plugin.getClaimManager().clearSelection(player.getUniqueId());
        clearSelectionVisualization(player);

        player.sendMessage(msg("claim-created").replace("{blocks}", String.valueOf(blocks)));
        player.sendMessage(ChatColor.GRAY + "Claim blocks remaining: " + (limit - used - blocks) + " / " + limit);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getWorld() == to.getWorld()
                && from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Claim claim = plugin.getClaimManager().getAt(to);
        UUID currentId = claim == null ? null : claim.getId();
        UUID previousId = lastClaim.put(player.getUniqueId(), currentId);

        if (currentId == null || currentId.equals(previousId)) return;

        String ownerName = plugin.getServer().getOfflinePlayer(claim.getOwner()).getName();
        if (ownerName == null || ownerName.isBlank()) ownerName = "another player";

        player.sendActionBar(ChatColor.GOLD + "You've just entered "
                + ChatColor.YELLOW + ownerName
                + ChatColor.GOLD + "'s claim!");
    }

    private int claimBlockLimit(Player player) {
        int highest = plugin.getConfig().getInt("ranks.default.claim-blocks", 500);
        var ranks = plugin.getConfig().getConfigurationSection("ranks");

        if (ranks != null) {
            for (String rank : ranks.getKeys(false)) {
                String permission = ranks.getString(rank + ".permission");
                if (permission != null && player.hasPermission(permission)) {
                    highest = Math.max(highest, ranks.getInt(rank + ".claim-blocks", highest));
                }
            }
        }
        return highest;
    }

    private String msg(String key) {
        return ClaimCommand.color(plugin.getConfig().getString("messages." + key, ""));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (protect(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (protect(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    private boolean protect(Player player, Block block) {
        Claim claim = plugin.getClaimManager().getAt(block.getLocation());

        if (claim != null && !claim.canBuild(player.getUniqueId())) {
            String message = msg("protected");
            player.sendMessage(message);
            player.sendActionBar(message);
            return true;
        }
        return false;
    }
}
