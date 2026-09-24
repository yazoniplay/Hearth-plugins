package dev.hearthsmp.auth;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AuthService {
    private final HearthAuthPlugin plugin;
    private final Set<UUID> authenticated = new HashSet<>();
    private File file;
    private YamlConfiguration data;

    public AuthService(HearthAuthPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            throw new IllegalStateException("Could not create HearthAuth data folder.");
        }

        file = new File(plugin.getDataFolder(), "accounts.yml");
        data = YamlConfiguration.loadConfiguration(file);
    }

    public void shutdown() {
        authenticated.clear();
        save();
    }

    public boolean isRegistered(Player player) {
        return data.contains(accountPath(player.getName()));
    }

    public boolean isAuthenticated(Player player) {
        return authenticated.contains(player.getUniqueId());
    }

    public boolean register(Player player, String password) {
        if (isRegistered(player)) return false;

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        String path = accountPath(player.getName());
        data.set(path + ".uuid", player.getUniqueId().toString());
        data.set(path + ".salt", Base64.getEncoder().encodeToString(salt));
        data.set(path + ".hash", hash(password, salt));
        save();

        authenticated.add(player.getUniqueId());
        return true;
    }

    public boolean login(Player player, String password) {
        String path = accountPath(player.getName());
        if (!data.contains(path)) return false;

        String saltText = data.getString(path + ".salt");
        String storedHash = data.getString(path + ".hash");
        if (saltText == null || storedHash == null) return false;

        byte[] salt;
        try {
            salt = Base64.getDecoder().decode(saltText);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        String suppliedHash = hash(password, salt);
        if (!MessageDigest.isEqual(
                suppliedHash.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8))) {
            return false;
        }

        authenticated.add(player.getUniqueId());
        return true;
    }

    public void logout(Player player) {
        authenticated.remove(player.getUniqueId());
    }

    private String accountPath(String username) {
        return "accounts." + username.toLowerCase(java.util.Locale.ROOT);
    }

    private void save() {
        if (data == null || file == null) return;
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save HearthAuth accounts: " + exception.getMessage());
        }
    }

    private String hash(String password, byte[] salt) {
        try {
            int iterations = plugin.getConfig().getInt("security.iterations", 210000);
            var spec = new javax.crypto.spec.PBEKeySpec(
                    password.toCharArray(), salt, iterations, 256);
            byte[] hash = javax.crypto.SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
            spec.clearPassword();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash HearthAuth password.", exception);
        }
    }
}
