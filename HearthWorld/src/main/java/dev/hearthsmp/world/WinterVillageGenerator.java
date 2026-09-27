package dev.hearthsmp.world;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

/**
 * Original Hearth SMP winter village generator.
 * Designed around palettes and silhouettes inspired by cozy winter building
 * principles, without copying any third-party build or schematic.
 */
public final class WinterVillageGenerator {
    private WinterVillageGenerator() {}

    public static void tryGenerate(World world, int chunkX, int chunkZ) {
        long seed = world.getSeed();
        int cellX = Math.floorDiv(chunkX, 125);
        int cellZ = Math.floorDiv(chunkZ, 125);
        Random r = new Random(seed ^ (cellX * 341873128712L) ^ (cellZ * 132897987541L));

        // Roughly one village per selected 2000-block region, with deterministic placement.
        if (r.nextDouble() > 0.72) return;

        int style = WinterChunkGenerator.regionStyle(seed, cellX * 2000 + 1000, cellZ * 2000 + 1000);
        if (!villageStyle(style)) return;

        int villageX = cellX * 2000 + 1000 + r.nextInt(601) - 300;
        int villageZ = cellZ * 2000 + 1000 + r.nextInt(601) - 300;

        int villageChunkX = Math.floorDiv(villageX, 16);
        int villageChunkZ = Math.floorDiv(villageZ, 16);
        if (chunkX != villageChunkX || chunkZ != villageChunkZ) return;

        // Don't place villages too close to the world border.
        if (!world.getWorldBorder().isInside(world.getBlockAt(villageX, 64, villageZ).getLocation())) return;

        int groundY = world.getHighestBlockYAt(villageX, villageZ) + 1;
        if (groundY < 55 || groundY > 135) return;

        int palette = Math.floorMod(style + r.nextInt(5), 5);
        buildVillage(world, r, villageX, groundY, villageZ, palette, style);
    }

    private static boolean villageStyle(int style) {
        return style == 0 || style == 4 || style == 5 || style == 12 || style == 15;
    }

    private static void buildVillage(World w, Random r, int x, int y, int z, int palette, int style) {
        Material log = switch (palette) {
            case 1 -> Material.DARK_OAK_LOG;
            case 2 -> Material.BIRCH_LOG;
            case 3 -> Material.STRIPPED_SPRUCE_LOG;
            case 4 -> Material.DARK_OAK_LOG;
            default -> Material.SPRUCE_LOG;
        };
        Material plank = switch (palette) {
            case 1 -> Material.DARK_OAK_PLANKS;
            case 2 -> Material.BIRCH_PLANKS;
            case 3 -> Material.SPRUCE_PLANKS;
            case 4 -> Material.SPRUCE_PLANKS;
            default -> Material.SPRUCE_PLANKS;
        };
        Material roof = palette == 2 ? Material.DARK_OAK_PLANKS : Material.SPRUCE_PLANKS;
        Material stone = palette == 4 ? Material.POLISHED_ANDESITE : Material.COBBLESTONE;

        clearArea(w, x, y, z, 28, 24);

        // Central square, paths, lamps and a recognizable Hearth landmark.
        path(w, x, y - 1, z, 20, 20);
        well(w, x, y, z, stone);
        lamp(w, x + 7, y, z + 7);
        lamp(w, x - 7, y, z + 7);
        lamp(w, x + 7, y, z - 7);
        lamp(w, x - 7, y, z - 7);

        house(w, x - 14, ground(w, x - 14, z - 9), z - 9, r, log, plank, roof, stone, 1);
        house(w, x + 14, ground(w, x + 14, z - 9), z - 9, r, log, plank, roof, stone, 2);
        house(w, x - 14, ground(w, x - 14, z + 9), z + 9, r, log, plank, roof, stone, 3);
        house(w, x + 14, ground(w, x + 14, z + 9), z + 9, r, log, plank, roof, stone, 4);

        if (r.nextBoolean()) {
            house(w, x, ground(w, x, z - 18), z - 18, r, log, plank, roof, stone, 5);
        }

        lodge(w, x, ground(w, x, z + 18), z + 18, r, log, plank, roof, stone);
        blacksmith(w, x - 20, ground(w, x - 20, z), z, log, plank, roof, stone);
        barn(w, x + 20, ground(w, x + 20, z), z, log, plank, roof);
        watchtower(w, x - 22, ground(w, x - 22, z - 22), z - 22, log, roof, stone);

        if (style == 12 || style == 15) {
            bridge(w, x + 20, y, z + 10, log, plank);
        }

        shrine(w, x, ground(w, x, z), z, style);
        snowDecor(w, r, x, y, z);
    }

