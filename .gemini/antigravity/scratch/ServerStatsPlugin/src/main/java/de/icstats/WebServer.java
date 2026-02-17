package de.icstats;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.Executors;

public class WebServer {

    private final ICStatsPlugin plugin;
    private final StatsCollector statsCollector;
    private final ConfigManager configManager;
    private HttpServer server;

    public WebServer(ICStatsPlugin plugin, StatsCollector statsCollector, ConfigManager configManager) {
        this.plugin = plugin;
        this.statsCollector = statsCollector;
        this.configManager = configManager;
    }

    public void start() {
        try {
            int port = configManager.getPort();
            String host = configManager.getHost();

            server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.createContext("/api/stats", new StatsHandler());
            server.createContext("/", new DashboardHandler());
            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();

            plugin.getLogger().info("Web server started on " + host + ":" + port);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start web server: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Web server stopped");
        }
    }

    private void addCorsHeaders(HttpExchange exchange) {
        if (configManager.isCorsEnabled()) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", configManager.getAllowedOrigins());
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        }
    }

    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String json = buildJsonResponse();
            byte[] response = json.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }

        private String buildJsonResponse() {
            StringBuilder json = new StringBuilder("{");

            json.append("\"server\":{");
            json.append("\"name\":\"").append(escapeJson(configManager.getServerName())).append("\",");
            json.append("\"description\":\"").append(escapeJson(configManager.getServerDescription())).append("\",");
            json.append("\"website\":\"").append(escapeJson(configManager.getServerWebsite())).append("\"");
            json.append("},");

            if (configManager.showOnlinePlayers() || configManager.showMaxPlayers()) {
                json.append("\"players\":{");
                if (configManager.showOnlinePlayers()) {
                    json.append("\"online\":").append(statsCollector.getOnlinePlayers());
                    if (configManager.showMaxPlayers())
                        json.append(",");
                }
                if (configManager.showMaxPlayers()) {
                    json.append("\"max\":").append(statsCollector.getMaxPlayers());
                }
                json.append(",\"list\":[");
                boolean first = true;
                for (String name : statsCollector.getPlayerNames()) {
                    if (!first)
                        json.append(",");
                    json.append("\"").append(escapeJson(name)).append("\"");
                    first = false;
                }
                json.append("]},");
            }

            if (configManager.showTps()) {
                json.append("\"tps\":").append(String.format(Locale.US, "%.2f", statsCollector.getTps())).append(",");
            }

            if (configManager.showRamUsage() || configManager.showRamMax()) {
                json.append("\"memory\":{");
                if (configManager.showRamUsage()) {
                    json.append("\"used\":").append(statsCollector.getUsedMemoryMB());
                    if (configManager.showRamMax())
                        json.append(",");
                }
                if (configManager.showRamMax()) {
                    json.append("\"max\":").append(statsCollector.getMaxMemoryMB());
                }
                json.append("},");
            }

            if (configManager.showUptime()) {
                json.append("\"uptime\":{");
                json.append("\"seconds\":").append(statsCollector.getUptimeSeconds()).append(",");
                json.append("\"formatted\":\"").append(escapeJson(statsCollector.getUptimeFormatted())).append("\"");
                json.append("},");
            }

            json.append("\"timestamp\":").append(System.currentTimeMillis());
            json.append("}");

            return json.toString();
        }

        private String escapeJson(String str) {
            return str.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String html = getHtmlDashboard();
            byte[] response = html.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }

        private String getHtmlDashboard() {
            return "<!DOCTYPE html><html lang=\"de\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>IC Stats Dashboard</title><style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px}::-webkit-scrollbar{width:8px}::-webkit-scrollbar-track{background:rgba(255,255,255,.1)}::-webkit-scrollbar-thumb{background:rgba(255,255,255,.3);border-radius:4px}::-webkit-scrollbar-thumb:hover{background:rgba(255,255,255,.5)}.container{max-width:1200px;width:100%}.header{text-align:center;margin-bottom:40px;animation:fadeInDown .8s ease}.header h1{color:#fff;font-size:3em;font-weight:700;text-shadow:0 4px 20px rgba(0,0,0,.3);margin-bottom:10px}.header p{color:rgba(255,255,255,.9);font-size:1.1em}.stats-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:25px;margin-bottom:30px}.stat-card{background:rgba(255,255,255,.95);border-radius:20px;padding:30px;box-shadow:0 10px 40px rgba(0,0,0,.2);transition:all .3s ease;animation:fadeInUp .8s ease;backdrop-filter:blur(10px)}.stat-card:hover{transform:translateY(-5px);box-shadow:0 15px 50px rgba(0,0,0,.3)}.stat-icon{font-size:2.5em;margin-bottom:15px;display:inline-block}.stat-label{color:#666;font-size:.9em;text-transform:uppercase;letter-spacing:1px;margin-bottom:8px;font-weight:600}.stat-value{color:#333;font-size:2.2em;font-weight:700;margin-bottom:5px}.stat-subtext{color:#999;font-size:.85em}.tps-bar{width:100%;height:8px;background:#e0e0e0;border-radius:10px;margin-top:10px;overflow:hidden}.tps-fill{height:100%;border-radius:10px;transition:all .5s ease}.players-list{background:rgba(255,255,255,.95);border-radius:20px;padding:30px;box-shadow:0 10px 40px rgba(0,0,0,.2);animation:fadeInUp 1s ease}.players-list h2{color:#333;margin-bottom:20px;font-size:1.5em}.player-tags{display:flex;flex-wrap:wrap;gap:10px}.player-tag{background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;padding:8px 16px;border-radius:20px;font-size:.9em;box-shadow:0 2px 10px rgba(0,0,0,.1);animation:fadeIn .5s ease}.footer{text-align:center;margin-top:30px;color:rgba(255,255,255,.8);animation:fadeIn 1.2s ease}.footer a{color:#fff;text-decoration:none;font-weight:600;transition:opacity .3s}.footer a:hover{opacity:.8}@keyframes fadeInDown{from{opacity:0;transform:translateY(-30px)}to{opacity:1;transform:translateY(0)}}@keyframes fadeInUp{from{opacity:0;transform:translateY(30px)}to{opacity:1;transform:translateY(0)}}@keyframes fadeIn{from{opacity:0}to{opacity:1}}</style></head><body><div class=\"container\"><div class=\"header\"><h1>🎮 IC Stats</h1><p>Live Server Statistics</p></div><div class=\"stats-grid\"><div class=\"stat-card\"><div class=\"stat-icon\">👥</div><div class=\"stat-label\">Players Online</div><div class=\"stat-value\" id=\"players\">-</div><div class=\"stat-subtext\" id=\"max-players\">-</div></div><div class=\"stat-card\"><div class=\"stat-icon\">⚡</div><div class=\"stat-label\">Server TPS</div><div class=\"stat-value\" id=\"tps\">-</div><div class=\"tps-bar\"><div class=\"tps-fill\" id=\"tps-bar\"></div></div></div><div class=\"stat-card\"><div class=\"stat-icon\">💾</div><div class=\"stat-label\">RAM Usage</div><div class=\"stat-value\" id=\"ram\">-</div><div class=\"stat-subtext\" id=\"ram-max\">-</div></div><div class=\"stat-card\"><div class=\"stat-icon\">⏱️</div><div class=\"stat-label\">Uptime</div><div class=\"stat-value\" id=\"uptime\" style=\"font-size:1.5em\">-</div></div></div><div class=\"players-list\"><h2>Online Players</h2><div class=\"player-tags\" id=\"player-list\"></div></div><div class=\"footer\"><p>Powered by <strong>IC Stats v1.0</strong> • Created by <strong>frkn</strong></p></div></div><script>async function fetchStats(){try{const e=await fetch('/api/stats'),t=await e.json();document.getElementById('players').textContent=t.players.online,document.getElementById('max-players').textContent=`Max: ${t.players.max}`;const a=t.tps,s=a/20*100,l=s>90?'#4caf50':s>70?'#ff9800':'#f44336';document.getElementById('tps').textContent=a.toFixed(2),document.getElementById('tps-bar').style.width=s+'%',document.getElementById('tps-bar').style.background=l,document.getElementById('ram').textContent=t.memory.used+' MB',document.getElementById('ram-max').textContent=`Max: ${t.memory.max} MB`,document.getElementById('uptime').textContent=t.uptime.formatted;const n=document.getElementById('player-list');n.innerHTML='',t.players.list.length>0?t.players.list.forEach(e=>{const t=document.createElement('div');t.className='player-tag',t.textContent=e,n.appendChild(t)}):n.innerHTML='<div style=\"color:#999\">No players online</div>'}catch(e){console.error('Error fetching stats:',e)}}fetchStats(),setInterval(fetchStats,1e3)</script></body></html>";
        }
    }
}
