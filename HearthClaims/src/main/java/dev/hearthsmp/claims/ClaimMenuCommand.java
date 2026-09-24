package dev.hearthsmp.claims;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class ClaimMenuCommand implements org.bukkit.command.CommandExecutor, Listener {
    private static final String TITLE = "§8🔥 Hearth Claim";
    private final HearthClaimsPlugin plugin;

    public ClaimMenuCommand(HearthClaimsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        open(player);
        return true;
    }

    private void open(Player player) {
        Claim claim = plugin.getClaimManager().getAt(player.getLocation());
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        if (claim == null) {
            inv.setItem(13, item(Material.CAMPFIRE, "§6No claim here",
                    "§7Stand inside one of your claims", "§7to manage it."));
            player.openInventory(inv);
            return;
        }

        boolean owner = claim.getOwner().equals(player.getUniqueId());
        inv.setItem(10, item(Material.MAP, "§6Claim information",
                "§7Size: §f" + claim.getBlocks() + " blocks",
                "§7Bounds: §f" + claim.getMinX() + ", " + claim.getMinZ()
                        + " §7→ §f" + claim.getMaxX() + ", " + claim.getMaxZ()));

        inv.setItem(12, item(Material.PLAYER_HEAD, "§eTrusted players",
                "§7Trusted: §f" + claim.getTrusted().size(),
                "§8Use /trust <player> to add someone."));

        inv.setItem(14, item(Material.GOLDEN_SHOVEL, "§6Expand / shrink",
                "§7Use §f/claimexpand §7to resize.",
                "§8Example: /claimexpand east 10"));

        if (owner) {
            inv.setItem(16, item(Material.TNT, "§cAbandon claim",
                    "§7Click to abandon this claim.",
                    "§8This cannot be undone."));
        } else {
            inv.setItem(16, item(Material.BARRIER, "§cYou are not the owner",
                    "§7Only the owner can manage it."));
        }

        inv.setItem(22, item(Material.ARROW, "§7Close"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!TITLE.equals(event.getView().getTitle())) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getRawSlot() == 16) {
            Claim claim = plugin.getClaimManager().getAt(player.getLocation());
            if (claim == null || !claim.getOwner().equals(player.getUniqueId())) {
                player.sendMessage(color(plugin.getConfig().getString("messages.not-owner", "&cYou do not own this claim.")));
                player.closeInventory();
                return;
            }

            player.closeInventory();
            player.sendMessage(color(plugin.getConfig().getString("messages.abandon-confirm",
                    "&cType /abandonclaim to confirm abandoning your claim.")));
            return;
        }

        if (event.getRawSlot() == 22) {
            player.closeInventory();
        }
    }

    private ItemStack item(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(lore));
        stack.setItemMeta(meta);
        return stack;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
