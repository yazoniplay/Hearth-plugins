package dev.hearthsmp.core.data;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerDataListener implements Listener {
    private final PlayerDataService service;

    public PlayerDataListener(PlayerDataService service) {
        this.service = service;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        service.get(event.getPlayer()).touch();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        service.get(event.getPlayer()).touch();
    }
}
