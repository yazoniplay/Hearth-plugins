package dev.hearthsmp.world;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;

public final class HearthLootListener implements Listener {
    private final NamespacedKey generatedKey;

    public HearthLootListener(JavaPlugin plugin) {
        this.generatedKey = new NamespacedKey(plugin, "hearth_generated_loot");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) return;
        Chunk chunk = event.getChunk();
        for (int x = 0; x < 16; x++) {
            for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (!(block.getState() instanceof Chest chest)) continue;
                    if (!chest.getPersistentDataContainer().has(generatedKey, PersistentDataType.BYTE)) continue;
                    if (chest.getInventory().isEmpty()) {
                        fill(chest, new Random(chunk.getWorld().getSeed() ^ (chunk.getX() * 341873128712L) ^ (chunk.getZ() * 132897987541L) ^ y));
                    }
                }
            }
        }
    }

    public void markAndFill(Chest chest, Random random, int tier) {
        chest.getPersistentDataContainer().set(generatedKey, PersistentDataType.BYTE, (byte) 1);
        fill(chest, random, tier);
    }

    private void fill(Chest chest, Random random) {
        fill(chest, random, 2);
    }

    private void fill(Chest chest, Random random, int tier) {
        chest.getInventory().clear();
        chest.getInventory().addItem(new ItemStack(Material.BREAD, 2 + random.nextInt(4)));
        chest.getInventory().addItem(new ItemStack(Material.TORCH, 4 + random.nextInt(7)));
        if (tier >= 2) chest.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 1 + random.nextInt(3)));
        if (tier >= 3) chest.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
        if (tier >= 4) chest.getInventory().addItem(new ItemStack(Material.EMERALD, 1 + random.nextInt(2)));
        if (tier >= 5) chest.getInventory().addItem(new ItemStack(Material.DIAMOND));

        ItemStack note = new ItemStack(Material.PAPER);
        ItemMeta meta = note.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bHearth Discovery");
            meta.setLore(List.of("§7A warm light survives the snow.", "§eThe First Flame remembers."));
            note.setItemMeta(meta);
        }
        chest.getInventory().addItem(note);
        chest.update(true, false);
    }
}
