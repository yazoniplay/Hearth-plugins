package dev.hearthsmp.world;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class WinterBiomeProvider extends BiomeProvider {
    private static final List<Biome> BIOMES = List.of(
            Biome.SNOWY_TAIGA, Biome.SNOWY_PLAINS, Biome.ICE_SPIKES,
            Biome.FROZEN_PEAKS, Biome.JAGGED_PEAKS, Biome.SNOWY_SLOPES,
            Biome.GROVE, Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN,
            Biome.FROZEN_RIVER, Biome.SNOWY_BEACH
    );

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo info, int x, int y, int z) {
        int style = WinterChunkGenerator.regionStyle(info.getSeed(), x, z);
        double detail = WinterChunkGenerator.noise(info.getSeed() + 71237L, x, z, 0.008);

        return switch (style) {
            case 0 -> Biome.SNOWY_TAIGA;
            case 1 -> Biome.SNOWY_PLAINS;
            case 2 -> Biome.ICE_SPIKES;
            case 3 -> detail > 0 ? Biome.JAGGED_PEAKS : Biome.FROZEN_PEAKS;
            case 4 -> Biome.SNOWY_PLAINS;
            case 5 -> Biome.GROVE;
            case 6 -> y < 70 ? Biome.FROZEN_OCEAN : Biome.SNOWY_BEACH;
            case 7 -> Biome.FROZEN_RIVER;
            case 8 -> Biome.SNOWY_SLOPES;
            default -> Biome.ICE_SPIKES;
        };
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo info) {
        return BIOMES;
    }
}
