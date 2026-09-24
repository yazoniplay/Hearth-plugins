package dev.hearthsmp.core.command;

import dev.hearthsmp.core.HearthCorePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public final class HearthCommand implements CommandExecutor, TabCompleter {
    private final HearthCorePlugin plugin;

    public HearthCommand(HearthCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            sender.sendMessage("§6§l🔥 " + plugin.getHearthConfig().serverName());
            sender.sendMessage("§7" + plugin.getHearthConfig().serverTagline());
            sender.sendMessage("§8HearthCore " + plugin.getPluginMeta().getVersion());
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("hearth.admin")) {
                sender.sendMessage("§cYou do not have permission to do that.");
                return true;
            }
            plugin.getHearthConfig().load();
            sender.sendMessage("§aHearthCore configuration reloaded.");
            return true;
        }

        sender.sendMessage("§7Usage: §f/hearth [info|reload]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("info", "reload");
        }
        return List.of();
    }
}
