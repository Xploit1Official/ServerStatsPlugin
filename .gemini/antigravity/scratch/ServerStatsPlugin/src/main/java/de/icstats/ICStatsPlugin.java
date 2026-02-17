package de.icstats;

import org.bukkit.plugin.java.JavaPlugin;

public class ICStatsPlugin extends JavaPlugin {

    private StatsCollector statsCollector;
    private WebServer webServer;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        statsCollector = new StatsCollector(this);
        statsCollector.start();

        if (configManager.isWebServerEnabled()) {
            webServer = new WebServer(this, statsCollector, configManager);
            webServer.start();
        }

        getCommand("icstats").setExecutor(new StatsCommand(this, configManager, webServer));

        getLogger().info("ICStats v" + getDescription().getVersion() + " by frkn enabled!");
        if (configManager.isWebServerEnabled()) {
            getLogger().info("Web interface: http://" + configManager.getHost() + ":" + configManager.getPort());
        }
    }

    @Override
    public void onDisable() {
        if (statsCollector != null) {
            statsCollector.stop();
        }
        if (webServer != null) {
            webServer.stop();
        }
        getLogger().info("ICStats disabled!");
    }

    public StatsCollector getStatsCollector() {
        return statsCollector;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
