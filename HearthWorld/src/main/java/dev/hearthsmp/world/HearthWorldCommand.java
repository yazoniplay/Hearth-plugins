package dev.hearthsmp.world;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HearthWorldCommand implements CommandExecutor {
    private static final Map<String, Integer> REGIONS = new LinkedHashMap<>();
    static {
        REGIONS.put("frozen-taiga", 0);
        REGIONS.put("snowstorm-plains", 1);
        REGIONS.put("glacier-fields", 2);
        REGIONS.put("frostpeak-mountains", 3);
        REGIONS.put("snowy-pinewood", 4);
        REGIONS.put("frozen-grove", 5);
        REGIONS.put("icebound-coast", 6);
        REGIONS.put("frost-valley", 7);
        REGIONS.put("aurora-peaks", 8);
        REGIONS.put("frozen-rift", 9);
    }

    private final HearthWorldPlugin plugin;

    public HearthWorldCommand(HearthWorldPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            player.sendMessage(ChatColor.AQUA + "❄ HearthWorld regions:");
            for (String name : REGIONS.keySet()) player.sendMessage(ChatColor.GRAY + " • " + name);
            player.sendMessage(ChatColor.YELLOW + "Use: /hw biome <region>");
            return true;
        }

        if (!args[0].equalsIgnoreCase("biome") || args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Use: /hw biome <region> or /hw list");
            return true;
        }

        String targetName = args[1].toLowerCase();
        Integer targetStyle = REGIONS.get(targetName);
        if (targetStyle == null) {
            player.sendMessage(ChatColor.RED + "Unknown winter region. Use /hw list.");
            return true;
        }

        World world = player.getWorld();
        if (!world.getName().equalsIgnoreCase(plugin.getConfig().getString("world.name", "world"))) {
            World configured = plugin.getServer().getWorld(plugin.getConfig().getString("world.name", "world"));
            if (configured != null) world = configured;
        }

        Location target = findRegion(world, targetStyle);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Couldn't find that region nearby.");
            return true;
        }

        player.teleport(target);
        player.sendMessage(ChatColor.AQUA + "❄ Teleported to " + ChatColor.WHITE + targetName + ChatColor.AQUA + ".");
        return true;
    }

    private Location findRegion(World world, int style) {
        long seed = world.getSeed();
        int originX = 0;
        int originZ = 0;

        for (int radius = 0; radius <= 20_000; radius += 2_000) {
            for (int sx = -1; sx <= 1; sx++) {
                for (int sz = -1; sz <= 1; sz++) {
                    if (radius == 0 && (sx != 0 || sz != 0)) continue;
                    int x = originX + sx * radius;
                    int z = originZ + sz * radius;
                    if (WinterChunkGenerator.regionStyle(seed, x, z) != style) continue;

                    int y = world.getHighestBlockYAt(x, z);
                    if (y < world.getMinHeight() + 2) continue;

                    Location loc = new Location(world, x + 0.5, y + 1.0, z + 0.5);
                    if (isSafe(loc)) return loc;
                }
            }
        }
        return null;
    }

    private boolean isSafe(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;
        return world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()).getType().isSolid()
                && world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).isEmpty()
                && world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ()).isEmpty();
    }
}
