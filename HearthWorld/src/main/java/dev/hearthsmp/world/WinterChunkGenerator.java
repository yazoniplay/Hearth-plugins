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
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData data) {
        if (!config.getBoolean("generation.terrain-sculpting", true)) return;

        long seed = worldInfo.getSeed();
        int minHeight = data.getMinHeight();
        int maxHeight = data.getMaxHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = (chunkX << 4) + x;
                int worldZ = (chunkZ << 4) + z;

                int vanillaTop = data.getHeight(HeightMap.MOTION_BLOCKING_NO_LEAVES, x, z);
                int target = winterHeight(seed, worldX, worldZ, vanillaTop);

                if (target > vanillaTop) {
                    Material fill = target > 115 ? Material.SNOW_BLOCK : Material.STONE;
                    for (int y = vanillaTop; y < Math.min(target, maxHeight - 1); y++) {
                        data.setBlock(x, y, z, fill);
                    }
                } else if (target < vanillaTop - 4) {
                    int clearTo = Math.max(target, minHeight + 5);
                    for (int y = vanillaTop - 1; y >= clearTo; y--) {
                        data.setBlock(x, y, z, Material.AIR);
                    }
                }
            }
        }
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

    @Override
    public boolean shouldGenerateNoise() { return true; }

    @Override
    public boolean shouldGenerateSurface() { return true; }

    @Override
    public boolean shouldGenerateCaves() { return true; }

    @Override
    public boolean shouldGenerateDecorations() { return true; }

    @Override
    public boolean shouldGenerateMobs() { return true; }

    @Override
    public boolean shouldGenerateStructures() { return true; }

    private static int winterHeight(long seed, int x, int z, int vanillaTop) {
        double continental = smoothNoise(seed, x, z, 0.0012);
        double mountains = smoothNoise(seed + 71_291L, x, z, 0.0035);
        double ridges = Math.abs(smoothNoise(seed + 91_177L, x, z, 0.0065));
        double detail = smoothNoise(seed + 14_321L, x, z, 0.018);

        double mountain = Math.max(0.0, continental - 0.18) * 85.0;
        mountain += Math.max(0.0, mountains - 0.18) * 38.0;
        mountain += Math.max(0.0, ridges - 0.55) * 30.0;

        double valley = Math.max(0.0, -continental - 0.28) * 32.0;
        double rolling = detail * 7.0;

        int target = (int) Math.round(vanillaTop + mountain - valley + rolling);
        return Math.max(48, Math.min(190, target));
    }

    private static double smoothNoise(long seed, int x, int z, double scale) {
        double gx = x * scale;
        double gz = z * scale;

        int x0 = (int) Math.floor(gx);
        int z0 = (int) Math.floor(gz);
        double fx = gx - x0;
        double fz = gz - z0;

        double a = hash(seed, x0, z0);
        double b = hash(seed, x0 + 1, z0);
        double c = hash(seed, x0, z0 + 1);
        double d = hash(seed, x0 + 1, z0 + 1);

        double u = fx * fx * (3.0 - 2.0 * fx);
        double v = fz * fz * (3.0 - 2.0 * fz);

        return lerp(lerp(a, b, u), lerp(c, d, u), v);
    }

    private static double hash(long seed, int x, int z) {
        long n = seed ^ (x * 341873128712L) ^ (z * 132897987541L);
        n ^= n >>> 33;
        n *= 0xff51afd7ed558ccdl;
        n ^= n >>> 33;
        return (n / (double) Long.MAX_VALUE);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
