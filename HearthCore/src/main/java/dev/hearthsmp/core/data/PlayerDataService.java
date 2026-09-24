package dev.hearthsmp.core.data;

import dev.hearthsmp.core.HearthCorePlugin;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataService {
    private final HearthCorePlugin plugin;
    private final Map<UUID, HearthPlayerData> cache = new ConcurrentHashMap<>();

    public PlayerDataService(HearthCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        plugin.getServer().getPluginManager().registerEvents(new PlayerDataListener(this), plugin);
    }

    public HearthPlayerData get(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(),
                uuid -> new HearthPlayerData(uuid, player.getName()));
    }

    public void shutdown() {
        cache.clear();
    }
}
