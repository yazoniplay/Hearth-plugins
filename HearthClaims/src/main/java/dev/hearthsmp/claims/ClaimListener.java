package dev.hearthsmp.claims;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class ClaimListener implements Listener {
    private final HearthClaimsPlugin plugin;

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
            selection[0] = event.getClickedBlock().getLocation();
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
        var selection = plugin.getClaimManager().getSelection(player.getUniqueId());
        if (selection[0] == null) return;

        var first = selection[0];
        var second = selection[1] == null ? first : selection[1];
        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());
        double y = player.getLocation().getY() + 1.0;
        int spacing = Math.max(1, plugin.getConfig().getInt("visualization.spacing", 3));

        for (int x = minX; x <= maxX; x += spacing) {
            particle(player, x + 0.5, y, minZ + 0.5);
            particle(player, x + 0.5, y, maxZ + 0.5);
        }
        for (int z = minZ; z <= maxZ; z += spacing) {
            particle(player, minX + 0.5, y, z + 0.5);
            particle(player, maxX + 0.5, y, z + 0.5);
        }
    }

    private void particle(Player player, double x, double y, double z) {
        player.spawnParticle(org.bukkit.Particle.FLAME, x, y, z, 1, 0, 0, 0, 0);
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

        player.sendMessage(msg("claim-created").replace("{blocks}", String.valueOf(blocks)));
        player.sendMessage(ChatColor.GRAY + "Claim blocks remaining: " + (limit - used - blocks) + " / " + limit);
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
