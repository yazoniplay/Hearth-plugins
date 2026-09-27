package dev.hearthsmp.world;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.Random;

public final class WinterVillageGenerator {
    private WinterVillageGenerator() {}

    public static void tryGenerate(World w, int cx, int cz) {
        long seed=w.getSeed(); int gx=Math.floorDiv(cx,125), gz=Math.floorDiv(cz,125);
        Random r=new Random(seed ^ gx*341873128712L ^ gz*132897987541L);
        if(r.nextDouble()>.72) return;
        int style=WinterChunkGenerator.regionStyle(seed,gx*2000+1000,gz*2000+1000);
        if(style!=0&&style!=4&&style!=5&&style!=12&&style!=15)return;
        int x=gx*2000+1000+r.nextInt(601)-300,z=gz*2000+1000+r.nextInt(601)-300;
        if(cx!=Math.floorDiv(x,16)||cz!=Math.floorDiv(z,16))return;
        int y=w.getHighestBlockYAt(x,z)+1;
        if(y<55||y>135)return;
        int p=Math.floorMod(style+r.nextInt(5),5);
        build(w,r,x,y,z,p,style);
    }

    private static void build(World w,Random r,int x,int y,int z,int p,int style){
        Material log=switch(p){case 1->Material.DARK_OAK_LOG;case 2->Material.BIRCH_LOG;case 3->Material.STRIPPED_SPRUCE_LOG;case 4->Material.PALE_OAK_LOG;default->Material.SPRUCE_LOG;};
        Material plank=switch(p){case 1->Material.DARK_OAK_PLANKS;case 2->Material.BIRCH_PLANKS;case 4->Material.PALE_OAK_PLANKS;default->Material.SPRUCE_PLANKS;};
        Material roof=p==2?Material.DARK_OAK_PLANKS:plank, stone=p==4?Material.POLISHED_ANDESITE:Material.COBBLESTONE;
        terraform(w,x,y,z,31,25); roads(w,x,y-1,z); hearth(w,x,y,z,stone); sign(w,x,y+2,z,style);
        house(w,x-17,ground(w,x-17,z-12),z-12,r,log,plank,roof,stone,1);
        house(w,x+17,ground(w,x+17,z-12),z-12,r,log,plank,roof,stone,2);
        house(w,x-17,ground(w,x-17,z+12),z+12,r,log,plank,roof,stone,3);
        house(w,x+17,ground(w,x+17,z+12),z+12,r,log,plank,roof,stone,4);
        inn(w,x,ground(w,x,z+20),z+20,r,log,plank,roof,stone);
        bakery(w,x-21,ground(w,x-21,z+1),z+1,log,plank,roof,stone);
        smith(w,x+21,ground(w,x+21,z+1),z+1,log,plank,roof,stone);
        woodcutter(w,x-2,ground(w,x-2,z-21),z-21,log,plank,roof,stone);
        barn(w,x+20,ground(w,x+20,z+18),z+18,log,plank,roof);
        if(r.nextBoolean()) chapel(w,x-23,ground(w,x-23,z-21),z-21,log,plank,roof,stone);
        tower(w,x+25,ground(w,x+25,z-23),z-23,log,roof,stone);
        pond(w,x-27,z+22,r); if(style==12||style==15) bridge(w,x+27,ground(w,x+27,z+8),z+8,log,plank);
        christmas(w,r,x,y,z); details(w,r,x,y,z);
    }

    private static int ground(World w,int x,int z){return w.getHighestBlockYAt(x,z)+1;}

    private static void terraform(World w,int x,int y,int z,int rx,int rz){
        for(int dx=-rx;dx<=rx;dx++)for(int dz=-rz;dz<=rz;dz++){
            if(dx*dx/(double)(rx*rx)+dz*dz/(double)(rz*rz)>1)continue;
            int top=w.getHighestBlockYAt(x+dx,z+dz), target=y-1;
            if(top<target)for(int yy=top;yy<=target;yy++)w.getBlockAt(x+dx,yy,z+dz).setType(Material.SNOW_BLOCK,false);
            else if(top>target+3){for(int yy=target+1;yy<=top;yy++)w.getBlockAt(x+dx,yy,z+dz).setType(Material.AIR,false);w.getBlockAt(x+dx,target,z+dz).setType(Material.SNOW_BLOCK,false);}
        }
    }

