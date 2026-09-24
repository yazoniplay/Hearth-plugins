package dev.hearthsmp.core;

import org.bukkit.plugin.java.JavaPlugin;

public final class HearthCorePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("HearthCore is warming the Hearth.");
    }

    @Override
    public void onDisable() {
        getLogger().info("HearthCore is shutting down.");
    }
}
