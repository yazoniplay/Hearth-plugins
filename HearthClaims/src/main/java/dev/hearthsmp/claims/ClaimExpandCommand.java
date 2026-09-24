package dev.hearthsmp.claims;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public final class ClaimExpandCommand implements CommandExecutor, TabCompleter {
    private final HearthClaimsPlugin plugin;

    public ClaimExpandCommand(HearthClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!plugin.getConfig().getBoolean("claims.expansion-enabled", true)) {
            player.sendMessage(msg("expansion-disabled"));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(msg("expansion-usage"));
            return true;
        }

        Claim claim = plugin.getClaimManager().getAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(msg("no-claim"));
            return true;
        }
        if (!claim.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(msg("expansion-not-owner"));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(msg("expansion-invalid"));
            return true;
        }

        if (amount == 0) {
            player.sendMessage(msg("expansion-invalid"));
            return true;
        }

        int maximum = plugin.getConfig().getInt("claims.maximum-expansion-blocks", 10000);
        if (Math.abs(amount) > maximum) {
            player.sendMessage(msg("expansion-too-large").replace("{maximum}", String.valueOf(maximum)));
            return true;
        }

        int minX = claim.getMinX();
        int maxX = claim.getMaxX();
        int minZ = claim.getMinZ();
        int maxZ = claim.getMaxZ();

        switch (args[0].toLowerCase()) {
            case "north" -> minZ -= amount;
            case "south" -> maxZ += amount;
            case "west" -> minX -= amount;
            case "east" -> maxX += amount;
            default -> {
                player.sendMessage(msg("expansion-usage"));
                return true;
            }
        }

        int newBlocks = (maxX - minX + 1) * (maxZ - minZ + 1);
        int oldBlocks = claim.getBlocks();
        int used = plugin.getClaimManager().getOwned(player.getUniqueId()).stream()
                .mapToInt(Claim::getBlocks).sum();

        int limit = plugin.getClaimManager().claimBlockLimit(player);
        if (used - oldBlocks + newBlocks > limit) {
            player.sendMessage(msg("claim-limit"));
            player.sendMessage(ChatColor.GRAY + "Used: " + used + " / " + limit + " claim blocks.");
            return true;
        }

        if (newBlocks < plugin.getConfig().getInt("claims.minimum-size", 25)) {
            player.sendMessage(msg("claim-too-small").replace("{minimum}",
                    String.valueOf(plugin.getConfig().getInt("claims.minimum-size", 25))));
            return true;
        }

        if (!plugin.getClaimManager().resize(claim, minX, maxX, minZ, maxZ)) {
            player.sendMessage(msg("expansion-overlap"));
            return true;
        }

        player.sendMessage(msg("expansion-success").replace("{blocks}", String.valueOf(newBlocks)));
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return java.util.List.of("north", "south", "east", "west");
        }
        return java.util.Collections.emptyList();
    }

    private String msg(String key) {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages." + key, ""));
    }
}
