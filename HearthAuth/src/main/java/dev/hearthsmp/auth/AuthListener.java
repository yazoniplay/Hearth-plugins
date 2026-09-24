package dev.hearthsmp.auth;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public final class AuthListener implements Listener {
    private final HearthAuthPlugin plugin;

    public AuthListener(HearthAuthPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getAuthService().isRegistered(player)) {
            prompt(player, "login-required");
        } else {
            prompt(player, "register-required");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getAuthService().isAuthenticated(event.getPlayer())
                && event.getTo() != null) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getAuthService().isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cPlease log in before chatting.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (plugin.getAuthService().isAuthenticated(event.getPlayer())) return;

        String command = event.getMessage().trim().toLowerCase();
        if (command.equals("/login") || command.startsWith("/login ")
                || command.equals("/register") || command.startsWith("/register ")) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage("§cPlease log in or register first.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.getAuthService().isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getAuthService().logout(event.getPlayer());
    }

    private void prompt(Player player, String key) {
        String message = plugin.getConfig().getString("messages." + key, "");
        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
        player.sendActionBar(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
    }
}
