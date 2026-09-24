package dev.hearthsmp.claims;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class ClaimManager {
    private final HearthClaimsPlugin plugin;
    private final Map<UUID, Claim> claims = new LinkedHashMap<>();
    private final Map<UUID, Location[]> selections = new HashMap<>();
    private File file;
    private YamlConfiguration data;

    public ClaimManager(HearthClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "claims.yml");
        data = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = data.getConfigurationSection("claims");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                UUID owner = UUID.fromString(section.getString(key + ".owner"));
                String world = section.getString(key + ".world");
                int minX = section.getInt(key + ".min-x");
                int maxX = section.getInt(key + ".max-x");
                int minZ = section.getInt(key + ".min-z");
                int maxZ = section.getInt(key + ".max-z");

                Location first = new Location(plugin.getServer().getWorld(world), minX, 0, minZ);
                Location second = new Location(plugin.getServer().getWorld(world), maxX, 0, maxZ);
                Claim claim = new Claim(id, owner, first, second);

                for (String trusted : section.getStringList(key + ".trusted")) {
                    claim.getTrusted().add(UUID.fromString(trusted));
                }
                claims.put(id, claim);
            } catch (Exception ignored) {
                plugin.getLogger().warning("Could not load claim " + key);
            }
        }
    }

    public void save() {
        if (data == null) data = new YamlConfiguration();
        data.set("claims", null);

        for (Claim claim : claims.values()) {
            String path = "claims." + claim.getId();
            data.set(path + ".owner", claim.getOwner().toString());
            data.set(path + ".world", claim.getWorld());
            data.set(path + ".min-x", claim.getMinX());
            data.set(path + ".max-x", claim.getMaxX());
            data.set(path + ".min-z", claim.getMinZ());
            data.set(path + ".max-z", claim.getMaxZ());
            data.set(path + ".trusted", claim.getTrusted().stream().map(UUID::toString).toList());
        }

        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save claims.yml: " + e.getMessage());
        }
    }

    public Claim getAt(Location location) {
        for (Claim claim : claims.values()) {
            if (claim.contains(location)) return claim;
        }
        return null;
    }

    public List<Claim> getOwned(UUID owner) {
        return claims.values().stream().filter(c -> c.getOwner().equals(owner)).toList();
    }

    public boolean overlaps(Location first, Location second) {
        String world = first.getWorld().getName();
        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());

        return claims.values().stream().anyMatch(c ->
                c.getWorld().equals(world)
                        && minX <= c.getMaxX() && maxX >= c.getMinX()
                        && minZ <= c.getMaxZ() && maxZ >= c.getMinZ());
    }

    public Claim create(UUID owner, Location first, Location second) {
        Claim claim = new Claim(UUID.randomUUID(), owner, first, second);
        claims.put(claim.getId(), claim);
        save();
        return claim;
    }

    public void delete(Claim claim) {
        claims.remove(claim.getId());
        save();
    }

    public Map<UUID, Location[]> getSelections() { return selections; }

    public Location[] getSelection(UUID player) {
        return selections.computeIfAbsent(player, k -> new Location[2]);
    }

    public void clearSelection(UUID player) {
        selections.remove(player);
    }
}
