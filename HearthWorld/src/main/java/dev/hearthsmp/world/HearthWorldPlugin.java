package dev.hearthsmp.world;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public final class HearthWorldPlugin extends JavaPlugin {
    private WinterWorldListener winterWorldListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        winterWorldListener = new WinterWorldListener(this);
        getServer().getPluginManager().registerEvents(winterWorldListener, this);

        World world = getServer().getWorld(getConfig().getString("world.name", "world"));
        if (world != null && getConfig().getBoolean("ambience.permanent-storm", true)) {
            winterWorldListener.applyWinterWeather(world);
        }

        getLogger().info("HearthWorld enabled for world: " + getConfig().getString("world.name", "world"));
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new WinterChunkGenerator(getConfig());
    }
}
