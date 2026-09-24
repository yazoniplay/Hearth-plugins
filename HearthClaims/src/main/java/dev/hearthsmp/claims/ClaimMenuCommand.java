package dev.hearthsmp.claims;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ClaimMenuCommand implements org.bukkit.command.CommandExecutor, Listener {
    private static final String MAIN = "§8🔥 Hearth Claim";
    private static final String CLAIMS = "§8🏡 Your Claims";
    private static final String TRUST = "§8👥 Trusted Players";
    private static final String EXPAND = "§8⛏ Claim Size";
    private static final String CONFIRM = "§8⚠ Abandon Claim";
    private final HearthClaimsPlugin plugin;

    public ClaimMenuCommand(HearthClaimsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        openClaims(player);
        return true;
    }

    private void openClaims(Player player) {
        List<Claim> owned = plugin.getClaimManager().getOwned(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54, CLAIMS);

        if (owned.isEmpty()) {
            inv.setItem(22, item(Material.CAMPFIRE, "§6No claims yet",
                    "§7Use §f/claim §7to create one."));
        } else {
            for (int i = 0; i < Math.min(owned.size(), 45); i++) {
                Claim claim = owned.get(i);
                inv.setItem(i, item(Material.GRASS_BLOCK, "§6Claim " + (i + 1),
                        "§7Size: §f" + claim.getBlocks() + " blocks",
                        "§7World: §f" + claim.getWorld(),
                        "§7Bounds: §f" + claim.getMinX() + ", " + claim.getMinZ()
                                + " §7→ §f" + claim.getMaxX() + ", " + claim.getMaxZ(),
                        "§eClick to manage"));
            }
        }
        inv.setItem(49, item(Material.ARROW, "§7Close"));
        player.openInventory(inv);
    }

    private void openMain(Player player, Claim claim) {
        Inventory inv = Bukkit.createInventory(null, 27, MAIN);
        inv.setItem(10, item(Material.MAP, "§6Claim information",
                "§7Size: §f" + claim.getBlocks() + " blocks",
                "§7World: §f" + claim.getWorld(),
                "§7Bounds: §f" + claim.getMinX() + ", " + claim.getMinZ()
                        + " §7→ §f" + claim.getMaxX() + ", " + claim.getMaxZ()));

        inv.setItem(12, item(Material.PLAYER_HEAD, "§eTrusted players",
                "§7Trusted: §f" + claim.getTrusted().size(),
                "§eClick to manage"));

        inv.setItem(14, item(Material.GOLDEN_SHOVEL, "§6Resize claim",
                "§7Quick resize controls",
                "§eClick to open"));

        inv.setItem(16, item(Material.TNT, "§cAbandon claim",
                "§7Permanently remove this claim.",
                "§eClick to confirm"));

        inv.setItem(22, item(Material.ARROW, "§7Back to claims"));
        player.openInventory(inv);
    }

    private void openExpand(Player player, Claim claim) {
        Inventory inv = Bukkit.createInventory(null, 54, EXPAND);
        int[] slots = {10, 12, 14, 16, 28, 30, 32, 34};
        String[] dirs = {"North", "West", "East", "South", "North", "West", "East", "South"};
        int[] amounts = {10, 10, 10, 10, -10, -10, -10, -10};

        for (int i = 0; i < slots.length; i++) {
            String sign = amounts[i] > 0 ? "+" : "";
            inv.setItem(slots[i], item(amounts[i] > 0 ? Material.GOLD_BLOCK : Material.COAL_BLOCK,
                    "§6" + dirs[i] + " " + sign + amounts[i],
                    "§7Resize by §f" + Math.abs(amounts[i]) + " §7blocks",
                    "§eClick to apply"));
        }

        inv.setItem(22, item(Material.PAPER, "§eCurrent size",
                "§7" + claim.getBlocks() + " claim blocks"));
        inv.setItem(49, item(Material.ARROW, "§7Back"));
        player.openInventory(inv);
    }

    private void openTrust(Player player, Claim claim) {
        Inventory inv = Bukkit.createInventory(null, 54, TRUST);
        int slot = 0;
        for (UUID uuid : claim.getTrusted()) {
            if (slot >= 45) break;
            Player trusted = Bukkit.getPlayer(uuid);
            String name = trusted != null ? trusted.getName() : uuid.toString().substring(0, 8);
            inv.setItem(slot++, item(Material.PLAYER_HEAD, "§e" + name,
                    "§7Click to remove trust"));
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(player.getUniqueId())) continue;
            if (claim.getTrusted().contains(online.getUniqueId())) continue;
            if (slot >= 45) break;
            inv.setItem(slot++, item(Material.PLAYER_HEAD, "§a" + online.getName(),
                    "§7Click to trust this player"));
        }

        inv.setItem(49, item(Material.ARROW, "§7Back"));
        player.openInventory(inv);
    }

    private void openConfirm(Player player, Claim claim) {
        Inventory inv = Bukkit.createInventory(null, 27, CONFIRM);
        inv.setItem(11, item(Material.RED_WOOL, "§cAbandon claim",
                "§7This permanently removes the claim.",
                "§eClick to confirm"));
        inv.setItem(15, item(Material.LIME_WOOL, "§aCancel",
                "§7Keep your claim"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!List.of(MAIN, CLAIMS, TRUST, EXPAND, CONFIRM).contains(title)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;

        if (title.equals(CLAIMS)) {
            if (event.getRawSlot() == 49) { player.closeInventory(); return; }
            if (event.getRawSlot() < 45) {
                List<Claim> owned = plugin.getClaimManager().getOwned(player.getUniqueId());
                if (event.getRawSlot() < owned.size()) {
                    openMain(player, owned.get(event.getRawSlot()));
                }
            }
            return;
        }

        Claim claim = plugin.getClaimManager().getAt(player.getLocation());
        if (claim == null || !claim.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(color(plugin.getConfig().getString("messages.not-owner", "&cYou do not own this claim.")));
            player.closeInventory();
            return;
        }

        if (title.equals(MAIN)) {
            switch (event.getRawSlot()) {
                case 12 -> openTrust(player, claim);
                case 14 -> openExpand(player, claim);
                case 16 -> openConfirm(player, claim);
                case 22 -> openClaims(player);
                default -> {}
            }
        } else if (title.equals(EXPAND)) {
            if (event.getRawSlot() == 49) { openMain(player, claim); return; }
            int amount = switch (event.getRawSlot()) {
                case 10, 28 -> event.getRawSlot() == 10 ? 10 : -10;
                case 12, 30 -> event.getRawSlot() == 12 ? 10 : -10;
                case 14, 32 -> event.getRawSlot() == 14 ? 10 : -10;
                case 16, 34 -> event.getRawSlot() == 16 ? 10 : -10;
                default -> 0;
            };
            String direction = switch (event.getRawSlot()) {
                case 10, 28 -> "north";
                case 12, 30 -> "west";
                case 14, 32 -> "east";
                case 16, 34 -> "south";
                default -> "";
            };
            if (amount != 0) {
                String[] fakeArgs = {direction, String.valueOf(amount)};
                new ClaimExpandCommand(plugin).onCommand(player, null, "claimexpand", fakeArgs);
                Claim updated = plugin.getClaimManager().getAt(player.getLocation());
                if (updated != null) openExpand(player, updated);
            }
        } else if (title.equals(TRUST)) {
            if (event.getRawSlot() == 49) { openMain(player, claim); return; }
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            String display = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            if (display == null || display.isBlank()) return;
            Player target = Bukkit.getPlayerExact(display);
            if (target == null) {
                player.sendMessage("§cThat player is not online.");
                return;
            }
            if (claim.getTrusted().contains(target.getUniqueId())) {
                claim.getTrusted().remove(target.getUniqueId());
                player.sendMessage("§e" + target.getName() + " is no longer trusted.");
            } else {
                claim.getTrusted().add(target.getUniqueId());
                player.sendMessage("§a" + target.getName() + " is now trusted.");
            }
            plugin.getClaimManager().save();
            openTrust(player, claim);
        } else if (title.equals(CONFIRM)) {
            if (event.getRawSlot() == 11) {
                plugin.getClaimManager().delete(claim);
                player.closeInventory();
                player.sendMessage(color(plugin.getConfig().getString("messages.claim-abandoned", "&aClaim abandoned.")));
            } else if (event.getRawSlot() == 15) {
                openMain(player, claim);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        // Reserved for future menu state cleanup.
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
