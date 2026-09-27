package dev.hearthsmp.world;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public final class HearthWorldPlugin extends JavaPlugin {
    private WinterWorldListener winterWorldListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        printStartupBanner();

        winterWorldListener = new WinterWorldListener(this);
        getServer().getPluginManager().registerEvents(winterWorldListener, this);

        WinterEvents winterEvents = new WinterEvents(this);
        getServer().getPluginManager().registerEvents(winterEvents, this);

        getCommand("hearthworld").setExecutor(new WinterBiomeCommand(this));

        World world = getServer().getWorld(getConfig().getString("world.name", "world"));
        if (world != null) {
            winterWorldListener.applyWinterWeather(world);
            startWinterAmbience(world);
            winterEvents.startBlizzards(world);
            getLogger().info("❄ Winter atmosphere attached to world: " + world.getName());
        } else {
            getLogger().warning("⚠ World '" + getConfig().getString("world.name", "world")
                    + "' is not loaded yet. Winter rules will apply when it loads.");
        }

        getLogger().info("🔥 HearthWorld enabled.");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new WinterChunkGenerator(getConfig());
    }

    private void printStartupBanner() {
        getLogger().info("");
        getLogger().info("############################################################");
        getLogger().info("#                                                          #");
        getLogger().info("#              ❄ HEARTH WORLD IS COLDING ❄               #");
        getLogger().info("#                                                          #");
        getLogger().info("#                 THE WINTER HAS ARRIVED                  #");
        getLogger().info("#                                                          #");
        getLogger().info("############################################################");
        getLogger().info("# World     : " + getConfig().getString("world.name", "world"));
        getLogger().info("# Generator : Hearth Winter Terrain");
        getLogger().info("# Biomes    : Frozen / Snowy / Alpine");
        getLogger().info("# Structures: Cabins / Glaciers / Springs");
        getLogger().info("# Events    : Blizzards / Discoveries / Scouts");
        getLogger().info("# Ambience  : Snowstorm / Aurora / Ice");
        getLogger().info("############################################################");
        getLogger().info("");
    }

    private void startWinterAmbience(World world) {
        if (!getConfig().getBoolean("ambience.aurora", true)) return;

        long interval = Math.max(20L, getConfig().getLong("ambience.aurora-interval-ticks", 200L));
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : world.getPlayers()) {
                if (player.getLocation().getY() < getConfig().getDouble("ambience.aurora-min-height", 90.0)) continue;
                if (ThreadLocalRandom.current().nextDouble() > 0.18) continue;

                double radius = 18.0;
                double x = player.getLocation().getX() + ThreadLocalRandom.current().nextDouble(-radius, radius);
                double y = player.getLocation().getY() + ThreadLocalRandom.current().nextDouble(5.0, 14.0);
                double z = player.getLocation().getZ() + ThreadLocalRandom.current().nextDouble(-radius, radius);

                player.spawnParticle(Particle.END_ROD, x, y, z, 3, 0.8, 0.25, 0.8, 0.01);
                player.spawnParticle(Particle.GLOW, x, y, z, 2, 0.6, 0.2, 0.6, 0.0);
            }
        }, interval, interval);
    }
}
