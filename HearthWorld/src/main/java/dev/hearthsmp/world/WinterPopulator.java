package dev.hearthsmp.world;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
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

        if (random.nextDouble() < 0.75) generateCabin(world, x, y + 1, z, random);
        else generateFrozenSpring(world, x, y + 1, z);
    }

    private void generateCabin(World world, int x, int y, int z, Random random) {
        // Cozy 9x9 cabin shell with a warm wooden interior.
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int dy = 0; dy < 5; dy++) {
                    boolean floor = dy == 0;
                    boolean wall = Math.abs(dx) == 4 || Math.abs(dz) == 4;
                    if (floor || wall) {
                        world.getBlockAt(x + dx, y + dy, z + dz).setType(
                                floor ? Material.SPRUCE_PLANKS : Material.SPRUCE_LOG, false);
                    } else {
                        world.getBlockAt(x + dx, y + dy, z + dz).setType(Material.AIR, false);
                    }
                }
            }
        }

        // Roof and snow cap.
        for (int layer = 0; layer < 4; layer++) {
            int radius = 4 - layer;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    world.getBlockAt(x + dx, y + 5 + layer, z + dz)
                            .setType(layer < 2 ? Material.SPRUCE_STAIRS : Material.SPRUCE_SLAB, false);
                    if (layer >= 2) {
                        world.getBlockAt(x + dx, y + 6 + layer, z + dz).setType(Material.SNOW, false);
                    }
                }
            }
        }

        // Front door + windows.
        world.getBlockAt(x, y + 1, z - 4).setType(Material.SPRUCE_DOOR, false);
        world.getBlockAt(x, y + 2, z - 4).setType(Material.SPRUCE_DOOR, false);
        world.getBlockAt(x - 2, y + 2, z - 4).setType(Material.GLASS_PANE, false);
        world.getBlockAt(x + 2, y + 2, z - 4).setType(Material.GLASS_PANE, false);
        world.getBlockAt(x - 4, y + 2, z).setType(Material.GLASS_PANE, false);
        world.getBlockAt(x + 4, y + 2, z).setType(Material.GLASS_PANE, false);

        // Interior fireplace, seating and lighting.
        world.getBlockAt(x, y + 1, z + 2).setType(Material.CAMPFIRE, false);
        world.getBlockAt(x, y + 2, z + 2).setType(Material.COBBLESTONE, false);
        world.getBlockAt(x - 2, y + 1, z + 1).setType(Material.SPRUCE_STAIRS, false);
        world.getBlockAt(x + 2, y + 1, z + 1).setType(Material.SPRUCE_STAIRS, false);
        world.getBlockAt(x - 3, y + 1, z - 1).setType(Material.LANTERN, false);
        world.getBlockAt(x + 3, y + 1, z - 1).setType(Material.LANTERN, false);

        // Bed and little bedside tables.
        world.getBlockAt(x - 2, y + 1, z - 2).setType(Material.SPRUCE_PLANKS, false);
        world.getBlockAt(x - 2, y + 1, z - 1).setType(Material.SPRUCE_PLANKS, false);
        world.getBlockAt(x - 1, y + 1, z - 2).setType(Material.RED_BED, false);
        world.getBlockAt(x - 1, y + 1, z - 1).setType(Material.RED_BED, false);

        // Decorative storage.
        world.getBlockAt(x + 2, y + 1, z - 2).setType(Material.BARREL, false);
        world.getBlockAt(x + 3, y + 1, z - 2).setType(Material.CRAFTING_TABLE, false);
        world.getBlockAt(x + 2, y + 1, z - 1).setType(Material.FURNACE, false);

        // Loot chest with useful early-game winter gear.
        Block chestBlock = world.getBlockAt(x + 1, y + 1, z - 2);
        chestBlock.setType(Material.CHEST, false);
        if (chestBlock.getState() instanceof Chest chest) {
            chest.getInventory().clear();
            chest.getInventory().addItem(new ItemStack(Material.BREAD, 3 + random.nextInt(4)));
            chest.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 2 + random.nextInt(3)));
            chest.getInventory().addItem(new ItemStack(Material.TORCH, 6 + random.nextInt(9)));
            chest.getInventory().addItem(new ItemStack(Material.SPRUCE_PLANKS, 8 + random.nextInt(13)));
            chest.getInventory().addItem(new ItemStack(Material.LEATHER, 2 + random.nextInt(5)));

            if (random.nextDouble() < 0.55) chest.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 1 + random.nextInt(3)));
            if (random.nextDouble() < 0.35) chest.getInventory().addItem(new ItemStack(Material.CHAINMAIL_HELMET));
            if (random.nextDouble() < 0.25) chest.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
            if (random.nextDouble() < 0.20) chest.getInventory().addItem(new ItemStack(Material.COMPASS));
            if (random.nextDouble() < 0.10) chest.getInventory().addItem(createFrostNote());
            chest.update();
        }
    }

    private ItemStack createFrostNote() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bFrostbound Note");
            meta.setLore(List.of("§7Someone lived here before the deep freeze.", "§eThe First Flame is still burning."));
            item.setItemMeta(meta);
        }
        return item;
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
