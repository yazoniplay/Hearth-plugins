package dev.hearthsmp.world;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;

public final class WinterWorldListener implements Listener {
    private final HearthWorldPlugin plugin;

    public WinterWorldListener(HearthWorldPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (!isWinterWorld(event.getWorld())) return;
        applyWinterWeather(event.getWorld());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!isWinterWorld(event.getWorld())) return;
        winterizeChunk(event.getChunk());
    }

    public void applyWinterWeather(World world) {
        world.setStorm(true);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MAX_VALUE);
        world.setThunderDuration(Integer.MAX_VALUE);
    }

    private void winterizeChunk(Chunk chunk) {
        World world = chunk.getWorld();
        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;

        for (int x = baseX; x < baseX + 16; x++) {
            for (int z = baseZ; z < baseZ + 16; z++) {
                int y = world.getHighestBlockYAt(x, z);
                if (y <= world.getMinHeight()) continue;

                Block top = world.getBlockAt(x, y, z);
                if (top.getType() == Material.WATER && plugin.getConfig().getBoolean("generation.frozen-water", true)) {
                    top.setType(Material.ICE, false);
                    continue;
                }

                if (plugin.getConfig().getBoolean("generation.snow-cover", true)
                        && top.getType().isSolid()
                        && top.getType() != Material.ICE
                        && top.getType() != Material.PACKED_ICE
                        && top.getType() != Material.BLUE_ICE
                        && top.getType() != Material.SNOW) {
                    world.getBlockAt(x, y + 1, z).setType(Material.SNOW, false);
                }
            }
        }
    }

    private boolean isWinterWorld(World world) {
        return world.getName().equalsIgnoreCase(plugin.getConfig().getString("world.name", "world"));
    }
}