    private static int ground(World w, int x, int z) {
        return w.getHighestBlockYAt(x, z) + 1;
    }

    private static void clearArea(World w, int x, int y, int z, int rx, int rz) {
        for (int dx = -rx; dx <= rx; dx++) {
            for (int dz = -rz; dz <= rz; dz++) {
                for (int dy = 0; dy < 14; dy++) {
                    Block b = w.getBlockAt(x + dx, y + dy, z + dz);
                    if (b.getType() == Material.SNOW || b.getType() == Material.SNOW_BLOCK
                            || b.getType() == Material.LEAVES || b.getType().name().endsWith("_LEAVES")) {
                        b.setType(Material.AIR, false);
                    }
                }
            }
        }
    }

    private static void path(World w, int x, int y, int z, int rx, int rz) {
        for (int dx = -rx; dx <= rx; dx++) {
            for (int dz = -rz; dz <= rz; dz++) {
                if (Math.abs(dx) <= 1 || Math.abs(dz) <= 1) {
                    Block b = w.getBlockAt(x + dx, y, z + dz);
                    if (b.getType().isSolid()) b.setType(Material.PACKED_ICE, false);
                    w.getBlockAt(x + dx, y + 1, z + dz).setType(Material.SNOW, false);
                }
            }
        }
    }

    private static void house(World w, int x, int y, int z, Random r,
                               Material log, Material plank, Material roof, Material stone, int tier) {
        int sx = 5 + (tier % 2);
        int sz = 4;
        foundation(w, x, y, z, sx, sz, stone);

        for (int dx = -sx; dx <= sx; dx++) {
            for (int dz = -sz; dz <= sz; dz++) {
                boolean wall = Math.abs(dx) == sx || Math.abs(dz) == sz;
                for (int h = 1; h <= 4; h++) {
                    if (wall) w.getBlockAt(x + dx, y + h, z + dz).setType(h == 1 ? log : plank, false);
                    else w.getBlockAt(x + dx, y + h, z + dz).setType(Material.AIR, false);
                }
            }
        }

        // Heavy overhanging winter roof.
        for (int layer = 0; layer < 4; layer++) {
            int halfX = sx + 2 - layer;
            int halfZ = sz + 1;
            for (int dx = -halfX; dx <= halfX; dx++) {
                for (int dz = -halfZ; dz <= halfZ; dz++) {
                    w.getBlockAt(x + dx, y + 5 + layer, z + dz).setType(
                            layer < 2 ? roof : Material.SNOW_BLOCK, false);
                }
            }
        }

        // Doorway and windows.
        w.getBlockAt(x, y + 1, z - sz).setType(Material.AIR, false);
        w.getBlockAt(x, y + 2, z - sz).setType(Material.AIR, false);
        w.getBlockAt(x - sx, y + 2, z).setType(Material.GLASS_PANE, false);
        w.getBlockAt(x + sx, y + 2, z).setType(Material.GLASS_PANE, false);
        w.getBlockAt(x - 2, y + 2, z + sz).setType(Material.GLASS_PANE, false);
        w.getBlockAt(x + 2, y + 2, z + sz).setType(Material.GLASS_PANE, false);

        w.getBlockAt(x, y + 1, z).setType(Material.CRAFTING_TABLE, false);
        w.getBlockAt(x + 2, y + 1, z + 1).setType(Material.BARREL, false);
        w.getBlockAt(x - 2, y + 1, z + 1).setType(Material.LANTERN, false);
        w.getBlockAt(x + 1, y + 1, z - 1).setType(Material.CAMPFIRE, false);
        chest(w, r, x - 1, y + 1, z + 1, Math.min(4, tier));
        chimney(w, x + sx - 1, y + 1, z + 1, y + 7);
    }

    private static void foundation(World w, int x, int y, int z, int sx, int sz, Material stone) {
        for (int dx = -sx; dx <= sx; dx++) {
            for (int dz = -sz; dz <= sz; dz++) {
                w.getBlockAt(x + dx, y, z + dz).setType(stone, false);
            }
        }
    }

    private static void chimney(World w, int x, int y, int z, int top) {
        for (int yy = y; yy <= top; yy++) w.getBlockAt(x, yy, z).setType(Material.BRICKS, false);
        w.getBlockAt(x, top + 1, z).setType(Material.CAMPFIRE, false);
    }

