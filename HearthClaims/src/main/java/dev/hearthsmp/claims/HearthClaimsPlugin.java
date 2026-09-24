package dev.hearthsmp.claims;

import org.bukkit.plugin.java.JavaPlugin;

public final class HearthClaimsPlugin extends JavaPlugin {
    private ClaimManager claimManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        claimManager = new ClaimManager(this);
        claimManager.load();

        getServer().getPluginManager().registerEvents(new ClaimListener(this), this);
        getServer().getPluginManager().registerEvents(new ClaimProtectionListener(this), this);

        ClaimCommand command = new ClaimCommand(this);
        getCommand("claim").setExecutor(command);
        getCommand("claim").setTabCompleter(command);

        TrustCommand trust = new TrustCommand(this);
        getCommand("trust").setExecutor(trust);
        getCommand("untrust").setExecutor(trust);

        getCommand("abandonclaim").setExecutor(new AbandonClaimCommand(this));
        getCommand("claims").setExecutor(new ClaimsCommand(this));
        getCommand("claiminfo").setExecutor(new ClaimInfoCommand(this));

        getLogger().info("HearthClaims is protecting the Hearth.");
    }

    @Override
    public void onDisable() {
        if (claimManager != null) {
            claimManager.save();
        }
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }
}
