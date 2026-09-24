package dev.hearthsmp.claims;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class ClaimCommand implements CommandExecutor, TabCompleter {
    private final HearthClaimsPlugin plugin;

    public ClaimCommand(HearthClaimsPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.hasPermission("hearth.claims.use")) {
            player.sendMessage(color(plugin.getConfig().getString("messages.not-owner")));
            return true;
        }

        Material material = Material.matchMaterial(plugin.getConfig().getString("claims.tool-material", "GOLDEN_SHOVEL"));
        if (material == null) material = Material.GOLDEN_SHOVEL;

        player.getInventory().addItem(new ItemStack(material));
        player.sendMessage(color(plugin.getConfig().getString("messages.shovel-given")));
        player.sendMessage(color("&7Right-click two corners with the shovel to create a claim."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }

    static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
