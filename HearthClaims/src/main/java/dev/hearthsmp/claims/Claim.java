package dev.hearthsmp.claims;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Claim {
    private final UUID id;
    private final UUID owner;
    private final String world;
    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;
    private final Set<UUID> trusted = new HashSet<>();

    public Claim(UUID id, UUID owner, Location first, Location second) {
        this.id = id;
        this.owner = owner;
        this.world = first.getWorld().getName();
        this.minX = Math.min(first.getBlockX(), second.getBlockX());
        this.maxX = Math.max(first.getBlockX(), second.getBlockX());
        this.minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        this.maxZ = Math.max(first.getBlockZ(), second.getBlockZ());
    }

    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public String getWorld() { return world; }
    public int getMinX() { return minX; }
    public int getMaxX() { return maxX; }
    public int getMinZ() { return minZ; }
    public int getMaxZ() { return maxZ; }
    public Set<UUID> getTrusted() { return trusted; }

    public int getBlocks() {
        return (maxX - minX + 1) * (maxZ - minZ + 1);
    }

    public boolean contains(Location location) {
        return location.getWorld() != null
                && location.getWorld().getName().equals(world)
                && location.getBlockX() >= minX && location.getBlockX() <= maxX
                && location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ;
    }

    public boolean canBuild(UUID player) {
        return owner.equals(player) || trusted.contains(player);
    }
}
