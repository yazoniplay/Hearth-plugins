package dev.hearthsmp.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public final class HearthWorldPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("HearthWorld is ready. Winter generation is available.");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new WinterChunkGenerator(getConfig());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("hearthworld")) return false;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§6§l🔥 HearthWorld §8»");
            sender.sendMessage("§7/hearthworld create §fCreate the winter world");
            sender.sendMessage("§7/hearthworld info §fShow winter generation info");
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            sender.sendMessage("§6§l🔥 HearthWorld §8» §fPermanent Winter");
            sender.sendMessage("§7Winter regions: §fFrozen Taiga, Snowy Pinewood, Glacier Fields, Frostpeak Mountains, Snowstorm Plains, Frozen Grove, Icebound Coast, Frost Valley, Aurora Peaks");
            sender.sendMessage("§7Generation: §fVanilla terrain + Hearth winter layer + structures");
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (!sender.hasPermission("hearth.world.create")) {
                sender.sendMessage("§cYou do not have permission.");
                return true;
            }

            String worldName = getConfig().getString("world.name", "hearth_winter");
            World existing = Bukkit.getWorld(worldName);
            if (existing != null) {
                sender.sendMessage("§eThe winter world already exists: §f" + worldName);
                return true;
            }

            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(World.Environment.NORMAL);
            creator.generateStructures(true);
            creator.generator(new WinterChunkGenerator(getConfig()));
            creator.biomeProvider(new WinterBiomeProvider());

            World world = creator.createWorld();
            if (world == null) {
                sender.sendMessage("§cFailed to create the winter world.");
                return true;
            }

            world.setStorm(true);
            world.setThundering(false);
            world.setWeatherDuration(Integer.MAX_VALUE);
            world.setThunderDuration(Integer.MAX_VALUE);

            sender.sendMessage("§a❄ Winter world created: §f" + world.getName());
            return true;
        }

        sender.sendMessage("§cUnknown subcommand. Use §f/hearthworld help");
        return true;
    }
}
