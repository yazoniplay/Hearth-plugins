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
        int min = data.getMinHeight();
        int max = data.getMaxHeight();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int x = (chunkX << 4) + lx;
                int z = (chunkZ << 4) + lz;
                int style = regionStyle(seed, x, z);
                int vanillaTop = data.getHeight(HeightMap.MOTION_BLOCKING_NO_LEAVES, lx, lz);

                double macro = noise(seed + 11L, x, z, 0.00055);
                double medium = noise(seed + 71_291L, x, z, 0.0022);
                double detail = noise(seed + 14_321L, x, z, 0.012);
                double ridge = Math.abs(noise(seed + 91_177L, x, z, 0.0048));

                double offset = switch (style) {
                    case 0 -> 7 + macro * 10 + medium * 6;                         // frozen taiga
                    case 1 -> -22 + macro * 5 + medium * 3;                       // whiteout plains
                    case 2 -> 18 + Math.max(0, macro) * 32 + ridge * 22;          // glacier fields
                    case 3 -> 35 + Math.max(0, macro) * 58 + Math.max(0, medium) * 28; // frost peaks
                    case 4 -> -4 + macro * 16 + detail * 8;                       // snowstorm plains
                    case 5 -> 2 + macro * 12 + medium * 8;                        // frozen grove
                    case 6 -> -42 + macro * 8 + medium * 4;                       // icebound coast
                    case 7 -> -8 + Math.max(0, macro) * 18 - Math.max(0, -medium) * 18; // frost valley
                    case 8 -> 24 + Math.max(0, macro) * 42 + ridge * 30;          // aurora peaks
                    case 9 -> -8 + macro * 10;                                     // frozen rift
                    case 10 -> 10 + Math.max(0, macro) * 20 + medium * 8;          // crystal tundra
                    case 11 -> -16 + macro * 9 + medium * 4;                      // frozen marsh
                    case 12 -> 3 + macro * 12 + detail * 12;                      // redwood snow forest
                    case 13 -> 18 + Math.max(0, macro) * 30 - Math.max(0, medium) * 8; // ice canyon
                    case 14 -> -30 + macro * 8;                                   // frozen basin
                    default -> 8 + Math.max(0, macro) * 25 + ridge * 12;           // skywood highlands
                };

                // The Rift: a huge, recognizable frozen scar rather than another mountain.
                if (style == 9) {
                    double rift = Math.abs(noise(seed + 330_001L, x, z, 0.0011));
                    if (rift < 0.16) offset -= 38 + (0.16 - rift) * 180;
                    else if (rift < 0.24) offset -= 18;
                }

                int target = Math.max(40, Math.min(210, (int) Math.round(vanillaTop + offset)));

                if ((style == 6 || style == 11) && target > 72) target = 72 + (int) Math.round(medium * 5);
                if (style == 1 || style == 11 || style == 14) target = Math.min(target, 82);

                if (target > vanillaTop) {
                    Material fill = switch (style) {
                        case 2, 3, 8 -> Material.PACKED_ICE;
                        case 9 -> Material.STONE;
                        default -> target > 115 ? Material.SNOW_BLOCK : Material.STONE;
                    };
                    for (int y = vanillaTop; y < Math.min(target, max - 1); y++) {
                        data.setBlock(lx, y, lz, fill);
                    }
                } else if (target < vanillaTop - 3) {
                    int clearTo = Math.max(target, min + 5);
                    for (int y = vanillaTop - 1; y >= clearTo; y--) {
                        data.setBlock(lx, y, lz, Material.AIR);
                    }
                }

                // Frozen rift floor and walls.
                if (style == 9) {
                    double rift = Math.abs(noise(seed + 330_001L, x, z, 0.0011));
                    if (rift < 0.12) {
                        int floor = Math.max(min + 8, target);
                        for (int y = floor; y < Math.min(floor + 4, max - 1); y++) {
                            data.setBlock(lx, y, lz, y == floor ? Material.BLUE_ICE : Material.PACKED_ICE);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData data) {
        if (!config.getBoolean("generation.snow-cover", true)) return;

        long seed = worldInfo.getSeed();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = (chunkX << 4) + x;
                int worldZ = (chunkZ << 4) + z;
                int style = regionStyle(seed, worldX, worldZ);
                int y = data.getHeight(HeightMap.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (y <= data.getMinHeight() || y >= data.getMaxHeight()) continue;

                Material top = data.getType(x, y - 1, z);
                if (!top.isSolid()) continue;

                if (style == 2 || style == 8) {
                    data.setBlock(x, y - 1, z, Material.ICE);
                    data.setBlock(x, y, z, Material.SNOW);
                } else if (style == 9) {
                    data.setBlock(x, y, z, Material.SNOW);
                } else if (top != Material.ICE && top != Material.PACKED_ICE && top != Material.BLUE_ICE) {
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

    public static int regionStyle(long seed, int x, int z) {
        int zx = Math.floorDiv(x, 2000);
        int zz = Math.floorDiv(z, 2000);
        long n = seed ^ (zx * 341873128712L) ^ (zz * 132897987541L);
        n ^= n >>> 33;
        n *= 0xff51afd7ed558ccdl;
        n ^= n >>> 33;
        return (int) Math.floorMod(n, 16);
    }

    public static double noise(long seed, int x, int z, double scale) {
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
        return n / (double) Long.MAX_VALUE;
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
