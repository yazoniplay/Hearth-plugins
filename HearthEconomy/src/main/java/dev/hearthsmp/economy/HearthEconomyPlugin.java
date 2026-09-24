package dev.hearthsmp.economy;

import org.bukkit.plugin.java.JavaPlugin;

public final class HearthEconomyPlugin extends JavaPlugin {
    private EconomyService economyService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        economyService = new EconomyService(this);
        economyService.start();

        EconomyCommand command = new EconomyCommand(this);
        getCommand("economy").setExecutor(command);
        getCommand("pay").setExecutor(command);

        getServer().getPluginManager().registerEvents(new EconomyMenuListener(this), this);
        getLogger().info("HearthEconomy is open for business.");
    }

    @Override
    public void onDisable() {
        if (economyService != null) economyService.shutdown();
    }

    public EconomyService getEconomyService() {
        return economyService;
    }
}
