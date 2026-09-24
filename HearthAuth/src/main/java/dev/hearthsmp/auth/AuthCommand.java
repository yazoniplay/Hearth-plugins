package dev.hearthsmp.auth;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AuthCommand implements CommandExecutor {
    private final HearthAuthPlugin plugin;

    public AuthCommand(HearthAuthPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        if (!player.hasPermission("hearth.auth.use")) return true;

        switch (command.getName().toLowerCase()) {
            case "register" -> register(player, args);
            case "login" -> login(player, args);
            case "logout" -> logout(player);
            default -> {
            }
        }
        return true;
    }

    private void register(Player player, String[] args) {
        if (plugin.getAuthService().isRegistered(player)) {
            send(player, "already-registered");
            return;
        }
        if (args.length != 2) {
            send(player, "usage-register");
            return;
        }

        String password = args[0];
        String confirmation = args[1];
        int minimum = plugin.getConfig().getInt("auth.password.minimum-length", 6);
        int maximum = plugin.getConfig().getInt("auth.password.maximum-length", 64);

        if (password.length() < minimum) {
            send(player, "password-short", "{minimum}", String.valueOf(minimum));
            return;
        }
        if (password.length() > maximum) {
            send(player, "password-long");
            return;
        }
        if (!password.equals(confirmation)) {
            send(player, "passwords-differ");
            return;
        }

        if (plugin.getAuthService().register(player, password)) {
            send(player, "registered");
        } else {
            send(player, "already-registered");
        }
    }

    private void login(Player player, String[] args) {
        if (!plugin.getAuthService().isRegistered(player)) {
            send(player, "not-registered");
            return;
        }
        if (plugin.getAuthService().isAuthenticated(player)) {
            send(player, "already-logged-in");
            return;
        }
        if (args.length != 1) {
            send(player, "usage-login");
            return;
        }

        if (plugin.getAuthService().login(player, args[0])) {
            send(player, "logged-in");
        } else {
            send(player, "wrong-password");
        }
    }

    private void logout(Player player) {
        if (!plugin.getAuthService().isAuthenticated(player)) {
            send(player, "login-required");
            return;
        }
        plugin.getAuthService().logout(player);
        send(player, "logged-out");
    }

    private void send(Player player, String key, String... replacements) {
        String message = plugin.getConfig().getString("messages." + key, "");
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
