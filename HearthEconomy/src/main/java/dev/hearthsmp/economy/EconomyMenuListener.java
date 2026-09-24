package dev.hearthsmp.economy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class EconomyMenuListener implements Listener {
    private final HearthEconomyPlugin plugin;

    public EconomyMenuListener(HearthEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        String expected = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.title", "&8🔥 Hearth Economy"));
        if (!title.equals(expected)) return;
        event.setCancelled(true);
    }
}
