package dev.hearthsmp.claims;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public final class ClaimInfoCommand implements CommandExecutor {
    private final HearthClaimsPlugin plugin;

    public ClaimInfoCommand(HearthClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        Claim claim = plugin.getClaimManager().getAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(color(plugin.getConfig().getString("messages.no-claim")));
            return true;
        }

        player.sendMessage(color(plugin.getConfig().getString("messages.prefix")));
        player.sendMessage(ChatColor.GRAY + "Owner: " + ChatColor.WHITE
                + plugin.getServer().getOfflinePlayer(claim.getOwner()).getName());
        player.sendMessage(ChatColor.GRAY + "Size: " + ChatColor.WHITE + claim.getBlocks() + " blocks");
        player.sendMessage(ChatColor.GRAY + "Bounds: " + ChatColor.WHITE
                + claim.getMinX() + ", " + claim.getMinZ() + " → "
                + claim.getMaxX() + ", " + claim.getMaxZ());
        player.sendMessage(ChatColor.GRAY + "Trusted: " + ChatColor.WHITE + claim.getTrusted().size());
        return true;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
