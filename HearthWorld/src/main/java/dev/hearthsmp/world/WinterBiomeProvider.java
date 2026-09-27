package dev.hearthsmp.world;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class WinterBiomeProvider extends BiomeProvider {
    private static final List<Biome> BIOMES = List.of(
            Biome.SNOWY_PLAINS,
            Biome.SNOWY_TAIGA,
            Biome.ICE_SPIKES,
            Biome.GROVE,
            Biome.SNOWY_SLOPES,
            Biome.FROZEN_PEAKS,
            Biome.JAGGED_PEAKS,
            Biome.FROZEN_OCEAN,
            Biome.DEEP_FROZEN_OCEAN,
            Biome.FROZEN_RIVER,
            Biome.SNOWY_BEACH
    );

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        double large = noise(worldInfo.getSeed(), x, z, 0.00075);
        double medium = noise(worldInfo.getSeed() + 99173L, x, z, 0.0025);
        double detail = noise(worldInfo.getSeed() + 71237L, x, z, 0.008);

        if (y < 50 && large < -0.58) {
            return medium > 0.15 ? Biome.DEEP_FROZEN_OCEAN : Biome.FROZEN_OCEAN;
        }
        if (large < -0.42 && medium < -0.35) return Biome.FROZEN_RIVER;
        if (large > 0.62) {
            if (medium > 0.20) return Biome.FROZEN_PEAKS;
            return detail > 0.0 ? Biome.JAGGED_PEAKS : Biome.SNOWY_SLOPES;
        }
        if (large > 0.25 && medium > 0.10) return Biome.GROVE;
        if (medium > 0.42) return Biome.SNOWY_TAIGA;
        if (medium < -0.48) return Biome.ICE_SPIKES;
        if (detail > 0.35) return Biome.SNOWY_BEACH;
        return Biome.SNOWY_PLAINS;
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return BIOMES;
    }

    private static double noise(long seed, int x, int z, double scale) {
        long a = Math.round(x * scale * 10000.0);
        long b = Math.round(z * scale * 10000.0);
        long n = seed ^ (a * 341873128712L) ^ (b * 132897987541L);
        n ^= n >>> 33;
        n *= 0xff51afd7ed558ccdl;
        n ^= n >>> 33;
        return (n / (double) Long.MAX_VALUE);
    }
}
