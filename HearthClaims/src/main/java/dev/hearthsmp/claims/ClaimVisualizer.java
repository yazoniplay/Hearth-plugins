package dev.hearthsmp.claims;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public final class ClaimVisualizer {
    private final HearthClaimsPlugin plugin;

    public ClaimVisualizer(HearthClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    public void show(Player player, Claim claim) {
        if (!plugin.getConfig().getBoolean("visualization.enabled", true)) return;

        int spacing = Math.max(1, plugin.getConfig().getInt("visualization.spacing", 3));
        double y = player.getLocation().getY() + 1.0;

        for (int x = claim.getMinX(); x <= claim.getMaxX(); x += spacing) {
            spawn(player, new Location(player.getWorld(), x + 0.5, y, claim.getMinZ() + 0.5));
            spawn(player, new Location(player.getWorld(), x + 0.5, y, claim.getMaxZ() + 0.5));
        }
        for (int z = claim.getMinZ(); z <= claim.getMaxZ(); z += spacing) {
            spawn(player, new Location(player.getWorld(), claim.getMinX() + 0.5, y, z + 0.5));
            spawn(player, new Location(player.getWorld(), claim.getMaxX() + 0.5, y, z + 0.5));
        }
    }

    private void spawn(Player player, Location location) {
        player.spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, 0);
    }
}
