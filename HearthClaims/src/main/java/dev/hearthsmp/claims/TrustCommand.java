package dev.hearthsmp.claims;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public final class TrustCommand implements CommandExecutor {
    private final HearthClaimsPlugin plugin;

    public TrustCommand(HearthClaimsPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        Claim claim = plugin.getClaimManager().getAt(player.getLocation());
        if (claim == null || !claim.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ClaimCommand.color(plugin.getConfig().getString("messages.not-owner")));
            return true;
        }
        if (args.length != 1) return false;
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer must be online.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("trust")) {
            claim.getTrusted().add(target.getUniqueId());
            player.sendMessage(ClaimCommand.color(plugin.getConfig().getString("messages.trusted")).replace("{player}", target.getName()));
        } else {
            claim.getTrusted().remove(target.getUniqueId());
            player.sendMessage(ClaimCommand.color(plugin.getConfig().getString("messages.untrusted")).replace("{player}", target.getName()));
        }
        plugin.getClaimManager().save();
        return true;
    }
}
