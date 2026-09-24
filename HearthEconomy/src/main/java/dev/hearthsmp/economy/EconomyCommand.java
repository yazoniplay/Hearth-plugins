package dev.hearthsmp.economy;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public final class EconomyCommand implements CommandExecutor {
    private final HearthEconomyPlugin plugin;

    public EconomyCommand(HearthEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("economy")) {
            if (!player.hasPermission("hearth.economy.use")) return true;
            EconomyMenu.open(plugin, player);
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(ChatColor.GRAY + "Usage: /pay <player> <amount>");
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(color(plugin.getConfig().getString("messages.player-not-found", "&cPlayer not found.")));
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage(color(plugin.getConfig().getString("messages.cannot-pay-self", "&cYou cannot pay yourself.")));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException exception) {
            player.sendMessage(color(plugin.getConfig().getString("messages.invalid-amount", "&cInvalid amount.")));
            return true;
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            player.sendMessage(color(plugin.getConfig().getString("messages.invalid-amount", "&cInvalid amount.")));
            return true;
        }

        if (!plugin.getEconomyService().withdraw(player.getUniqueId(), amount)) {
            player.sendMessage(color(plugin.getConfig().getString("messages.not-enough", "&cNot enough money.")));
            return true;
        }

        plugin.getEconomyService().deposit(target.getUniqueId(), amount);

        send(player, "paid", "{amount}", plugin.getEconomyService().format(amount), "{player}", target.getName());
        send(target, "received", "{amount}", plugin.getEconomyService().format(amount), "{player}", player.getName());
        return true;
    }

    private void send(Player player, String key, String... replacements) {
        String message = plugin.getConfig().getString("messages." + key, "");
        for (int i = 0; i + 1 < replacements.length; i += 2) message = message.replace(replacements[i], replacements[i + 1]);
        player.sendMessage(color(message));
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
