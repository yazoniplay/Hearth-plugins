package dev.hearthsmp.economy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class EconomyService {
    private final HearthEconomyPlugin plugin;
    private File file;
    private YamlConfiguration data;

    public EconomyService(HearthEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), "balances.yml");
        data = YamlConfiguration.loadConfiguration(file);
    }

    public void shutdown() {
        save();
    }

    public double getBalance(UUID uuid) {
        return data.getDouble("balances." + uuid, plugin.getConfig().getDouble("economy.starting-balance", 100.0));
    }

    public void setBalance(UUID uuid, double amount) {
        data.set("balances." + uuid, Math.max(0, amount));
        save();
    }

    public boolean withdraw(UUID uuid, double amount) {
        double balance = getBalance(uuid);
        if (amount <= 0 || balance < amount) return false;
        data.set("balances." + uuid, balance - amount);
        save();
        return true;
    }

    public void deposit(UUID uuid, double amount) {
        if (amount <= 0) return;
        data.set("balances." + uuid, getBalance(uuid) + amount);
        save();
    }

    public String format(double amount) {
        int decimals = plugin.getConfig().getInt("economy.decimals", 2);
        String symbol = plugin.getConfig().getString("economy.currency-symbol", "$");
        return symbol + String.format(java.util.Locale.US, "%." + decimals + "f", amount);
    }

    private void save() {
        if (data == null || file == null) return;
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save economy data: " + exception.getMessage());
        }
    }
}
