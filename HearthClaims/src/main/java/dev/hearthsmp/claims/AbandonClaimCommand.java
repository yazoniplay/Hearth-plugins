package dev.hearthsmp.claims;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public final class AbandonClaimCommand implements CommandExecutor {
    private final HearthClaimsPlugin plugin;

    public AbandonClaimCommand(HearthClaimsPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        Claim claim = plugin.getClaimManager().getAt(player.getLocation());
        if (claim == null || !claim.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ClaimCommand.color(plugin.getConfig().getString("messages.not-owner")));
            return true;
        }

        plugin.getClaimManager().delete(claim);
        player.sendMessage(ClaimCommand.color(plugin.getConfig().getString("messages.claim-abandoned")));
        return true;
    }
}
