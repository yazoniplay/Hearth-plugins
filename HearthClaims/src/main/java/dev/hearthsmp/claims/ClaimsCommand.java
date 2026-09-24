package dev.hearthsmp.claims;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;

public final class ClaimsCommand implements CommandExecutor {
    private final HearthClaimsPlugin plugin;

    public ClaimsCommand(HearthClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        int limit = plugin.getClaimManager().claimBlockLimit(player);
        int used = plugin.getClaimManager().getOwned(player.getUniqueId())
                .stream().mapToInt(Claim::getBlocks).sum();
        int remaining = Math.max(0, limit - used);

        player.sendMessage(color(plugin.getConfig().getString("messages.prefix", "&6&l🔥 Hearth &8»")));
        player.sendMessage(ChatColor.GRAY + "Claim blocks: " + ChatColor.WHITE + used
                + ChatColor.GRAY + " / " + ChatColor.WHITE + limit
                + ChatColor.GRAY + " used");
        player.sendMessage(ChatColor.GRAY + "Remaining: " + ChatColor.GREEN + remaining);
        player.sendMessage(ChatColor.GRAY + "Claims: " + ChatColor.WHITE
                + plugin.getClaimManager().getOwned(player.getUniqueId()).size()
                + ChatColor.GRAY + " / "
                + plugin.getConfig().getInt("claims.maximum-claims-per-player", 10));
        return true;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
