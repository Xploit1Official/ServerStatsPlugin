package de.icstats;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StatsCommand implements CommandExecutor {

    private final ICStatsPlugin plugin;
    private final ConfigManager configManager;
    private final WebServer webServer;

    public StatsCommand(ICStatsPlugin plugin, ConfigManager configManager, WebServer webServer) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.webServer = webServer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("icstats.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                configManager.reload();
                sender.sendMessage(ChatColor.GREEN + "IC Stats configuration reloaded!");
                sender.sendMessage(ChatColor.YELLOW + "Note: Web server restart required for some changes.");
                break;

            case "info":
                sendInfo(sender);
                break;

            case "status":
                sendStatus(sender);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        sender.sendMessage(ChatColor.AQUA + "IC Stats v" + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "by frkn For IslandCraft :D");
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        sender.sendMessage(ChatColor.YELLOW + "/icstats reload " + ChatColor.WHITE + "- Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/icstats info " + ChatColor.WHITE + "- Show plugin info");
        sender.sendMessage(ChatColor.YELLOW + "/icstats status " + ChatColor.WHITE + "- Show current stats");
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        sender.sendMessage(ChatColor.AQUA + "IC Stats Information");
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.WHITE + "frkn");

        if (configManager.isWebServerEnabled()) {
            String url = "http://" + configManager.getHost() + ":" + configManager.getPort();
            sender.sendMessage(ChatColor.YELLOW + "Web Interface: " + ChatColor.GREEN + url);
            sender.sendMessage(ChatColor.YELLOW + "API Endpoint: " + ChatColor.GREEN + url + "/api/stats");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Web Server: " + ChatColor.RED + "Disabled");
        }

        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
    }

    private void sendStatus(CommandSender sender) {
        StatsCollector stats = plugin.getStatsCollector();

        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
        sender.sendMessage(ChatColor.AQUA + "Current Server Statistics");
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");

        if (configManager.showOnlinePlayers()) {
            sender.sendMessage(ChatColor.YELLOW + "Players: " + ChatColor.WHITE +
                    stats.getOnlinePlayers() + "/" + stats.getMaxPlayers());
        }

        if (configManager.showTps()) {
            double tps = stats.getTps();
            ChatColor tpsColor = tps > 18 ? ChatColor.GREEN : tps > 15 ? ChatColor.YELLOW : ChatColor.RED;
            sender.sendMessage(ChatColor.YELLOW + "TPS: " + tpsColor + String.format("%.2f", tps));
        }

        if (configManager.showRamUsage()) {
            sender.sendMessage(ChatColor.YELLOW + "RAM: " + ChatColor.WHITE +
                    stats.getUsedMemoryMB() + " MB / " + stats.getMaxMemoryMB() + " MB");
        }

        if (configManager.showUptime()) {
            sender.sendMessage(ChatColor.YELLOW + "Uptime: " + ChatColor.WHITE + stats.getUptimeFormatted());
        }

        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════");
    }
}