    private static void lodge(World w, int x, int y, int z, Random r,
                              Material log, Material plank, Material roof, Material stone) {
        foundation(w, x, y, z, 7, 5, stone);
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                boolean wall = Math.abs(dx) == 7 || Math.abs(dz) == 5;
                for (int h = 1; h <= 5; h++) {
                    w.getBlockAt(x + dx, y + h, z + dz).setType(wall ? (h == 1 ? log : plank) : Material.AIR, false);
                }
            }
        }
        for (int layer = 0; layer < 5; layer++) {
            int half = 8 - layer;
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -6 + Math.min(layer, 2); dz <= 6 - Math.min(layer, 2); dz++) {
                    w.getBlockAt(x + dx, y + 6 + layer, z + dz).setType(layer < 2 ? roof : Material.SNOW_BLOCK, false);
                }
            }
        }
        w.getBlockAt(x, y + 1, z - 5).setType(Material.AIR, false);
        w.getBlockAt(x, y + 2, z - 5).setType(Material.AIR, false);
        w.getBlockAt(x - 4, y + 2, z - 5).setType(Material.GLASS_PANE, false);
        w.getBlockAt(x + 4, y + 2, z - 5).setType(Material.GLASS_PANE, false);
        w.getBlockAt(x, y + 1, z).setType(Material.CAMPFIRE, false);
        w.getBlockAt(x - 4, y + 1, z + 2).setType(Material.BOOKSHELF, false);
        w.getBlockAt(x + 4, y + 1, z + 2).setType(Material.BOOKSHELF, false);
        chest(w, r, x + 4, y + 1, z, 4);
        chimney(w, x - 5, y + 1, z + 2, y + 8);
    }

    private static void blacksmith(World w, int x, int y, int z, Material log,
                                    Material plank, Material roof, Material stone) {
        foundation(w, x, y, z, 5, 4, stone);
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                boolean wall = Math.abs(dx) == 5 || Math.abs(dz) == 4;
                for (int h = 1; h <= 4; h++) {
                    w.getBlockAt(x + dx, y + h, z + dz).setType(wall ? (h == 1 ? log : plank) : Material.AIR, false);
                }
            }
        }
        for (int dx = -6; dx <= 6; dx++) for (int dz = -5; dz <= 5; dz++)
            w.getBlockAt(x + dx, y + 5, z + dz).setType(Material.SNOW_BLOCK, false);

        w.getBlockAt(x - 2, y + 1, z).setType(Material.SMITHING_TABLE, false);
        w.getBlockAt(x + 1, y + 1, z).setType(Material.BLAST_FURNACE, false);
        w.getBlockAt(x + 3, y + 1, z + 1).setType(Material.LAVA_CAULDRON, false);
        w.getBlockAt(x, y + 1, z + 2).setType(Material.ANVIL, false);
    }

    private static void barn(World w, int x, int y, int z, Material log, Material plank, Material roof) {
        for (int dx = -5; dx <= 5; dx++) for (int dz = -4; dz <= 4; dz++) {
            boolean wall = Math.abs(dx) == 5 || Math.abs(dz) == 4;
            w.getBlockAt(x + dx, y, z + dz).setType(Material.SPRUCE_PLANKS, false);
            for (int h = 1; h <= 4; h++) if (wall) w.getBlockAt(x + dx, y + h, z + dz).setType(log, false);
        }
        for (int dx = -6; dx <= 6; dx++) for (int dz = -5; dz <= 5; dz++)
            w.getBlockAt(x + dx, y + 5, z + dz).setType(roof, false);
        w.getBlockAt(x, y + 1, z - 4).setType(Material.AIR, false);
        w.getBlockAt(x, y + 2, z - 4).setType(Material.AIR, false);
        w.getBlockAt(x - 3, y + 1, z).setType(Material.HAY_BLOCK, false);
        w.getBlockAt(x + 3, y + 1, z).setType(Material.HAY_BLOCK, false);
        w.getBlockAt(x, y + 1, z + 2).setType(Material.BARREL, false);
    }

    private static void watchtower(World w, int x, int y, int z, Material log, Material roof, Material stone) {
        for (int h = 0; h < 11; h++) {
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                w.getBlockAt(x + dx, y + h, z + dz).setType(h == 0 ? stone : log, false);
            }
        }
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            w.getBlockAt(x + dx, y + 11, z + dz).setType(roof, false);
        w.getBlockAt(x, y + 9, z).setType(Material.CHEST, false);
        w.getBlockAt(x - 2, y + 11, z).setType(Material.LANTERN, false);
        w.getBlockAt(x + 2, y + 11, z).setType(Material.LANTERN, false);
    }

    private static void well(World w, int x, int y, int z, Material stone) {
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            if (Math.abs(dx) == 2 || Math.abs(dz) == 2) w.getBlockAt(x + dx, y, z + dz).setType(stone, false);
            else w.getBlockAt(x + dx, y, z + dz).setType(Material.WATER, false);
        }
        for (int yy = 1; yy <= 3; yy++) {
            w.getBlockAt(x - 2, y + yy, z - 2).setType(Material.SPRUCE_LOG, false);
            w.getBlockAt(x + 2, y + yy, z - 2).setType(Material.SPRUCE_LOG, false);
            w.getBlockAt(x - 2, y + yy, z + 2).setType(Material.SPRUCE_LOG, false);
            w.getBlockAt(x + 2, y + yy, z + 2).setType(Material.SPRUCE_LOG, false);
        }
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            w.getBlockAt(x + dx, y + 4, z + dz).setType(Material.SNOW_BLOCK, false);
    }

    private static void lamp(World w, int x, int y, int z) {
        for (int h = 0; h < 3; h++) w.getBlockAt(x, y + h, z).setType(Material.SPRUCE_FENCE, false);
        w.getBlockAt(x, y + 3, z).setType(Material.LANTERN, false);
        w.getBlockAt(x, y + 4, z).setType(Material.SNOW, false);
    }

    private static void bridge(World w, int x, int y, int z, Material log, Material plank) {
        for (int i = -5; i <= 5; i++) {
            w.getBlockAt(x + i, y, z).setType(plank, false);
            w.getBlockAt(x + i, y + 1, z - 1).setType(log, false);
            w.getBlockAt(x + i, y + 1, z + 1).setType(log, false);
        }
        for (int i = -5; i <= 5; i += 2) w.getBlockAt(x + i, y + 2, z).setType(Material.SNOW, false);
    }

    private static void shrine(World w, int x, int y, int z, int style) {
        Material base = style == 15 ? Material.PACKED_ICE : Material.SNOW_BLOCK;
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            if (Math.abs(dx) == 2 || Math.abs(dz) == 2) w.getBlockAt(x + dx, y, z + dz).setType(base, false);
        w.getBlockAt(x, y + 1, z).setType(style == 12 ? Material.AMETHYST_BLOCK : Material.BLUE_ICE, false);
        w.getBlockAt(x, y + 2, z).setType(Material.SOUL_LANTERN, false);
    }

    private static void snowDecor(World w, Random r, int x, int y, int z) {
        for (int i = 0; i < 22; i++) {
            int dx = r.nextInt(45) - 22;
            int dz = r.nextInt(45) - 22;
            if (Math.abs(dx) < 5 && Math.abs(dz) < 5) continue;
            int gx = x + dx, gz = z + dz;
            int gy = w.getHighestBlockYAt(gx, gz) + 1;
            if (r.nextDouble() < 0.65) w.getBlockAt(gx, gy, gz).setType(Material.SNOW, false);
            if (r.nextDouble() < 0.12) w.getBlockAt(gx, gy + 1, gz).setType(Material.SNOW_BLOCK, false);
        }
    }

    private static void chest(World w, Random r, int x, int y, int z, int tier) {
        Block b = w.getBlockAt(x, y, z);
        b.setType(Material.CHEST, false);
        if (b.getState() instanceof Chest c) {
            c.getInventory().addItem(new ItemStack(Material.BREAD, 2 + r.nextInt(5)));
            c.getInventory().addItem(new ItemStack(Material.TORCH, 4 + r.nextInt(8)));
            if (tier >= 2) c.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 1 + r.nextInt(4)));
            if (tier >= 3) c.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
            if (tier >= 4) c.getInventory().addItem(new ItemStack(Material.EMERALD, 1 + r.nextInt(3)));
            ItemStack note = new ItemStack(Material.PAPER);
            ItemMeta meta = note.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§bHearth Village Note");
                meta.setLore(List.of("§7A warm light survives the snow.", "§eThe First Flame remembers."));
                note.setItemMeta(meta);
            }
            c.getInventory().addItem(note);
            c.update();
        }
    }
}
