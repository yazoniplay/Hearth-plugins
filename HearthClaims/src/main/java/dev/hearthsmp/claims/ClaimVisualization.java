package dev.hearthsmp.claims;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class ClaimVisualization {
    private final HearthClaimsPlugin plugin;

    public ClaimVisualization(HearthClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    public void show(Player player, Claim claim) {
        if (!plugin.getConfig().getBoolean("visualization.enabled", true)) return;

        Location center = player.getLocation().clone();
        double y = center.getY() + 1.0;

        for (int x = claim.getMinX(); x <= claim.getMaxX(); x += Math.max(1, (claim.getMaxX() - claim.getMinX()) / 20)) {
            spawn(player, new Location(player.getWorld(), x + 0.5, y, claim.getMinZ() + 0.5));
            spawn(player, new Location(player.getWorld(), x + 0.5, y, claim.getMaxZ() + 0.5));
        }

        for (int z = claim.getMinZ(); z <= claim.getMaxZ(); z += Math.max(1, (claim.getMaxZ() - claim.getMinZ()) / 20)) {
            spawn(player, new Location(player.getWorld(), claim.getMinX() + 0.5, y, z + 0.5));
            spawn(player, new Location(player.getWorld(), claim.getMaxX() + 0.5, y, z + 0.5));
        }
    }

    private void spawn(Player player, Location location) {
        player.spawnParticle(Particle.FLAME, location, 2, 0, 0, 0, 0);
    }
}