    private static void roads(World w,int x,int y,int z){
        for(int i=-24;i<=24;i++)for(int j=-24;j<=24;j++)if(Math.abs(i)<=1||Math.abs(j)<=1||Math.abs(i-j)<=1||Math.abs(i+j)<=1){
            w.getBlockAt(x+i,y,z+j).setType(Material.PACKED_ICE,false);w.getBlockAt(x+i,y+1,z+j).setType(Material.SNOW,false);
        }
    }

    private static void hearth(World w,int x,int y,int z,Material stone){
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)if(Math.abs(dx)==4||Math.abs(dz)==4)w.getBlockAt(x+dx,y,z+dz).setType(stone,false);
        w.getBlockAt(x,y,z).setType(Material.CAMPFIRE,false);w.getBlockAt(x,y+1,z).setType(Material.LANTERN,false);w.getBlockAt(x,y+1,z-4).setType(Material.BELL,false);
        for(int dx=-3;dx<=3;dx+=3){w.getBlockAt(x+dx,y,z-2).setType(Material.SPRUCE_STAIRS,false);w.getBlockAt(x+dx,y,z+2).setType(Material.SPRUCE_STAIRS,false);}
    }

    private static void sign(World w,int x,int y,int z,int style){
        String n=switch(Math.floorMod(style,5)){case 0->"Frostpine";case 1->"Snowmere";case 2->"Hearthvale";case 3->"Winterrest";default->"Everfrost";};
        Block b=w.getBlockAt(x,y,z);b.setType(Material.OAK_SIGN,false);if(b.getState() instanceof Sign s){s.getSide(Side.FRONT).setLine(1,n);s.getSide(Side.FRONT).setLine(2,"Hearth Village");s.update(true,false);}
    }

    private static void house(World w,int x,int y,int z,Random r,Material log,Material plank,Material roof,Material stone,int tier){
        int sx=5+tier%2,sz=4;shell(w,x,y,z,sx,sz,4,log,plank,stone);roof(w,x,y+5,z,sx,sz,roof);door(w,x,y+1,z-sz);
        window(w,x-sx,y+2,z);window(w,x+sx,y+2,z);window(w,x-2,y+2,z+sz);window(w,x+2,y+2,z+sz);
        w.getBlockAt(x,y+1,z).setType(Material.CAMPFIRE,false);w.getBlockAt(x+2,y+1,z+1).setType(Material.BARREL,false);
        w.getBlockAt(x-2,y+1,z+1).setType(Material.BOOKSHELF,false);w.getBlockAt(x-1,y+1,z-1).setType(Material.CRAFTING_TABLE,false);
        w.getBlockAt(x+sx-1,y+1,z+1).setType(Material.LANTERN,false);chest(w,r,x+1,y+1,z+1,Math.min(4,tier));chimney(w,x+sx-1,y+1,z+1,y+7);
    }

    private static void inn(World w,int x,int y,int z,Random r,Material log,Material plank,Material roof,Material stone){
        shell(w,x,y,z,8,5,5,log,plank,stone);roof(w,x,y+6,z,8,5,roof);door(w,x,y+1,z-5);window(w,x-8,y+2,z);window(w,x+8,y+2,z);
        w.getBlockAt(x,y+1,z).setType(Material.CAMPFIRE,false);w.getBlockAt(x-5,y+1,z+2).setType(Material.BOOKSHELF,false);w.getBlockAt(x+4,y+1,z+2).setType(Material.BARREL,false);
        w.getBlockAt(x-4,y+1,z).setType(Material.WHITE_BED,false);w.getBlockAt(x+4,y+1,z).setType(Material.WHITE_BED,false);chest(w,r,x+5,y+1,z+2,4);chimney(w,x-6,y+1,z+2,y+8);
    }

    private static void bakery(World w,int x,int y,int z,Material log,Material plank,Material roof,Material stone){
        shell(w,x,y,z,5,4,4,log,plank,stone);roof(w,x,y+5,z,5,4,roof);door(w,x,y+1,z-4);window(w,x-5,y+2,z);
        w.getBlockAt(x-2,y+1,z+1).setType(Material.FURNACE,false);w.getBlockAt(x,y+1,z+1).setType(Material.CRAFTING_TABLE,false);w.getBlockAt(x+2,y+1,z+1).setType(Material.BARREL,false);
    }

    private static void smith(World w,int x,int y,int z,Material log,Material plank,Material roof,Material stone){
        shell(w,x,y,z,5,4,4,log,plank,stone);roof(w,x,y+5,z,5,4,roof);door(w,x,y+1,z-4);
        w.getBlockAt(x-2,y+1,z).setType(Material.SMITHING_TABLE,false);w.getBlockAt(x+1,y+1,z).setType(Material.BLAST_FURNACE,false);w.getBlockAt(x+3,y+1,z+1).setType(Material.LAVA_CAULDRON,false);w.getBlockAt(x,y+1,z+2).setType(Material.ANVIL,false);
        chest(w,new Random(w.getSeed()^x*31L^z),x-2,y+1,z+2,4);
    }

    private static void woodcutter(World w,int x,int y,int z,Material log,Material plank,Material roof,Material stone){
        shell(w,x,y,z,5,4,4,log,plank,stone);roof(w,x,y+5,z,5,4,roof);door(w,x,y+1,z-4);w.getBlockAt(x-2,y+1,z+1).setType(Material.BARREL,false);w.getBlockAt(x+2,y+1,z+1).setType(Material.CRAFTING_TABLE,false);
        chest(w,new Random(w.getSeed()^x*47L^z),x,y+1,z+2,2);
    }

    private static void chapel(World w,int x,int y,int z,Material log,Material plank,Material roof,Material stone){
        shell(w,x,y,z,4,6,5,log,plank,stone);roof(w,x,y+6,z,4,6,roof);door(w,x,y+1,z-6);w.getBlockAt(x,y+1,z).setType(Material.CAMPFIRE,false);w.getBlockAt(x,y+1,z+3).setType(Material.LECTERN,false);w.getBlockAt(x,y+6,z).setType(Material.BELL,false);
    }

    private static void barn(World w,int x,int y,int z,Material log,Material plank,Material roof){
        for(int dx=-5;dx<=5;dx++)for(int dz=-4;dz<=4;dz++){w.getBlockAt(x+dx,y,z+dz).setType(plank,false);if(Math.abs(dx)==5||Math.abs(dz)==4)for(int h=1;h<=4;h++)w.getBlockAt(x+dx,y+h,z+dz).setType(log,false);}
        roof(w,x,y+5,z,5,4,roof);door(w,x,y+1,z-4);w.getBlockAt(x-3,y+1,z).setType(Material.HAY_BLOCK,false);w.getBlockAt(x+3,y+1,z).setType(Material.HAY_BLOCK,false);w.getBlockAt(x,y+1,z+2).setType(Material.BARREL,false);
    }

    private static void tower(World w,int x,int y,int z,Material log,Material roof,Material stone){
        for(int h=0;h<11;h++)for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)w.getBlockAt(x+dx,y+h,z+dz).setType(h==0?stone:log,false);
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)w.getBlockAt(x+dx,y+11,z+dz).setType(roof,false);
        chest(w,new Random(w.getSeed()^x*79L^z),x,y+9,z,4);w.getBlockAt(x-2,y+11,z).setType(Material.LANTERN,false);w.getBlockAt(x+2,y+11,z).setType(Material.LANTERN,false);
    }

    private static void shell(World w,int x,int y,int z,int sx,int sz,int h,Material log,Material plank,Material stone){
        for(int dx=-sx;dx<=sx;dx++)for(int dz=-sz;dz<=sz;dz++){w.getBlockAt(x+dx,y,z+dz).setType(stone,false);boolean wall=Math.abs(dx)==sx||Math.abs(dz)==sz;for(int yy=1;yy<=h;yy++)w.getBlockAt(x+dx,y+yy,z+dz).setType(wall?(yy==1?log:plank):Material.AIR,false);}
    }

    private static void roof(World w,int x,int y,int z,int sx,int sz,Material m){
        for(int l=0;l<4;l++){int hx=sx+2-l,hz=sz+1;for(int dx=-hx;dx<=hx;dx++)for(int dz=-hz;dz<=hz;dz++)w.getBlockAt(x+dx,y+l,z+dz).setType(l<2?m:Material.SNOW_BLOCK,false);}
    }

    private static void door(World w,int x,int y,int z){w.getBlockAt(x,y,z).setType(Material.AIR,false);w.getBlockAt(x,y+1,z).setType(Material.AIR,false);w.getBlockAt(x,y,z).setType(Material.SPRUCE_DOOR,false);}
    private static void window(World w,int x,int y,int z){w.getBlockAt(x,y,z).setType(Material.GLASS_PANE,false);w.getBlockAt(x,y+1,z).setType(Material.GLASS_PANE,false);}
    private static void chimney(World w,int x,int y,int z,int top){for(int yy=y;yy<=top;yy++)w.getBlockAt(x,yy,z).setType(Material.BRICKS,false);w.getBlockAt(x,top+1,z).setType(Material.CAMPFIRE,false);}

    private static void pond(World w,int x,int z,Random r){
        int y=ground(w,x,z)-1,rad=5+r.nextInt(4);for(int dx=-rad;dx<=rad;dx++)for(int dz=-rad;dz<=rad;dz++)if(dx*dx+dz*dz<=rad*rad)w.getBlockAt(x+dx,y,z+dz).setType(Math.abs(dx)+Math.abs(dz)<rad/2?Material.PACKED_ICE:Material.ICE,false);
        w.getBlockAt(x,y+1,z).setType(Material.SNOW,false);
    }

    private static void bridge(World w,int x,int y,int z,Material log,Material plank){for(int i=-6;i<=6;i++){w.getBlockAt(x+i,y,z).setType(plank,false);w.getBlockAt(x+i,y+1,z-1).setType(log,false);w.getBlockAt(x+i,y+1,z+1).setType(log,false);}w.getBlockAt(x-6,y+2,z).setType(Material.LANTERN,false);w.getBlockAt(x+6,y+2,z).setType(Material.LANTERN,false);}

    private static void christmas(World w,Random r,int x,int y,int z){
        int h=8+r.nextInt(4);for(int i=0;i<h;i++)w.getBlockAt(x,y+i,z).setType(Material.SPRUCE_LOG,false);
        for(int l=1;l<h-1;l+=2){int rad=Math.max(1,4-l/3),cy=y+h-l;for(int dx=-rad;dx<=rad;dx++)for(int dz=-rad;dz<=rad;dz++)if(dx*dx+dz*dz<=rad*rad)w.getBlockAt(x+dx,cy,z+dz).setType(Material.SPRUCE_LEAVES,false);}
        w.getBlockAt(x,y+h,z).setType(Material.GLOWSTONE,false);
        for(int i=0;i<8;i++){double a=i*Math.PI/4;int dx=(int)Math.round(Math.cos(a)*5),dz=(int)Math.round(Math.sin(a)*5);w.getBlockAt(x+dx,y,z+dz).setType(i%2==0?Material.RED_WOOL:Material.GREEN_WOOL,false);}
    }

    private static void details(World w,Random r,int x,int y,int z){
        for(int i=0;i<32;i++){int dx=r.nextInt(55)-27,dz=r.nextInt(50)-25;if(Math.abs(dx)<8&&Math.abs(dz)<8)continue;int gy=ground(w,x+dx,z+dz);if(r.nextDouble()<.35)w.getBlockAt(x+dx,gy,z+dz).setType(Material.SNOW,false);if(r.nextDouble()<.1)w.getBlockAt(x+dx,gy,z+dz).setType(Material.SPRUCE_FENCE,false);if(r.nextDouble()<.08)w.getBlockAt(x+dx,gy+1,z+dz).setType(Material.LANTERN,false);}
    }

    private static void chest(World w,Random r,int x,int y,int z,int tier){
        Block b=w.getBlockAt(x,y,z);b.setType(Material.CHEST,false);if(!(b.getState() instanceof Chest c))return;c.getInventory().clear();
        c.getInventory().addItem(new ItemStack(Material.BREAD,2+r.nextInt(4)));c.getInventory().addItem(new ItemStack(Material.TORCH,4+r.nextInt(7)));
        if(tier>=2)c.getInventory().addItem(new ItemStack(Material.IRON_INGOT,1+r.nextInt(3)));if(tier>=3)c.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));if(tier>=4)c.getInventory().addItem(new ItemStack(Material.EMERALD,1+r.nextInt(2)));if(tier>=5)c.getInventory().addItem(new ItemStack(Material.DIAMOND));
        ItemStack note=new ItemStack(Material.PAPER);ItemMeta meta=note.getItemMeta();if(meta!=null){meta.setDisplayName("§bHearth Village Discovery");meta.setLore(List.of("§7A warm light survives the snow.","§eThe First Flame remembers."));note.setItemMeta(meta);}c.getInventory().addItem(note);c.update(true,false);
    }
}