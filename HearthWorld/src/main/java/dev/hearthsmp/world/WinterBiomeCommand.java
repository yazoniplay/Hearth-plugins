package dev.hearthsmp.world;

import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class WinterBiomeCommand implements CommandExecutor {
    private final HearthWorldPlugin plugin;

    public WinterBiomeCommand(HearthWorldPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("hearth.world.use")) {
            player.sendMessage("§cYou don't have permission to use HearthWorld.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage("§b§lHearthWorld §8• §fBiome Teleporter");
            player.sendMessage("§7/hearthworld biome <region>");
            player.sendMessage("§7Regions: §f" + String.join(", ", Region.valuesAsNames()));
            return true;
        }

        if (!args[0].equalsIgnoreCase("biome")) {
            player.sendMessage("§cUnknown subcommand. Use §f/hearthworld biome <region>§c.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cChoose a region: §f" + String.join(", ", Region.valuesAsNames()));
            return true;
        }

        Region region = Region.from(args[1]);
        if (region == null) {
            player.sendMessage("§cUnknown winter region. §7Try: §f" + String.join(", ", Region.valuesAsNames()));
            return true;
        }

        World world = player.getWorld();
        String configuredWorld = plugin.getConfig().getString("world.name", "world");
        if (!world.getName().equalsIgnoreCase(configuredWorld)) {
            world = plugin.getServer().getWorld(configuredWorld);
        }

        if (world == null) {
            player.sendMessage("§cThe HearthWorld world is not loaded.");
            return true;
        }

        player.sendMessage("§7Searching the frozen wilderness for §b" + region.displayName + "§7...");
        Location target = findRegion(world, region, player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (target == null) {
            player.sendMessage("§cCouldn't find that region nearby. Try again after generating more world.");
            return true;
        }

        player.teleport(target);
        player.sendMessage("§b❄ Teleported to §f" + region.displayName + "§b.");
        player.sendMessage("§7Coordinates: §f" + target.getBlockX() + ", " + target.getBlockY() + ", " + target.getBlockZ());
        return true;
    }

    private Location findRegion(World world, Region wanted, int originX, int originZ) {
        final int step = 128;
        final int radius = 6000;

        for (int distance = 0; distance <= radius; distance += step) {
            for (int dx = -distance; dx <= distance; dx += step) {
                int[] zs = distance == 0 ? new int[]{0} : new int[]{-distance, distance};
                for (int z : zs) {
                    Location found = check(world, wanted, originX + dx, originZ + z);
                    if (found != null) return found;
                }
            }

            for (int dz = -distance + step; dz < distance; dz += step) {
                int[] xs = new int[]{-distance, distance};
                for (int x : xs) {
                    Location found = check(world, wanted, originX + x, originZ + dz);
                    if (found != null) return found;
                }
            }
        }

        return null;
    }

    private Location check(World world, Region wanted, int x, int z) {
        Region actual = RegionResolver.resolve(world.getSeed(), x, z);
        if (actual != wanted) return null;

        int y = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
        if (y <= world.getMinHeight() + 2) return null;

        Location location = new Location(world, x + 0.5, y + 1.0, z + 0.5);
        location.setYaw(0);
        location.setPitch(0);
        return location;
    }

    enum Region {
        FROSTPEAK_MOUNTAINS("Frostpeak Mountains"),
        FROZEN_TAIGA("Frozen Taiga"),
        SNOWY_PINEWOOD("Snowy Pinewood"),
        GLACIER_FIELDS("Glacier Fields"),
        SNOWSTORM_PLAINS("Snowstorm Plains"),
        FROZEN_GROVE("Frozen Grove"),
        ICEBOUND_COAST("Icebound Coast"),
        FROST_VALLEY("Frost Valley"),
        AURORA_PEAKS("Aurora Peaks");

        final String displayName;

        Region(String displayName) {
            this.displayName = displayName;
        }

        static String[] valuesAsNames() {
            Region[] values = values();
            String[] names = new String[values.length];
            for (int i = 0; i < values.length; i++) names[i] = values[i].name().toLowerCase(Locale.ROOT).replace('_', '-');
            return names;
        }

        static Region from(String input) {
            String normalized = input.toLowerCase(Locale.ROOT).replace('-', '_');
            for (Region region : values()) {
                if (region.name().equalsIgnoreCase(normalized)) return region;
            }
            return null;
        }
    }

    static final class RegionResolver {
        static Region resolve(long seed, int x, int z) {
            double large = noise(seed, x, z, 0.00075);
            double medium = noise(seed + 99173L, x, z, 0.0025);
            double detail = noise(seed + 71237L, x, z, 0.008);

            if (large > 0.62) {
                if (medium > 0.20) return Region.AURORA_PEAKS;
                return detail > 0.0 ? Region.FROSTPEAK_MOUNTAINS : Region.AURORA_PEAKS;
            }
            if (large < -0.42 && medium < -0.35) return Region.FROST_VALLEY;
            if (large > 0.25 && medium > 0.10) return Region.FROZEN_GROVE;
            if (medium > 0.42) return Region.FROZEN_TAIGA;
            if (medium < -0.48) return Region.GLACIER_FIELDS;
            if (detail > 0.35) return Region.ICEBOUND_COAST;
            if (large > 0.05) return Region.SNOWY_PINEWOOD;
            return Region.SNOWSTORM_PLAINS;
        }

        private static double noise(long seed, int x, int z, double scale) {
            long a = Math.round(x * scale * 10000.0);
            long b = Math.round(z * scale * 10000.0);
            long n = seed ^ (a * 341873128712L) ^ (b * 132897987541L);
            n ^= n >>> 33;
            n *= 0xff51afd7ed558ccdl;
            n ^= n >>> 33;
            return n / (double) Long.MAX_VALUE;
        }
    }
}
