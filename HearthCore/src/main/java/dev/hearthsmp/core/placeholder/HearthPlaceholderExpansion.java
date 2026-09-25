package dev.hearthsmp.core.placeholder;

import dev.hearthsmp.core.HearthCorePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class HearthPlaceholderExpansion extends PlaceholderExpansion {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneOffset.UTC);

    private final HearthCorePlugin plugin;

    public HearthPlaceholderExpansion(HearthCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "hearth";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Hearth SMP";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("server")) {
            return plugin.getHearthConfig().serverName();
        }
        if (params.equalsIgnoreCase("tagline")) {
            return plugin.getHearthConfig().serverTagline();
        }
        if (params.equalsIgnoreCase("online")) {
            return String.valueOf(Bukkit.getOnlinePlayers().size());
        }
        if (params.equalsIgnoreCase("max_players")) {
            return String.valueOf(Bukkit.getMaxPlayers());
        }

        if (player == null) {
            return "";
        }

        return switch (params.toLowerCase()) {
            case "player", "name" -> player.getName();
            case "uuid" -> player.getUniqueId().toString();
            case "world" -> player.getWorld().getName();
            case "x" -> String.valueOf(player.getLocation().getBlockX());
            case "y" -> String.valueOf(player.getLocation().getBlockY());
            case "z" -> String.valueOf(player.getLocation().getBlockZ());
            case "first_join" -> {
                long firstPlayed = player.getFirstPlayed();
                yield firstPlayed <= 0 ? "" : DATE.format(Instant.ofEpochMilli(firstPlayed));
            }
            case "health" -> String.format(java.util.Locale.US, "%.1f", player.getHealth());
            case "level" -> String.valueOf(player.getLevel());
            case "xp_percent" -> String.format(java.util.Locale.US, "%.0f", player.getExp() * 100.0);
            default -> null;
        };
    }
}
