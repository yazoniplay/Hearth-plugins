package dev.hearthsmp.world;

import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.List;
import java.util.Random;

public final class WinterChunkGenerator extends ChunkGenerator {
    private final FileConfiguration config;

    public WinterChunkGenerator(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return new WinterBiomeProvider();
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData data) {
        if (!config.getBoolean("generation.snow-cover", true)) return;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y = data.getHeight(HeightMap.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (y <= data.getMinHeight() || y >= data.getMaxHeight()) continue;

                Material top = data.getType(x, y - 1, z);
                if (top.isSolid() && top != Material.ICE && top != Material.PACKED_ICE && top != Material.BLUE_ICE) {
                    data.setBlock(x, y, z, Material.SNOW);
                }
            }
        }
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return List.of(new WinterPopulator());
    }

    @Override public boolean shouldGenerateNoise() { return true; }
    @Override public boolean shouldGenerateSurface() { return true; }
    @Override public boolean shouldGenerateCaves() { return true; }
    @Override public boolean shouldGenerateDecorations() { return true; }
    @Override public boolean shouldGenerateMobs() { return true; }
    @Override public boolean shouldGenerateStructures() { return true; }
}
