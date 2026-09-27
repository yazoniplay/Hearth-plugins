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
    @Override @SuppressWarnings("deprecation")
    public void populate(World world, Random random, Chunk chunk) {
        int baseX=chunk.getX()<<4, baseZ=chunk.getZ()<<4;
        freezeWater(world,baseX,baseZ);
        for(int i=0;i<20;i++){
            int x=baseX+random.nextInt(16), z=baseZ+random.nextInt(16);
            Block top=world.getHighestBlockAt(x,z);
            if(top.getType()==Material.SNOW&&random.nextDouble()<.32) placeSnowDrift(world,top,random);
            if(top.getY()>=70&&random.nextDouble()<.08) placeIcicles(world,top,random);
        }
        if(random.nextDouble()<.035) generateLandmark(world,random,baseX,baseZ);
        if(random.nextDouble()<.025) generateTree(world,random,baseX,baseZ);
        if(random.nextDouble()<.035) generateGlacier(world,random,baseX,baseZ);
    }

    private void freezeWater(World w,int bx,int bz){for(int x=bx;x<bx+16;x++)for(int z=bz;z<bz+16;z++){int y=w.getHighestBlockYAt(x,z);if(w.getBlockAt(x,y,z).getType()==Material.WATER)w.getBlockAt(x,y,z).setType(Material.ICE,false);}}
    private void placeSnowDrift(World w,Block c,Random r){int rad=1+r.nextInt(3);for(int dx=-rad;dx<=rad;dx++)for(int dz=-rad;dz<=rad;dz++)if(dx*dx+dz*dz<=rad*rad){Block b=w.getHighestBlockAt(c.getX()+dx,c.getZ()+dz);if(b.getType().isSolid()||b.getType()==Material.SNOW)for(int h=0;h<1+r.nextInt(2);h++)if(w.getBlockAt(b.getX(),b.getY()+1+h,b.getZ()).isEmpty())w.getBlockAt(b.getX(),b.getY()+1+h,b.getZ()).setType(Material.SNOW,false);}}
    private void placeIcicles(World w,Block c,Random r){for(int i=1;i<=1+r.nextInt(3);i++){Block b=w.getBlockAt(c.getX(),c.getY()-i,c.getZ());if(b.isEmpty())b.setType(Material.POINTED_DRIPSTONE,false);else break;}}
    
    private void generateLandmark(World w,Random r,int bx,int bz){
        int x=bx+3+r.nextInt(10),z=bz+3+r.nextInt(10),y=w.getHighestBlockYAt(x,z)+1;
        int type=r.nextInt(6);
        switch(type){case 0->cabin(w,r,x,y,z);case 1->ruin(w,r,x,y,z);case 2->tower(w,r,x,y,z);case 3->camp(w,r,x,y,z);case 4->shrine(w,r,x,y,z);default->bridge(w,r,x,y,z);}
    }
    private void cabin(World w,Random r,int x,int y,int z){
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)for(int dy=0;dy<5;dy++){boolean floor=dy==0,wall=Math.abs(dx)==4||Math.abs(dz)==4;w.getBlockAt(x+dx,y+dy,z+dz).setType(floor?Material.SPRUCE_PLANKS:wall?Material.SPRUCE_LOG:Material.AIR,false);}
        for(int l=0;l<4;l++){int rad=4-l;for(int dx=-rad;dx<=rad;dx++)for(int dz=-rad;dz<=rad;dz++){w.getBlockAt(x+dx,y+5+l,z+dz).setType(l<2?Material.SPRUCE_STAIRS:Material.SPRUCE_SLAB,false);if(l>=2)w.getBlockAt(x+dx,y+6+l,z+dz).setType(Material.SNOW,false);}}
        w.getBlockAt(x,y+1,z-4).setType(Material.SPRUCE_DOOR,false);w.getBlockAt(x,y+2,z-4).setType(Material.SPRUCE_DOOR,false);
        w.getBlockAt(x-2,y+2,z-4).setType(Material.GLASS_PANE,false);w.getBlockAt(x+2,y+2,z-4).setType(Material.GLASS_PANE,false);
        w.getBlockAt(x,y+1,z+2).setType(Material.CAMPFIRE,false);w.getBlockAt(x,y+2,z+2).setType(Material.COBBLESTONE,false);
        w.getBlockAt(x-2,y+1,z+1).setType(Material.SPRUCE_STAIRS,false);w.getBlockAt(x+2,y+1,z+1).setType(Material.SPRUCE_STAIRS,false);
        w.getBlockAt(x-3,y+1,z-1).setType(Material.LANTERN,false);w.getBlockAt(x+3,y+1,z-1).setType(Material.LANTERN,false);
        w.getBlockAt(x-1,y+1,z-2).setType(Material.RED_BED,false);w.getBlockAt(x-1,y+1,z-1).setType(Material.RED_BED,false);
        w.getBlockAt(x+2,y+1,z-2).setType(Material.BARREL,false);w.getBlockAt(x+3,y+1,z-2).setType(Material.CRAFTING_TABLE,false);w.getBlockAt(x+2,y+1,z-1).setType(Material.FURNACE,false);
        chest(w,r,x+1,y+1,z-2,2);
    }
    private void ruin(World w,Random r,int x,int y,int z){for(int i=0;i<3;i++){for(int h=0;h<5+r.nextInt(3);h++)w.getBlockAt(x-3+i*3,y+h,z).setType(Material.STONE_BRICKS,false);}for(int i=0;i<12;i++){int dx=r.nextInt(7)-3,dz=r.nextInt(7)-3;w.getBlockAt(x+dx,y,z+dz).setType(r.nextBoolean()?Material.CRACKED_STONE_BRICKS:Material.SNOW_BLOCK,false);}chest(w,r,x,y+1,z+2,3);}
    private void tower(World w,Random r,int x,int y,int z){for(int h=0;h<10;h++)for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)w.getBlockAt(x+dx,y+h,z+dz).setType(Material.SPRUCE_LOG,false);for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)w.getBlockAt(x+dx,y+10,z+dz).setType(Material.SPRUCE_PLANKS,false);w.getBlockAt(x,y+1,z).setType(Material.LADDER,false);chest(w,r,x,y+9,z,3);}
    private void camp(World w,Random r,int x,int y,int z){w.getBlockAt(x,y,z).setType(Material.CAMPFIRE,false);for(int dx=-3;dx<=3;dx+=3){w.getBlockAt(x+dx,y,z).setType(Material.SPRUCE_LOG,false);w.getBlockAt(x+dx,y+1,z).setType(Material.LANTERN,false);}w.getBlockAt(x+2,y,z+2).setType(Material.WHITE_WOOL,false);w.getBlockAt(x+2,y+1,z+2).setType(Material.WHITE_WOOL,false);chest(w,r,x-2,y,z-2,1);}
    private void shrine(World w,Random r,int x,int y,int z){for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)if(Math.abs(dx)==2||Math.abs(dz)==2)w.getBlockAt(x+dx,y,z+dz).setType(Material.PACKED_ICE,false);w.getBlockAt(x,y+1,z).setType(Material.SOUL_LANTERN,false);w.getBlockAt(x,y,z).setType(Material.BLUE_ICE,false);chest(w,r,x,y+1,z+2,4);}
    private void bridge(World w,Random r,int x,int y,int z){for(int i=-5;i<=5;i++){w.getBlockAt(x+i,y,z).setType(Material.SPRUCE_PLANKS,false);if(i%3==0)w.getBlockAt(x+i,y+1,z).setType(Material.SPRUCE_FENCE,false);}}
    private void generateTree(World w,Random r,int bx,int bz){int x=bx+2+r.nextInt(12),z=bz+2+r.nextInt(12),y=w.getHighestBlockYAt(x,z)+1;int h=5+r.nextInt(5);for(int i=0;i<h;i++)w.getBlockAt(x,y+i,z).setType(Material.SPRUCE_LOG,false);for(int dy=1;dy<5;dy++){int rad=dy<3?3:2;for(int dx=-rad;dx<=rad;dx++)for(int dz=-rad;dz<=rad;dz++)if(dx*dx+dz*dz<=rad*rad)w.getBlockAt(x+dx,y+h-dy,z+dz).setType(Material.SPRUCE_LEAVES,false);}}
    private void chest(World w,Random r,int x,int y,int z,int tier){Block b=w.getBlockAt(x,y,z);b.setType(Material.CHEST,false);if(b.getState() instanceof Chest c){c.getInventory().addItem(new ItemStack(Material.BREAD,1+r.nextInt(4)));c.getInventory().addItem(new ItemStack(Material.TORCH,3+r.nextInt(8)));if(tier>=2)c.getInventory().addItem(new ItemStack(Material.IRON_INGOT,1+r.nextInt(3)));if(tier>=3)c.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));if(tier>=4)c.getInventory().addItem(new ItemStack(Material.EMERALD,1+r.nextInt(2)));if(r.nextDouble()<.15)c.getInventory().addItem(note());c.update();}}
    private ItemStack note(){ItemStack i=new ItemStack(Material.PAPER);ItemMeta m=i.getItemMeta();if(m!=null){m.setDisplayName("§bFrozen Discovery");m.setLore(List.of("§7The ruins remember the First Flame.","§eKeep exploring."));i.setItemMeta(m);}return i;}
    private void generateGlacier(World w,Random r,int bx,int bz){int x=bx+2+r.nextInt(12),z=bz+2+r.nextInt(12),y=w.getHighestBlockYAt(x,z);int rad=2+r.nextInt(4),h=4+r.nextInt(7);for(int dx=-rad;dx<=rad;dx++)for(int dz=-rad;dz<=rad;dz++){double d=Math.sqrt(dx*dx+dz*dz);if(d>rad)continue;int ch=Math.max(1,(int)(h*(1-d/(rad+.5))));for(int dy=0;dy<ch;dy++)w.getBlockAt(x+dx,y+dy+1,z+dz).setType(dy>ch-2?Material.ICE:Material.PACKED_ICE,false);}}
}