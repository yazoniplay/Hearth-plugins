package dev.hearthsmp.core.data;

import java.util.UUID;

public final class HearthPlayerData {
    private final UUID uuid;
    private String name;
    private long firstSeen;
    private long lastSeen;

    public HearthPlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.firstSeen = System.currentTimeMillis();
        this.lastSeen = this.firstSeen;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getFirstSeen() { return firstSeen; }
    public long getLastSeen() { return lastSeen; }
    public void touch() { lastSeen = System.currentTimeMillis(); }
}
