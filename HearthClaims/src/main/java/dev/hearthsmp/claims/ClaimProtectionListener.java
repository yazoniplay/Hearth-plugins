package dev.hearthsmp.claims;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class ClaimProtectionListener implements Listener {
    private final HearthClaimsPlugin plugin;

    public ClaimProtectionListener(HearthClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (deny(event.getPlayer(), event.getBlock())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (deny(event.getPlayer(), event.getBlock())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Claim claim = plugin.getClaimManager().getAt(event.getClickedBlock().getLocation());
        if (claim != null && !claim.canBuild(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            String message = color(plugin.getConfig().getString("messages.protected", "&cThis land is claimed."));
            event.getPlayer().sendActionBar(message);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof Block block)) return;
        deny(player, block);
    }

    private boolean deny(Player player, Block block) {
        Claim claim = plugin.getClaimManager().getAt(block.getLocation());
        if (claim == null || claim.canBuild(player.getUniqueId())) return false;

        String message = color(plugin.getConfig().getString("messages.protected", "&cThis land is claimed."));
        player.sendMessage(message);
        player.sendActionBar(message);
        return true;
    }

    private String color(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
