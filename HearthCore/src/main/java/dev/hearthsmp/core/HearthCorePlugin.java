package dev.hearthsmp.core;

import dev.hearthsmp.core.command.HearthCommand;
import dev.hearthsmp.core.config.HearthConfig;
import dev.hearthsmp.core.data.PlayerDataService;
import dev.hearthsmp.core.placeholder.HearthPlaceholderExpansion;
import dev.hearthsmp.core.ui.HearthUI;
import org.bukkit.plugin.java.JavaPlugin;

public final class HearthCorePlugin extends JavaPlugin {
    private HearthConfig hearthConfig;
    private PlayerDataService playerDataService;
    private HearthUI hearthUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        hearthConfig = new HearthConfig(this);
        playerDataService = new PlayerDataService(this);
        hearthUI = new HearthUI(this);

        hearthConfig.load();
        playerDataService.start();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new HearthPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI integration enabled.");
        }

        getCommand("hearth").setExecutor(new HearthCommand(this));
        getCommand("hearth").setTabCompleter(new HearthCommand(this));

        getLogger().info("HearthCore is warming the Hearth.");
        getLogger().info("UI, configuration, and player-data services are ready.");
    }

    @Override
    public void onDisable() {
        if (playerDataService != null) {
            playerDataService.shutdown();
        }
        getLogger().info("HearthCore is shutting down.");
    }

    public HearthConfig getHearthConfig() {
        return hearthConfig;
    }

    public PlayerDataService getPlayerDataService() {
        return playerDataService;
    }

    public HearthUI getHearthUI() {
        return hearthUI;
    }
}
