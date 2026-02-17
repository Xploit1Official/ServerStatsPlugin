# IC Stats

Ein leichtgewichtiges Paper 1.21.4 Plugin für Server-Statistiken mit Web-Interface.

**Erstellt von:** frkn

## Features

- ⚡ **Lightweight** - Minimale Performance-Auswirkung
- 📊 **Live-Statistiken** - TPS, Spieleranzahl, RAM-Verbrauch, Uptime
- 🌐 **Eingebauter Webserver** - Keine externe Software nötig
- 🎨 **Modernes Dashboard** - Responsive Web-Interface mit Live-Updates
- 🔧 **Vollständig konfigurierbar** - Alles über config.yml steuerbar
- 🔌 **REST API** - JSON-Endpoint für eigene Integrationen

## Installation

1. Plugin-JAR in den `plugins` Ordner kopieren
2. Server starten
3. `config.yml` in `plugins/ICStats/` anpassen
4. `/icstats reload` ausführen

## Konfiguration

```yaml
webserver:
  enabled: true
  port: 8080
  host: "0.0.0.0"
  cors:
    enabled: true
    allowed-origins: "*"

server:
  name: "My Minecraft Server"
  description: "A cool Minecraft server"
  website: "https://example.com"

update-interval: 1

display:
  show-online-players: true
  show-max-players: true
  show-tps: true
  show-ram-usage: true
  show-ram-max: true
  show-uptime: true
```

## Verwendung

### Befehle

- `/icstats` - Zeigt Hilfe
- `/icstats reload` - Config neu laden
- `/icstats info` - Plugin-Informationen
- `/icstats status` - Aktuelle Statistiken

### Web-Interface

Nach dem Start ist das Dashboard erreichbar unter:
```
http://DEINE-IP:8080
```

### API-Endpoint

JSON-Daten abrufen:
```
http://DEINE-IP:8080/api/stats
```

Beispiel-Response:
```json
{
  "server": {
    "name": "My Minecraft Server",
    "description": "A cool Minecraft server",
    "website": "https://example.com"
  },
  "players": {
    "online": 5,
    "max": 20,
    "list": ["Player1", "Player2"]
  },
  "tps": 20.00,
  "memory": {
    "used": 2048,
    "max": 4096
  },
  "uptime": {
    "seconds": 3600,
    "formatted": "1h 0m 0s"
  },
  "timestamp": 1708185600000
}
```

## Eigene Website einbinden

### HTML Beispiel

```html
<!DOCTYPE html>
<html>
<head>
    <title>Server Stats</title>
</head>
<body>
    <h1>Server Status</h1>
    <div id="stats"></div>
    
    <script>
        async function loadStats() {
            const response = await fetch('http://DEINE-IP:8080/api/stats');
            const data = await response.json();
            
            document.getElementById('stats').innerHTML = `
                <p>Players: ${data.players.online}/${data.players.max}</p>
                <p>TPS: ${data.tps}</p>
                <p>RAM: ${data.memory.used} MB / ${data.memory.max} MB</p>
                <p>Uptime: ${data.uptime.formatted}</p>
            `;
        }
        
        loadStats();
        setInterval(loadStats, 1000);
    </script>
</body>
</html>
```

## Permissions

- `icstats.admin` - Zugriff auf alle Befehle (Standard: OP)

## Support

Bei Problemen oder Fragen, kontaktiere **Furkan** Per discord

## Version

**1.0.0** - Initial Release
