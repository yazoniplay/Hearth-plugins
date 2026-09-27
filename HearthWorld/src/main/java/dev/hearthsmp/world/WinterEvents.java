package dev.hearthsmp.world;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class WinterEvents implements Listener {
    private final HearthWorldPlugin plugin;
    private final Map<UUID, Long> lastDiscovery = new HashMap<>();
    private final Set<UUID> firstFlame = new HashSet<>();

    public WinterEvents(HearthWorldPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo() == null) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        World world = player.getWorld();
        if (!world.getName().equalsIgnoreCase(plugin.getConfig().getString("world.name", "world"))) return;

        long now = System.currentTimeMillis();
        if (now - lastDiscovery.getOrDefault(player.getUniqueId(), 0L) < 90_000L) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.0025) return;

        lastDiscovery.put(player.getUniqueId(), now);
        discoverFrozenLandmark(player);
    }

    public void startBlizzards(World world) {
        if (!plugin.getConfig().getBoolean("events.blizzards.enabled", true)) return;

        long interval = Math.max(20L, plugin.getConfig().getLong("events.blizzards.interval-ticks", 600L));
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (world.getPlayers().isEmpty()) return;
            if (ThreadLocalRandom.current().nextDouble() > 0.22) return;

            world.setStorm(true);
            world.setThundering(false);
            world.setWeatherDuration(
                    plugin.getConfig().getInt("events.blizzards.duration-ticks", 1200)
            );

            Bukkit.broadcast(Component.text("❄ A BLIZZARD IS MOVING IN", NamedTextColor.AQUA));
            Bukkit.broadcast(Component.text("The frozen wilderness has become dangerous. Find shelter.", NamedTextColor.GRAY));

            for (Player player : world.getPlayers()) {
                player.playSound(player.getLocation(), "block.snow.place", 1.0f, 0.65f);
            }

            spawnBlizzardScouts(world);
        }, interval, interval);
    }

    private void spawnBlizzardScouts(World world) {
        for (Player player : world.getPlayers()) {
            if (ThreadLocalRandom.current().nextDouble() > 0.35) continue;

            Location location = player.getLocation().clone().add(
                    ThreadLocalRandom.current().nextInt(-12, 13), 0,
                    ThreadLocalRandom.current().nextInt(-12, 13)
            );
            location.setY(world.getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 1);

            Snowman snowman = (Snowman) world.spawnEntity(location, EntityType.SNOW_GOLEM);
            snowman.setCustomName("§bBlizzard Scout");
            snowman.setCustomNameVisible(true);
        }
    }

    private void discoverFrozenLandmark(Player player) {
        player.sendMessage(Component.text("❄ You discovered something in the frozen wilderness.", NamedTextColor.AQUA));
        player.sendMessage(Component.text("The First Flame remembers every explorer.", NamedTextColor.GOLD));

        if (firstFlame.add(player.getUniqueId())) {
            player.getInventory().addItem(createDiscoveryToken());
            player.sendMessage(Component.text("🔥 First discovery! You found a Frostbound Ember.", NamedTextColor.GOLD));
        } else {
            player.getInventory().addItem(new ItemStack(Material.SNOWBALL, 8));
        }
    }

    private ItemStack createDiscoveryToken() {
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Frostbound Ember", NamedTextColor.GOLD));
            meta.lore(java.util.List.of(
                    Component.text("A spark that refuses to die.", NamedTextColor.GRAY),
                    Component.text("Season 1 • The First Flame", NamedTextColor.AQUA)
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
}
