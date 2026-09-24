package dev.hearthsmp.core.config;

import dev.hearthsmp.core.HearthCorePlugin;

public final class HearthConfig {
    private final HearthCorePlugin plugin;

    public HearthConfig(HearthCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
    }

    public String serverName() {
        return plugin.getConfig().getString("server.name", "Hearth SMP");
    }

    public String serverTagline() {
        return plugin.getConfig().getString("server.tagline", "Build something worth coming home to.");
    }
}
