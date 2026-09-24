package dev.hearthsmp.auth;

import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AuthService {
    private final HearthAuthPlugin plugin;
    private final Set<UUID> authenticated = new HashSet<>();
    private Connection connection;

    public AuthService(HearthAuthPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + new java.io.File(plugin.getDataFolder(), "auth.db").getAbsolutePath());
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS accounts (
                            username TEXT PRIMARY KEY COLLATE NOCASE,
                            uuid TEXT NOT NULL,
                            salt TEXT NOT NULL,
                            hash TEXT NOT NULL
                        )
                        """);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not open HearthAuth database.", exception);
        }
    }

    public void shutdown() {
        authenticated.clear();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public boolean isRegistered(Player player) {
        return findAccount(player.getName()) != null;
    }

    public boolean isAuthenticated(Player player) {
        return authenticated.contains(player.getUniqueId());
    }

    public boolean register(Player player, String password) {
        if (isRegistered(player)) return false;

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String saltText = Base64.getEncoder().encodeToString(salt);
        String hashText = hash(password, salt);

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO accounts(username, uuid, salt, hash) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, player.getName());
            statement.setString(2, player.getUniqueId().toString());
            statement.setString(3, saltText);
            statement.setString(4, hashText);
            statement.executeUpdate();
            authenticated.add(player.getUniqueId());
            return true;
        } catch (SQLException exception) {
            plugin.getLogger().warning("Could not register account for " + player.getName() + ": " + exception.getMessage());
            return false;
        }
    }

    public boolean login(Player player, String password) {
        Account account = findAccount(player.getName());
        if (account == null) return false;

        byte[] salt = Base64.getDecoder().decode(account.salt());
        String supplied = hash(password, salt);
        if (!MessageDigest.isEqual(
                supplied.getBytes(StandardCharsets.UTF_8),
                account.hash().getBytes(StandardCharsets.UTF_8))) {
            return false;
        }

        authenticated.add(player.getUniqueId());
        return true;
    }

    public void logout(Player player) {
        authenticated.remove(player.getUniqueId());
    }

    private Account findAccount(String username) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT username, uuid, salt, hash FROM accounts WHERE username = ? COLLATE NOCASE")) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                return new Account(
                        result.getString("username"),
                        result.getString("uuid"),
                        result.getString("salt"),
                        result.getString("hash"));
            }
        } catch (SQLException exception) {
            plugin.getLogger().warning("Could not read HearthAuth account: " + exception.getMessage());
            return null;
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

    private record Account(String username, String uuid, String salt, String hash) {}
}
