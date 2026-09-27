package dev.hearthsmp.world;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public final class WinterPopulator extends BlockPopulator {
    @Override
    @SuppressWarnings("deprecation")
    public void populate(World world, Random random, Chunk chunk) {
        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;

        freezeWater(world, baseX, baseZ);

        for (int i = 0; i < 20; i++) {
            int x = baseX + random.nextInt(16);
            int z = baseZ + random.nextInt(16);
            Block top = world.getHighestBlockAt(x, z);

            if (top.getType() == Material.SNOW && random.nextDouble() < 0.32) {
                placeSnowDrift(world, top, random);
            }

            if (top.getY() >= 70 && random.nextDouble() < 0.08) {
                placeIcicles(world, top, random);
            }
        }

        if (random.nextDouble() < 0.018) {
            generateStructure(world, random, baseX, baseZ);
        }
        if (random.nextDouble() < 0.035) {
            generateGlacier(world, random, baseX, baseZ);
        }
    }

    private void freezeWater(World world, int baseX, int baseZ) {
        for (int x = baseX; x < baseX + 16; x++) {
            for (int z = baseZ; z < baseZ + 16; z++) {
                int y = world.getHighestBlockYAt(x, z);
                Block block = world.getBlockAt(x, y, z);
                if (block.getType() == Material.WATER) block.setType(Material.ICE, false);
            }
        }
    }

    private void placeSnowDrift(World world, Block center, Random random) {
        int radius = 1 + random.nextInt(3);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) continue;
                Block base = world.getHighestBlockAt(center.getX() + dx, center.getZ() + dz);
                if (!base.getType().isSolid() && base.getType() != Material.SNOW) continue;
                int layers = 1 + random.nextInt(2);
                for (int h = 0; h < layers; h++) {
                    Block target = world.getBlockAt(base.getX(), base.getY() + 1 + h, base.getZ());
                    if (target.getType().isAir()) target.setType(Material.SNOW, false);
                }
            }
        }
    }

    private void placeIcicles(World world, Block center, Random random) {
        int length = 1 + random.nextInt(3);
        for (int i = 1; i <= length; i++) {
            Block target = world.getBlockAt(center.getX(), center.getY() - i, center.getZ());
            if (target.getType().isAir()) target.setType(Material.POINTED_DRIPSTONE, false);
            else break;
        }
    }

    private void generateStructure(World world, Random random, int baseX, int baseZ) {
        int x = baseX + 3 + random.nextInt(10);
        int z = baseZ + 3 + random.nextInt(10);
        int y = world.getHighestBlockYAt(x, z);
        if (y < 60 || !world.getBlockAt(x, y, z).getType().isSolid()) return;

        if (random.nextBoolean()) generateCabin(world, x, y + 1, z);
        else generateFrozenSpring(world, x, y + 1, z);
    }

    private void generateCabin(World world, int x, int y, int z) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = 0; dy < 4; dy++) {
                    boolean wall = Math.abs(dx) == 3 || Math.abs(dz) == 3 || dy == 0;
                    if (wall) world.getBlockAt(x + dx, y + dy, z + dz).setType(Material.SPRUCE_PLANKS, false);
                }
            }
        }

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 4) {
                    world.getBlockAt(x + dx, y + 4, z + dz).setType(Material.SPRUCE_SLAB, false);
                    world.getBlockAt(x + dx, y + 5, z + dz).setType(Material.SNOW, false);
                }
            }
        }

        world.getBlockAt(x, y + 1, z - 3).setType(Material.AIR, false);
        world.getBlockAt(x, y + 2, z - 3).setType(Material.AIR, false);
        world.getBlockAt(x - 2, y + 2, z - 3).setType(Material.GLASS_PANE, false);
        world.getBlockAt(x + 2, y + 2, z - 3).setType(Material.GLASS_PANE, false);
        world.getBlockAt(x, y + 1, z + 1).setType(Material.CAMPFIRE, false);
        world.getBlockAt(x, y + 1, z + 2).setType(Material.CHEST, false);
        world.getBlockAt(x, y + 1, z - 3).setType(Material.SPRUCE_DOOR, false);
    }

    private void generateGlacier(World world, Random random, int baseX, int baseZ) {
        int x = baseX + 2 + random.nextInt(12);
        int z = baseZ + 2 + random.nextInt(12);
        int y = world.getHighestBlockYAt(x, z);
        if (y < 55 || !world.getBlockAt(x, y, z).getType().isSolid()) return;

        int radius = 2 + random.nextInt(4);
        int height = 4 + random.nextInt(7);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius) continue;

                int columnHeight = Math.max(1, (int) (height * (1.0 - distance / (radius + 0.5))));
                for (int dy = 0; dy < columnHeight; dy++) {
                    Material material = dy > columnHeight - 2 ? Material.ICE : Material.PACKED_ICE;
                    world.getBlockAt(x + dx, y + dy + 1, z + dz).setType(material, false);
                }
            }
        }
    }

    private void generateFrozenSpring(World world, int x, int y, int z) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance <= 3.1) {
                    world.getBlockAt(x + dx, y - 1, z + dz).setType(Material.PACKED_ICE, false);
                    if (distance <= 2.1) world.getBlockAt(x + dx, y, z + dz).setType(Material.BLUE_ICE, false);
                }
            }
        }
        world.getBlockAt(x, y + 1, z).setType(Material.PACKED_ICE, false);
    }
}
