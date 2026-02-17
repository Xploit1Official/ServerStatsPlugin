package de.icstats;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class StatsCollector {

    private final ICStatsPlugin plugin;
    private final long startTime;
    private int taskId = -1;

    private volatile double currentTps = 20.0;
    private volatile int onlinePlayers = 0;
    private volatile int maxPlayers = 0;
    private volatile long usedMemory = 0;
    private volatile long maxMemory = 0;
    private volatile List<String> playerNames = new ArrayList<>();

    public StatsCollector(ICStatsPlugin plugin) {
        this.plugin = plugin;
        this.startTime = System.currentTimeMillis();
    }

    public void start() {
        int interval = plugin.getConfigManager().getUpdateInterval() * 20;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateStats, 0L, interval);
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void updateStats() {
        try {
            currentTps = Bukkit.getTPS()[0];
        } catch (Exception e) {
            currentTps = 20.0;
        }

        onlinePlayers = Bukkit.getOnlinePlayers().size();
        maxPlayers = Bukkit.getMaxPlayers();

        Runtime runtime = Runtime.getRuntime();
        maxMemory = runtime.maxMemory();
        usedMemory = runtime.totalMemory() - runtime.freeMemory();

        playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
    }

    public double getTps() {
        return Math.min(20.0, Math.max(0.0, currentTps));
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public long getUsedMemoryMB() {
        return usedMemory / (1024 * 1024);
    }

    public long getMaxMemoryMB() {
        return maxMemory / (1024 * 1024);
    }

    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    public List<String> getPlayerNames() {
        return new ArrayList<>(playerNames);
    }

    public String getUptimeFormatted() {
        long seconds = getUptimeSeconds();
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, secs);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}
