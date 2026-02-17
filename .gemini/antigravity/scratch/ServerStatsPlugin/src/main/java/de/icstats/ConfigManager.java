package de.icstats;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final ICStatsPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(ICStatsPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public boolean isWebServerEnabled() {
        return config.getBoolean("webserver.enabled", true);
    }

    public int getPort() {
        return config.getInt("webserver.port", 8080);
    }

    public String getHost() {
        return config.getString("webserver.host", "0.0.0.0");
    }

    public boolean isCorsEnabled() {
        return config.getBoolean("webserver.cors.enabled", true);
    }

    public String getAllowedOrigins() {
        return config.getString("webserver.cors.allowed-origins", "*");
    }

    public String getServerName() {
        return config.getString("server.name", "My Minecraft Server");
    }

    public String getServerDescription() {
        return config.getString("server.description", "A cool Minecraft server");
    }

    public String getServerWebsite() {
        return config.getString("server.website", "https://example.com");
    }

    public int getUpdateInterval() {
        return config.getInt("update-interval", 1);
    }

    public boolean showOnlinePlayers() {
        return config.getBoolean("display.show-online-players", true);
    }

    public boolean showMaxPlayers() {
        return config.getBoolean("display.show-max-players", true);
    }

    public boolean showTps() {
        return config.getBoolean("display.show-tps", true);
    }

    public boolean showRamUsage() {
        return config.getBoolean("display.show-ram-usage", true);
    }

    public boolean showRamMax() {
        return config.getBoolean("display.show-ram-max", true);
    }

    public boolean showUptime() {
        return config.getBoolean("display.show-uptime", true);
    }
}
