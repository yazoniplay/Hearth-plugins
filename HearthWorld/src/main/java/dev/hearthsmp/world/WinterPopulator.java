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

public final class WinterPopulator extends BlockPopulator {\n    // Prevent recursive chunk-population when decorations touch neighboring chunks.\n    private static final ThreadLocal<Boolean> POPULATING = ThreadLocal.withInitial(() -> false);
    @Override
    public void populate(World world, Random random, Chunk chunk) {
        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        long seed = world.getSeed();
        int style = WinterChunkGenerator.regionStyle(seed, baseX + 8, baseZ + 8);

        freezeWater(world, baseX, baseZ);

        // Large deterministic villages anchor the winter regions.
        WinterVillageGenerator.tryGenerate(world, chunk.getX(), chunk.getZ());

        // Every region gets its own vegetation/landmark language.
        switch (style) {
            case 0 -> frozenTaiga(world, random, baseX, baseZ);
            case 1 -> whiteoutPlains(world, random, baseX, baseZ);
            case 2 -> glacierFields(world, random, baseX, baseZ);
            case 3 -> frostPeaks(world, random, baseX, baseZ);
            case 4 -> snowstormPlains(world, random, baseX, baseZ);
            case 5 -> frozenGrove(world, random, baseX, baseZ);
            case 6 -> iceboundCoast(world, random, baseX, baseZ);
            case 7 -> frostValley(world, random, baseX, baseZ);
            case 8 -> auroraPeaks(world, random, baseX, baseZ);
            case 10 -> crystalTundra(world, random, baseX, baseZ);
            case 11 -> frozenMarsh(world, random, baseX, baseZ);
            case 12 -> redwoodSnowForest(world, random, baseX, baseZ);
            case 13 -> iceCanyon(world, random, baseX, baseZ);
            case 14 -> frozenBasin(world, random, baseX, baseZ);
            case 15 -> skywoodHighlands(world, random, baseX, baseZ);
            default -> frozenRift(world, random, baseX, baseZ);
        }

        if (random.nextDouble() < 0.045) generateLandmark(world, random, baseX, baseZ, style);
        if (random.nextDouble() < 0.035) generateGlacier(world, random, baseX, baseZ);

        // Small details make the world feel inhabited instead of procedurally empty.
        if (random.nextDouble() < 0.22) generateCozyDetail(world, random, baseX, baseZ, style);
        if (random.nextDouble() < 0.08) generateFrozenPond(world, random, baseX + 8, baseZ + 8);
        if (random.nextDouble() < 0.035) generateSnowyBridge(world, random, baseX + 8, baseZ + 8);
        if (random.nextDouble() < 0.018) generateChristmasClearing(world, random, baseX + 8, baseZ + 8);
    }

        } finally {\n            POPULATING.set(false);\n        }\n    }\n\n    private void frozenTaiga(World w, Random r, int bx, int bz) {
        for (int i = 0; i < 2 + r.nextInt(3); i++) ancientSpruceWinterTree(w, r, bx + r.nextInt(16), bz + r.nextInt(16));
        for (int i = 0; i < 2 + r.nextInt(3); i++) spruce(w, r, bx + r.nextInt(16), bz + r.nextInt(16), 7 + r.nextInt(5), 3);
        if (r.nextDouble() < 0.45) frostForestMix(w,r,bx,bz);
    }

    private void whiteoutPlains(World w, Random r, int bx, int bz) {
        if (r.nextDouble() < 0.28) snowPillar(w, bx + r.nextInt(16), bz + r.nextInt(16), 3 + r.nextInt(5));
        if (r.nextDouble() < 0.18) iceSpike(w, bx + r.nextInt(16), bz + r.nextInt(16), 5 + r.nextInt(9));
    }

    private void glacierFields(World w, Random r, int bx, int bz) {
        for (int i = 0; i < 2 + r.nextInt(3); i++) iceSpike(w, bx + r.nextInt(16), bz + r.nextInt(16), 6 + r.nextInt(14));
        if (r.nextDouble() < 0.30) iceArch(w, bx + 8, bz + 8);
    }

    private void frostPeaks(World w, Random r, int bx, int bz) {
        if (r.nextDouble() < 0.22) frozenCaveMouth(w, bx + 8, bz + 8);
        if (r.nextDouble() < 0.12) iceSpike(w, bx + 8, bz + 8, 12 + r.nextInt(14));
    }

    private void snowstormPlains(World w, Random r, int bx, int bz) {
        for (int i = 0; i < 10; i++) {
            Block top = w.getHighestBlockAt(bx + r.nextInt(16), bz + r.nextInt(16));
            snowDrift(w, top, r);
        }
    }

    private void frozenGrove(World w, Random r, int bx, int bz) {
        for (int i = 0; i < 2 + r.nextInt(4); i++) crookedPine(w, r, bx + r.nextInt(16), bz + r.nextInt(16));
        if (r.nextDouble() < 0.65) frostForestMix(w,r,bx,bz);
        if (r.nextDouble() < 0.15) mushroomGrove(w, bx + 8, bz + 8);
    }

    private void iceboundCoast(World w, Random r, int bx, int bz) {
        for (int i = 0; i < 2 + r.nextInt(3); i++) iceSpike(w, bx + r.nextInt(16), bz + r.nextInt(16), 3 + r.nextInt(7));
        if (r.nextDouble() < 0.10) shipwreck(w, bx + 8, bz + 8);
    }

    private void frostValley(World w, Random r, int bx, int bz) {
        if (r.nextDouble() < 0.20) frozenWaterfall(w, bx + 8, bz + 8);
        if (r.nextDouble() < 0.14) ruin(w, r, bx + 8, bz + 8, 3);
    }

    private void auroraPeaks(World w, Random r, int bx, int bz) {
        if (r.nextDouble() < 0.18) crystalShrine(w, bx + 8, bz + 8);
        if (r.nextDouble() < 0.10) iceSpike(w, bx + 8, bz + 8, 16 + r.nextInt(12));
    }

    private void frozenRift(World w, Random r, int bx, int bz) {
        // Blue-ice teeth around the rift make it visually obvious where the scar is.
        if (r.nextDouble() < 0.35) {
            int x = bx + 4 + r.nextInt(8), z = bz + 4 + r.nextInt(8);
            int y = w.getHighestBlockYAt(x, z) + 1;
            for (int h = 0; h < 4 + r.nextInt(7); h++) {
                w.getBlockAt(x, y + h, z).setType(h > 2 ? Material.BLUE_ICE : Material.PACKED_ICE, false);
            }
        }
        if (r.nextDouble() < 0.10) riftShrine(w, bx + 8, bz + 8);
    }


    private void crystalTundra(World w, Random r, int bx, int bz) {
        for (int i = 0; i < 3 + r.nextInt(4); i++) amethystPine(w, r, bx + r.nextInt(16), bz + r.nextInt(16));
        if (r.nextDouble() < 0.22) crystalShrine(w, bx + 8, bz + 8);
        if (r.nextDouble() < 0.28) warpedFrostTree(w, r, bx + r.nextInt(16), bz + r.nextInt(16));
    }

    private void frozenMarsh(World w, Random r, int bx, int bz) {
        if (r.nextDouble() < 0.30) mangroveWinterTree(w, r, bx + r.nextInt(16), bz + r.nextInt(16));
        for (int i = 0; i < 5; i++) {
            int x = bx + r.nextInt(16), z = bz + r.nextInt(16);
            int y = w.getHighestBlockYAt(x, z);
            w.getBlockAt(x, y, z).setType(Material.PACKED_ICE, false);
            w.getBlockAt(x, y + 1, z).setType(Material.SNOW, false);
            if (r.nextBoolean()) w.getBlockAt(x, y + 2, z).setType(Material.DEAD_BUSH, false);
        }
        if (r.nextDouble() < 0.12) frozenWaterfall(w, bx + 8, bz + 8);
    }

    private void redwoodSnowForest(World w, Random r, int bx, int bz) {
        for (int i = 0; i < 2 + r.nextInt(3); i++) giantDarkSpruce(w, r, bx + r.nextInt(16), bz + r.nextInt(16));
        if (r.nextDouble() < 0.12) lodge(w, r, bx + 8, bz + 8);
    }

    private void iceCanyon(World w, Random r, int bx, int bz) {
        for (int i = 0; i < 2; i++) iceSpike(w, bx + r.nextInt(16), bz + r.nextInt(16), 10 + r.nextInt(12));
        if (r.nextDouble() < 0.14) iceArch(w, bx + 8, bz + 8);
    }

    private void frozenBasin(World w, Random r, int bx, int bz) {
        for (int i = 0; i < 4; i++) {
            int x = bx + r.nextInt(16), z = bz + r.nextInt(16);
            snowDrift(w, w.getHighestBlockAt(x, z), r);
        }
        if (r.nextDouble() < 0.08) ruin(w, r, bx + 8, bz + 8, 4);
    }

    private void skywoodHighlands(World w, Random r, int bx, int bz) {
        for (int i = 0; i < 2 + r.nextInt(3); i++) birchFrostTree(w, r, bx + r.nextInt(16), bz + r.nextInt(16));
        if (r.nextDouble() < 0.22) mangroveWinterTree(w, r, bx + r.nextInt(16), bz + r.nextInt(16));
        if (r.nextDouble() < 0.10) tower(w, r, bx + 8, w.getHighestBlockYAt(bx + 8, bz + 8) + 1, bz + 8);
    }


    // Original organic tree silhouettes inspired by common Minecraft landscaping
    // principles: layered canopies, irregular trunks, branch variation and scale.
    private void warpedFrostTree(World w, Random r, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        int h = 8 + r.nextInt(6);
        int lean = r.nextInt(3) - 1;
        for (int i = 0; i < h; i++) {
            int tx = x + (i > h / 2 ? lean : 0);
            w.getBlockAt(tx, y + i, z).setType(Material.WARPED_STEM, false);
            if (i > 2 && i % 2 == 0) {
                w.getBlockAt(tx + 1, y + i, z).setType(Material.WARPED_STEM, false);
                w.getBlockAt(tx - 1, y + i, z).setType(Material.WARPED_STEM, false);
            }
        }
        for (int layer = 0; layer < h - 2; layer += 2) {
            int rad = Math.max(1, 4 - layer / 4);
            int cy = y + h - layer;
            for (int dx = -rad; dx <= rad; dx++) for (int dz = -rad; dz <= rad; dz++) {
                if (dx * dx + dz * dz <= rad * rad + 1) {
                    w.getBlockAt(x + dx + lean, cy, z + dz).setType(Material.WARPED_WART_BLOCK, false);
                }
            }
        }
        w.getBlockAt(x + lean, y + h + 1, z).setType(Material.SNOW, false);
    }

    private void mangroveWinterTree(World w, Random r, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        int h = 7 + r.nextInt(5);
        for (int i = 0; i < h; i++) {
            int tx = x + (i > 2 ? (i % 3) - 1 : 0);
            w.getBlockAt(tx, y + i, z).setType(Material.MANGROVE_LOG, false);
            if (i == 2 || i == 4) {
                w.getBlockAt(tx + 1, y + i, z).setType(Material.MANGROVE_ROOTS, false);
                w.getBlockAt(tx - 1, y + i, z).setType(Material.MANGROVE_ROOTS, false);
            }
        }
        // Exposed roots make this useful in frozen marshes and river edges.
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
            if (Math.abs(dx) + Math.abs(dz) <= 3 && r.nextDouble() < 0.55) {
                w.getBlockAt(x + dx, y, z + dz).setType(Material.MANGROVE_ROOTS, false);
            }
        }
        int crownY = y + h - 1;
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            int d = dx * dx + dz * dz;
            if (d <= 16 && r.nextDouble() < 0.78) {
                w.getBlockAt(x + dx, crownY + r.nextInt(3), z + dz).setType(Material.MANGROVE_LEAVES, false);
            }
        }
        w.getBlockAt(x, crownY + 3, z).setType(Material.SNOW, false);
    }

    private void oakFrostGiant(World w, Random r, int x, int z) {
        deciduousTree(w, r, x, z, Material.OAK_LOG, Material.OAK_LEAVES, 9 + r.nextInt(7), 4, true);
    }

    private void jungleFrostGiant(World w, Random r, int x, int z) {
        deciduousTree(w, r, x, z, Material.JUNGLE_LOG, Material.JUNGLE_LEAVES, 11 + r.nextInt(7), 5, true);
    }

    private void acaciaFrostTree(World w, Random r, int x, int z) {
        int y=w.getHighestBlockYAt(x,z)+1, h=7+r.nextInt(5);
        for(int i=0;i<h;i++) w.getBlockAt(x+(i>h/2?i%3-1:0),y+i,z).setType(Material.ACACIA_LOG,false);
        for(int dx=-4;dx<=4;dx++) for(int dz=-4;dz<=4;dz++)
            if(dx*dx+dz*dz<=16 && r.nextDouble()<.8) w.getBlockAt(x+dx,y+h-1+r.nextInt(2),z+dz).setType(Material.ACACIA_LEAVES,false);
        w.getBlockAt(x,y+h,z).setType(Material.SNOW,false);
    }

    private void cherryFrostTree(World w, Random r, int x, int z) {
        deciduousTree(w, r, x, z, Material.CHERRY_LOG, Material.CHERRY_LEAVES, 8 + r.nextInt(6), 4, true);
    }

    private void paleFrostTree(World w, Random r, int x, int z) {
        deciduousTree(w, r, x, z, Material.PALE_OAK_LOG, Material.PALE_OAK_LEAVES, 10 + r.nextInt(7), 5, true);
    }

    private void deciduousTree(World w, Random r, int x, int z, Material log, Material leaves, int h, int maxRadius, boolean snow) {
        int y=w.getHighestBlockYAt(x,z)+1;
        for(int i=0;i<h;i++) {
            int tx=x+(i>h/2?(i%3)-1:0), tz=z+(i>h/2?((i+1)%3)-1:0);
            w.getBlockAt(tx,y+i,tz).setType(log,false);
            if(i>3 && i%3==0) {
                w.getBlockAt(tx+1,y+i,tz).setType(log,false);
                w.getBlockAt(tx-1,y+i,tz).setType(log,false);
            }
        }
        for(int layer=2;layer<h;layer+=2) {
            int radius=Math.max(1,maxRadius-layer/4), cy=y+h-layer;
            for(int dx=-radius;dx<=radius;dx++) for(int dz=-radius;dz<=radius;dz++) {
                if(dx*dx+dz*dz<=radius*radius+1) {
                    w.getBlockAt(x+dx,cy,z+dz).setType(leaves,false);
                    if(snow && r.nextDouble()<.22) w.getBlockAt(x+dx,cy+1,z+dz).setType(Material.SNOW,false);
                }
            }
        }
    }

    private void frostForestMix(World w, Random r, int bx, int bz) {
        int count=3+r.nextInt(4);
        for(int i=0;i<count;i++) {
            int x=bx+r.nextInt(16), z=bz+r.nextInt(16);
            switch(r.nextInt(8)) {
                case 0,1 -> oakFrostGiant(w,r,x,z);
                case 2 -> birchFrostTree(w,r,x,z);
                case 3 -> ancientSpruceWinterTree(w,r,x,z);
                case 4 -> giantDarkSpruce(w,r,x,z);
                case 5 -> cherryFrostTree(w,r,x,z);
                case 6 -> paleFrostTree(w,r,x,z);
                default -> acaciaFrostTree(w,r,x,z);
            }
        }
    }

    private void ancientSpruceWinterTree(World w, Random r, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        int h = 14 + r.nextInt(8);
        for (int i = 0; i < h; i++) {
            int tx = x + (i > 5 ? (i % 3) - 1 : 0);
            w.getBlockAt(tx, y + i, z).setType(Material.SPRUCE_LOG, false);
        }
        for (int layer = 2; layer < h; layer += 2) {
            int rad = Math.max(1, 6 - layer / 3);
            int cy = y + h - layer;
            for (int dx = -rad; dx <= rad; dx++) for (int dz = -rad; dz <= rad; dz++) {
                if (dx * dx + dz * dz <= rad * rad) {
                    w.getBlockAt(x + dx, cy, z + dz).setType(Material.SPRUCE_LEAVES, false);
                    if (r.nextDouble() < 0.18) w.getBlockAt(x + dx, cy + 1, z + dz).setType(Material.SNOW, false);
                }
            }
        }
        w.getBlockAt(x, y + h, z).setType(Material.SNOW_BLOCK, false);
    }

    private void amethystPine(World w, Random r, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        int h = 5 + r.nextInt(4);
        for (int i = 0; i < h; i++) w.getBlockAt(x, y + i, z).setType(Material.DARK_OAK_LOG, false);
        for (int i = 1; i < h; i++) {
            int rad = Math.max(1, 3 - i / 3);
            for (int dx = -rad; dx <= rad; dx++) for (int dz = -rad; dz <= rad; dz++)
                if (dx * dx + dz * dz <= rad * rad) w.getBlockAt(x + dx, y + h - i, z + dz).setType(Material.AMETHYST_BLOCK, false);
        }
        w.getBlockAt(x, y + h, z).setType(Material.SNOW, false);
    }

    private void giantDarkSpruce(World w, Random r, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        int h = 10 + r.nextInt(7);
        for (int i = 0; i < h; i++) w.getBlockAt(x, y + i, z).setType(Material.DARK_OAK_LOG, false);
        for (int i = 2; i < h; i++) {
            int rad = Math.max(1, 5 - i / 3);
            for (int dx = -rad; dx <= rad; dx++) for (int dz = -rad; dz <= rad; dz++)
                if (dx * dx + dz * dz <= rad * rad) w.getBlockAt(x + dx, y + h - i, z + dz).setType(Material.DARK_OAK_LEAVES, false);
        }
    }

    private void birchFrostTree(World w, Random r, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        int h = 6 + r.nextInt(4);
        for (int i = 0; i < h; i++) w.getBlockAt(x, y + i, z).setType(Material.BIRCH_LOG, false);
        for (int i = 2; i < h; i++) {
            int rad = Math.max(1, 3 - i / 3);
            for (int dx = -rad; dx <= rad; dx++) for (int dz = -rad; dz <= rad; dz++)
                if (dx * dx + dz * dz <= rad * rad) w.getBlockAt(x + dx, y + h - i, z + dz).setType(Material.BIRCH_LEAVES, false);
        }
    }

    private void lodge(World w, Random r, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        for (int dx = -5; dx <= 5; dx++) for (int dz = -4; dz <= 4; dz++) {
            boolean floor = Math.abs(dx) <= 4 && Math.abs(dz) <= 3;
            boolean wall = Math.abs(dx) == 5 || Math.abs(dz) == 4;
            w.getBlockAt(x + dx, y, z + dz).setType(floor ? Material.DARK_OAK_PLANKS : wall ? Material.DARK_OAK_LOG : Material.AIR, false);
        }
        for (int h = 1; h <= 4; h++) {
            for (int dx = -5 + h; dx <= 5 - h; dx++) for (int dz = -4 + h; dz <= 4 - h; dz++)
                w.getBlockAt(x + dx, y + h, z + dz).setType(Material.SPRUCE_PLANKS, false);
        }
        w.getBlockAt(x, y + 1, z).setType(Material.CAMPFIRE, false);
        w.getBlockAt(x - 3, y + 1, z - 3).setType(Material.BARREL, false);
        w.getBlockAt(x + 3, y + 1, z - 3).setType(Material.CHEST, false);
        chest(w, r, x + 3, y + 2, z - 3, 4);
    }

    private void generateCozyDetail(World w, Random r, int bx, int bz, int style) {
        int x=bx+2+r.nextInt(12), z=bz+2+r.nextInt(12);
        int y=w.getHighestBlockYAt(x,z)+1;
        switch(r.nextInt(8)) {
            case 0 -> {
                w.getBlockAt(x,y,z).setType(Material.CAMPFIRE,false);
                for(int i=0;i<4;i++) {
                    int dx=(i%2==0?1:-1)*(2+(i/2));
                    w.getBlockAt(x+dx,y,z).setType(Material.SPRUCE_LOG,false);
                }
                w.getBlockAt(x,y+2,z).setType(Material.SNOW,false);
            }
            case 1 -> {
                w.getBlockAt(x,y,z).setType(Material.BARREL,false);
                w.getBlockAt(x+1,y,z).setType(Material.SPRUCE_PLANKS,false);
                w.getBlockAt(x,y+1,z).setType(Material.LANTERN,false);
            }
            case 2 -> {
                for(int i=-2;i<=2;i++) {
                    w.getBlockAt(x+i,y,z).setType(Material.SNOW_BLOCK,false);
                    if(Math.abs(i)<2) w.getBlockAt(x+i,y+1,z).setType(Material.SNOW,false);
                }
                w.getBlockAt(x,y+1,z).setType(Material.SPRUCE_FENCE,false);
                w.getBlockAt(x,y+2,z).setType(Material.LANTERN,false);
            }
            case 3 -> generateLogPile(w,x,y,z);
            case 4 -> {
                w.getBlockAt(x,y,z).setType(Material.POWDER_SNOW,false);
                w.getBlockAt(x+1,y,z).setType(Material.ICE,false);
                w.getBlockAt(x-1,y,z).setType(Material.SNOW,false);
            }
            case 5 -> generateSnowyRock(w,r,x,y,z);
            case 6 -> {
                w.getBlockAt(x,y,z).setType(Material.SPRUCE_PLANKS,false);
                w.getBlockAt(x+1,y,z).setType(Material.SPRUCE_PLANKS,false);
                w.getBlockAt(x,y+1,z).setType(Material.LANTERN,false);
                w.getBlockAt(x+1,y+1,z).setType(Material.LANTERN,false);
            }
            default -> {
                if(style==5 || style==11) mangroveWinterTree(w,r,x,z);
                else if(style==12 || style==15) oakFrostGiant(w,r,x,z);
                else ancientSpruceWinterTree(w,r,x,z);
            }
        }
    }

    private void generateLogPile(World w,int x,int y,int z) {
        for(int layer=0;layer<2;layer++) for(int i=0;i<4;i++) {
            int dx=(i%2)*2-1, dz=(i/2)*2-1;
            w.getBlockAt(x+dx,y+layer,z+dz).setType(Material.SPRUCE_LOG,false);
        }
        w.getBlockAt(x,y+2,z).setType(Material.SNOW,false);
    }

    private void generateSnowyRock(World w,Random r,int x,int y,int z) {
        Material[] rocks={Material.STONE,Material.ANDESITE,Material.DIORITE,Material.GRANITE};
        Material rock=rocks[r.nextInt(rocks.length)];
        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++)
            if(dx*dx+dz*dz<=2) w.getBlockAt(x+dx,y,z+dz).setType(rock,false);
        w.getBlockAt(x,y+1,z).setType(Material.SNOW,false);
    }

    private void generateFrozenPond(World w,Random r,int x,int z) {
        int y=w.getHighestBlockYAt(x,z);
        int radius=3+r.nextInt(3);
        for(int dx=-radius;dx<=radius;dx++) for(int dz=-radius;dz<=radius;dz++) {
            if(dx*dx+dz*dz<=radius*radius) {
                int yy=w.getHighestBlockYAt(x+dx,z+dz);
                if(Math.abs(yy-y)<=2) {
                    w.getBlockAt(x+dx,yy,z+dz).setType(Material.ICE,false);
                    if(r.nextDouble()<.18) w.getBlockAt(x+dx,yy+1,z+dz).setType(Material.SNOW,false);
                }
            }
        }
        w.getBlockAt(x,y+1,z).setType(Material.SNOW,false);
        w.getBlockAt(x+radius+1,y+1,z).setType(Material.SPRUCE_FENCE,false);
        w.getBlockAt(x+radius+1,y+2,z).setType(Material.LANTERN,false);
    }

    private void generateSnowyBridge(World w,Random r,int x,int z) {
        int y=w.getHighestBlockYAt(x,z)+1;
        for(int i=-4;i<=4;i++) {
            w.getBlockAt(x+i,y,z).setType(Material.SPRUCE_PLANKS,false);
            if(Math.abs(i)==4) {
                w.getBlockAt(x+i,y+1,z-1).setType(Material.SPRUCE_FENCE,false);
                w.getBlockAt(x+i,y+1,z+1).setType(Material.SPRUCE_FENCE,false);
            }
            if(i%2==0) {
                w.getBlockAt(x+i,y+1,z-1).setType(Material.SPRUCE_FENCE,false);
                w.getBlockAt(x+i,y+1,z+1).setType(Material.SPRUCE_FENCE,false);
            }
        }
        w.getBlockAt(x,y+1,z).setType(Material.SNOW,false);
        w.getBlockAt(x-4,y+2,z).setType(Material.LANTERN,false);
        w.getBlockAt(x+4,y+2,z).setType(Material.LANTERN,false);
    }

    private void generateChristmasClearing(World w,Random r,int x,int z) {
        int y=w.getHighestBlockYAt(x,z)+1;
        int h=8+r.nextInt(4);
        for(int i=0;i<h;i++) w.getBlockAt(x,y+i,z).setType(Material.SPRUCE_LOG,false);
        for(int layer=1;layer<h-1;layer+=2) {
            int rad=Math.max(1,4-layer/3);
            int cy=y+h-layer;
            for(int dx=-rad;dx<=rad;dx++) for(int dz=-rad;dz<=rad;dz++)
                if(dx*dx+dz*dz<=rad*rad) {
                    w.getBlockAt(x+dx,cy,z+dz).setType(Material.SPRUCE_LEAVES,false);
                    if(r.nextDouble()<.35) w.getBlockAt(x+dx,cy+1,z+dz).setType(Material.SNOW,false);
                }
        }
        w.getBlockAt(x,y+h,z).setType(Material.GLOWSTONE,false);
        // Simple seasonal decoration using vanilla blocks; no custom resource pack required.
        for(int i=0;i<6;i++) {
            double a=i*Math.PI/3.0;
            int dx=(int)Math.round(Math.cos(a)*4), dz=(int)Math.round(Math.sin(a)*4);
            w.getBlockAt(x+dx,y,z+dz).setType(i%2==0?Material.RED_WOOL:Material.GREEN_WOOL,false);
            w.getBlockAt(x+dx,y+1,z+dz).setType(Material.SNOW,false);
        }
        w.getBlockAt(x,y+1,z).setType(Material.LANTERN,false);
    }

    private void generateLandmark(World w, Random r, int bx, int bz, int style) {
        int x = bx + 3 + r.nextInt(10);
        int z = bz + 3 + r.nextInt(10);
        int y = w.getHighestBlockYAt(x, z) + 1;

        if (style == 2 || style == 8) {
            crystalShrine(w, x, z);
            return;
        }
        if (style == 9) {
            riftShrine(w, x, z);
            return;
        }

        switch (r.nextInt(7)) {
            case 0 -> cabin(w, r, x, y, z);
            case 1 -> ruin(w, r, x, z, 3);
            case 2 -> tower(w, r, x, y, z);
            case 3 -> camp(w, r, x, y, z);
            case 4 -> shrine(w, x, z);
            case 5 -> iceArch(w, x, z);
            default -> shipwreck(w, x, z);
        }
    }

    private void cabin(World w, Random r, int x, int y, int z) {
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) for (int dy = 0; dy < 5; dy++) {
            boolean floor = dy == 0;
            boolean wall = Math.abs(dx) == 4 || Math.abs(dz) == 4;
            w.getBlockAt(x + dx, y + dy, z + dz).setType(floor ? Material.SPRUCE_PLANKS : wall ? Material.SPRUCE_LOG : Material.AIR, false);
        }
        for (int l = 0; l < 4; l++) {
            int rad = 4 - l;
            for (int dx = -rad; dx <= rad; dx++) for (int dz = -rad; dz <= rad; dz++) {
                w.getBlockAt(x + dx, y + 5 + l, z + dz).setType(l < 2 ? Material.SPRUCE_STAIRS : Material.SPRUCE_SLAB, false);
                if (l >= 2) w.getBlockAt(x + dx, y + 6 + l, z + dz).setType(Material.SNOW, false);
            }
        }
        w.getBlockAt(x, y + 1, z + 2).setType(Material.CAMPFIRE, false);
        w.getBlockAt(x - 3, y + 1, z - 1).setType(Material.LANTERN, false);
        w.getBlockAt(x + 3, y + 1, z - 1).setType(Material.LANTERN, false);
        w.getBlockAt(x + 2, y + 1, z - 2).setType(Material.BARREL, false);
        w.getBlockAt(x + 3, y + 1, z - 2).setType(Material.CRAFTING_TABLE, false);
        chest(w, r, x + 1, y + 1, z - 2, 2);
    }

    private void ruin(World w, Random r, int x, int z, int tier) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        for (int i = 0; i < 3; i++) {
            for (int h = 0; h < 4 + r.nextInt(4); h++) {
                w.getBlockAt(x - 3 + i * 3, y + h, z).setType(Material.STONE_BRICKS, false);
            }
        }
        for (int i = 0; i < 16; i++) {
            int dx = r.nextInt(9) - 4, dz = r.nextInt(9) - 4;
            w.getBlockAt(x + dx, y, z + dz).setType(r.nextBoolean() ? Material.CRACKED_STONE_BRICKS : Material.SNOW_BLOCK, false);
        }
        chest(w, r, x, y + 1, z + 2, tier);
    }

    private void tower(World w, Random r, int x, int y, int z) {
        for (int h = 0; h < 12; h++) for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
            w.getBlockAt(x + dx, y + h, z + dz).setType(Material.SPRUCE_LOG, false);
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            w.getBlockAt(x + dx, y + 12, z + dz).setType(Material.SPRUCE_PLANKS, false);
        chest(w, r, x, y + 11, z, 3);
    }

    private void camp(World w, Random r, int x, int y, int z) {
        w.getBlockAt(x, y, z).setType(Material.CAMPFIRE, false);
        for (int dx = -3; dx <= 3; dx += 3) {
            w.getBlockAt(x + dx, y, z).setType(Material.SPRUCE_LOG, false);
            w.getBlockAt(x + dx, y + 1, z).setType(Material.LANTERN, false);
        }
        chest(w, r, x - 2, y, z - 2, 1);
    }

    private void shrine(World w, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            if (Math.abs(dx) == 2 || Math.abs(dz) == 2) w.getBlockAt(x + dx, y, z + dz).setType(Material.PACKED_ICE, false);
        w.getBlockAt(x, y, z).setType(Material.BLUE_ICE, false);
        w.getBlockAt(x, y + 1, z).setType(Material.SOUL_LANTERN, false);
    }

    private void crystalShrine(World w, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        for (int i = 0; i < 6; i++) {
            int dx = (i % 3) - 1, dz = (i / 3) - 1;
            for (int h = 0; h < 3 + i % 4; h++) w.getBlockAt(x + dx, y + h, z + dz).setType(Material.AMETHYST_BLOCK, false);
        }
        chest(w, new Random(w.getSeed() ^ x * 31L ^ z), x, y + 1, z, 4);
    }

    private void riftShrine(World w, int x, int z) {
        int y = Math.max(w.getMinHeight() + 8, w.getHighestBlockYAt(x, z) - 4);
        for (int dx = -2; dx <= 2; dx++) {
            w.getBlockAt(x + dx, y, z).setType(Material.BLUE_ICE, false);
            w.getBlockAt(x + dx, y + 1, z).setType(Material.SOUL_FIRE, false);
        }
        chest(w, new Random(w.getSeed() ^ x * 17L ^ z * 53L), x, y + 2, z + 2, 5);
    }

    private void iceArch(World w, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        for (int h = 0; h < 7; h++) {
            w.getBlockAt(x - 3, y + h, z).setType(Material.PACKED_ICE, false);
            w.getBlockAt(x + 3, y + h, z).setType(Material.PACKED_ICE, false);
        }
        for (int dx = -3; dx <= 3; dx++) w.getBlockAt(x + dx, y + 6, z).setType(Material.ICE, false);
    }

    private void shipwreck(World w, int x, int z) {
        int y = Math.max(w.getMinHeight() + 1, w.getHighestBlockYAt(x, z));
        for (int i = -5; i <= 5; i++) {
            w.getBlockAt(x + i, y, z).setType(Material.SPRUCE_PLANKS, false);
            if (Math.abs(i) % 3 == 0) w.getBlockAt(x + i, y + 1, z).setType(Material.SPRUCE_FENCE, false);
        }
        chest(w, new Random(w.getSeed() ^ x * 97L ^ z * 13L), x, y + 1, z + 1, 3);
    }

    private void frozenWaterfall(World w, int x, int z) {
        int y = w.getHighestBlockYAt(x, z);
        for (int h = 0; h < 12; h++) w.getBlockAt(x, y - h, z).setType(Material.ICE, false);
    }

    private void frozenCaveMouth(World w, int x, int z) {
        int y = w.getHighestBlockYAt(x, z);
        for (int dx = -3; dx <= 3; dx++) for (int dy = 0; dy < 4; dy++)
            if (Math.abs(dx) + dy < 6) w.getBlockAt(x + dx, y - dy, z).setType(Material.AIR, false);
        for (int dx = -4; dx <= 4; dx++) {
            w.getBlockAt(x + dx, y, z).setType(Material.ICE, false);
            w.getBlockAt(x + dx, y + 1, z).setType(Material.PACKED_ICE, false);
        }
    }

    private void mushroomGrove(World w, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        for (int i = 0; i < 4; i++) {
            w.getBlockAt(x - 3 + i * 2, y, z).setType(Material.MOSS_BLOCK, false);
            w.getBlockAt(x - 3 + i * 2, y + 1, z).setType(Material.RED_MUSHROOM, false);
        }
    }

    private void snowPillar(World w, int x, int z, int height) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        for (int i = 0; i < height; i++) w.getBlockAt(x, y + i, z).setType(Material.SNOW_BLOCK, false);
    }

    private void iceSpike(World w, int x, int z, int height) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        int radius = Math.max(1, height / 7);
        for (int h = 0; h < height; h++) {
            int r = Math.max(0, radius - h / Math.max(1, height / 3));
            for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++)
                if (dx * dx + dz * dz <= r * r) w.getBlockAt(x + dx, y + h, z + dz).setType(Material.ICE, false);
        }
    }

    private void spruce(World w, Random r, int x, int z, int height, int radius) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        for (int h = 0; h < height; h++) w.getBlockAt(x, y + h, z).setType(Material.SPRUCE_LOG, false);
        for (int dy = 1; dy < height; dy++) {
            int rad = Math.max(1, radius - dy / 3);
            for (int dx = -rad; dx <= rad; dx++) for (int dz = -rad; dz <= rad; dz++)
                if (dx * dx + dz * dz <= rad * rad) w.getBlockAt(x + dx, y + height - dy, z + dz).setType(Material.SPRUCE_LEAVES, false);
        }
    }

    private void crookedPine(World w, Random r, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) + 1;
        int h = 5 + r.nextInt(5);
        int cx = x;
        for (int i = 0; i < h; i++) {
            w.getBlockAt(cx, y + i, z).setType(Material.SPRUCE_LOG, false);
            if (i > 1 && r.nextBoolean()) cx += r.nextBoolean() ? 1 : -1;
        }
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            if (dx * dx + dz * dz <= 5) w.getBlockAt(cx + dx, y + h - 2, z + dz).setType(Material.SPRUCE_LEAVES, false);
    }

    private void snowDrift(World w, Block center, Random r) {
        int radius = 1 + r.nextInt(3);
        for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++)
            if (dx * dx + dz * dz <= radius * radius) {
                Block b = w.getHighestBlockAt(center.getX() + dx, center.getZ() + dz);
                if (b.getType().isSolid()) w.getBlockAt(b.getX(), b.getY() + 1, b.getZ()).setType(Material.SNOW, false);
            }
    }

    private void generateGlacier(World w, Random r, int bx, int bz) {
        int x = bx + 2 + r.nextInt(12), z = bz + 2 + r.nextInt(12);
        int y = w.getHighestBlockYAt(x, z);
        int radius = 2 + r.nextInt(4), height = 4 + r.nextInt(9);
        for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > radius) continue;
            int h = Math.max(1, (int) (height * (1 - d / (radius + .5))));
            for (int dy = 0; dy < h; dy++) w.getBlockAt(x + dx, y + dy + 1, z + dz).setType(dy > h - 2 ? Material.ICE : Material.PACKED_ICE, false);
        }
    }

    private void freezeWater(World w, int bx, int bz) {
        if (!w.getWorldBorder().isInside(new org.bukkit.Location(w, bx + 8, 64, bz + 8))) return;
        for (int x = bx; x < bx + 16; x++) for (int z = bz; z < bz + 16; z++) {
            int y = w.getHighestBlockYAt(x, z);
            if (w.getBlockAt(x, y, z).getType() == Material.WATER) w.getBlockAt(x, y, z).setType(Material.ICE, false);
        }
    }

    private void chest(World w, Random r, int x, int y, int z, int tier) {
        Block b = w.getBlockAt(x, y, z);
        b.setType(Material.CHEST, false);
        if (b.getState() instanceof Chest c) {
            c.getInventory().addItem(new ItemStack(Material.BREAD, 1 + r.nextInt(4)));
            c.getInventory().addItem(new ItemStack(Material.TORCH, 3 + r.nextInt(8)));
            if (tier >= 2) c.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 1 + r.nextInt(3)));
            if (tier >= 3) c.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
            if (tier >= 4) c.getInventory().addItem(new ItemStack(Material.EMERALD, 1 + r.nextInt(2)));
            if (tier >= 5) c.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
            ItemStack note = new ItemStack(Material.PAPER);
            ItemMeta meta = note.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§bFrozen Discovery");
                meta.setLore(List.of("§7The ruins remember the First Flame.", "§eKeep exploring."));
                note.setItemMeta(meta);
            }
            c.getInventory().addItem(note);
            c.update();
        }
    }
}
