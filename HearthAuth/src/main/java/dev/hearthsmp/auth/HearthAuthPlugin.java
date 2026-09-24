package dev.hearthsmp.auth;

import org.bukkit.plugin.java.JavaPlugin;

public final class HearthAuthPlugin extends JavaPlugin {
    private AuthService authService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        authService = new AuthService(this);
        authService.start();

        AuthCommand command = new AuthCommand(this);
        getCommand("register").setExecutor(command);
        getCommand("login").setExecutor(command);
        getCommand("logout").setExecutor(command);

        getServer().getPluginManager().registerEvents(new AuthListener(this), this);

        getLogger().info("HearthAuth is protecting player accounts.");
    }

    @Override
    public void onDisable() {
        if (authService != null) {
            authService.shutdown();
        }
    }

    public AuthService getAuthService() {
        return authService;
    }
}
