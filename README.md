# Adelheit - Discord Musikbot

## 📖 Projektbeschreibung

Adelheit ist ein Discord-Musikbot, der entwickelt wurde, um nahtlose Musikwiedergabe und Verwaltung in Discord-Servern zu ermöglichen.

## 🚧 Pre-Release Hinweis
**Version: 0.3.0-alpha**

⚠️ **Achtung:** Dies ist eine frühe Alpha-Version. Der Bot kann Fehler enthalten und die Funktionalität ist möglicherweise eingeschränkt.

### Bekannte Einschränkungen
- Begrenzte Fehlerbehandlung bei Netzwerkproblemen
- Fehlende GUI für Konfigurationsänderungen
- Keine Unterstützung für Spotify-Links

## 🆕 Was ist neu in 0.3.0-alpha
- **Warteraummusik-Modus**: Automatisches Abspielen von Musik in speziellen Warteräumen
- **Benutzerfreundliche Startskripte**: Einfacher Start mit Windows (.bat) oder Linux (.sh)
- Verbesserte Stabilität und Ressourcenoptimierung
- Verschiedene kleinere Fehlerbehebungen

## ✨ Funktionen

- 🎵 Musik von SoundCloud und lokalen Bibliotheken abspielen
- 🎧 Warteraummusik mit automatischer Aktivierung/Deaktivierung
- ⏭️ Tracks überspringen
- ⏸️ Wiedergabe pausieren und fortsetzen
- 🔊 Lautstärke anpassen
- 📋 Warteschlange verwalten
- 🎼 Lokale Playlists abspielen und durchsuchen
- 🔄 Automatisches Trennen der Verbindung bei Inaktivität
- 📊 Ausführliche Statusinformationen zum aktuellen Track

## 🛠️ Technische Details

### Verwendete Technologien
- Java 21
- JDA (Java Discord API) v5.3.0
- LavaPlayer für Audio-Streaming
- Maven für Projektmanagement
- Logback für Logging

### Abhängigkeiten
- Discord JDA
- LavaPlayer
- Gson
- Logback Classic

## 🚀 Installation

### Voraussetzungen
- Java 21 oder höher
- Discord Bot Token
- Internet-Verbindung

### Einfache Installation (Neu in 0.3.0!)

#### Windows
1. Installiere Java (21 oder höher)
2. Lade die neueste Version von der [Releases-Seite](https://github.com/Quartel/discord-adelheit/releases) herunter
3. Entpacke die ZIP-Datei in einen Ordner deiner Wahl
4. Starte den Bot mit Doppelklick auf `start-bot.bat`

#### Linux
1. Installiere Java (21 oder höher)
2. Lade die neueste Version herunter
   ```bash
   wget https://github.com/Quartel/discord-adelheit/releases/download/v0.3.1-alpha/discord-adelheit-0.3.1-alpha-distribution.zip
   ```
3. Entpacke die ZIP-Datei
   ```bash
   unzip discord-adelheit-0.3.1-alpha-distribution.zip
   cd adelheit-discord-bot
   ```
4. Starte den Bot
   ```bash
   ./start-bot.sh
   ```
5. Folge den Anweisungen zur Erstkonfiguration

### Manuelle Installation Linux (für Entwickler)

#### 1. Systemaktualisierung
```bash
sudo apt update
sudo apt upgrade -y
```

#### 2. Java Installation
```bash
sudo apt install -y openjdk-21-jdk
java --version  # Überprüfen der Installation
```

#### 3. Maven und Git Installation
```bash
sudo apt install -y maven git
mvn --version   # Überprüfen der Installation
```

#### 4. Repository klonen
```bash
# Wechseln Sie in Ihr Projektverzeichnis
cd ~/Projects

# Repository klonen
git clone https://github.com/Quartel/discord-adelheit.git
cd discord-adelheit
```

#### 5. Konfiguration vorbereiten
```bash
# Konfigurationsdatei kopieren
cp src/main/resources/config.properties.example src/main/resources/config.properties

# Bearbeiten Sie die Konfigurationsdatei
nano src/main/resources/config.properties
```

Passen Sie folgende Werte an:
- `bot.token=IHR_DISCORD_BOT_TOKEN`
- Optional: Präfix, Aktivität etc. anpassen

#### 6. Bot kompilieren
```bash
# Projekt bauen
mvn clean package
```

#### 7. Bot starten
```bash
# Direkter Start
java -jar target/discord-adelheit-0.3.0-alpha-jar-with-dependencies.jar
```

### Produktivbetrieb mit Systemd

#### Systemd-Service erstellen
```bash
sudo nano /etc/systemd/system/adelheit-bot.service
```

Inhalt der Datei:
```
[Unit]
Description=Adelheit Discord Bot
After=network.target

[Service]
Type=simple
User=DEIN_BENUTZERNAME
WorkingDirectory=/home/DEIN_BENUTZERNAME/Projects/discord-adelheit
ExecStart=/usr/bin/java -jar /home/DEIN_BENUTZERNAME/Projects/discord-adelheit/target/discord-adelheit-0.3.0-alpha-jar-with-dependencies.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

#### Service aktivieren
```bash
sudo systemctl daemon-reload
sudo systemctl enable adelheit-bot
sudo systemctl start adelheit-bot

# Status überprüfen
sudo systemctl status adelheit-bot
```

## 🎮 Verfügbare Befehle

### Musik-Befehle
- `/play [song/playlist]`: Musik abspielen
    - Unterstützt SoundCloud URLs und lokale Musik
    - Mit Playlist-Autocompletion für lokale Musik
- `/skip`: Aktuellen Track überspringen
- `/stop`: Wiedergabe stoppen und Warteschlange leeren
- `/queue`: Aktuelle Warteschlange anzeigen
- `/nowplaying`: Aktuellen Track mit Fortschrittsanzeige anzeigen
- `/volume [level]`: Lautstärke anpassen (0-100)
- `/pause`: Wiedergabe pausieren
- `/resume`: Wiedergabe fortsetzen
- `/play preview:[playlist]`: Vorschau der Tracks in einer Playlist anzeigen

### Warteraum-Befehle (Neu in 0.3.0!)
- `/warteraum aktivieren`: Aktiviert den Warteraummusik-Modus
- `/warteraum deaktivieren`: Deaktiviert den Warteraummusik-Modus

## 📂 Lokale Playlists einrichten

Um lokale Playlists zu verwenden:

1. Erstelle einen Ordner im `music_library/` Verzeichnis für jede Playlist
2. Platziere unterstützte Audiodateien (mp3, wav, flac) in diesen Ordnern
3. Bearbeite die `music_library.json` Datei, um neue Playlists zu registrieren:
   ```json
   {
     "playlists": [
       {
         "name": "playlist-name",
         "path": "music_library/playlist-ordner",
         "description": "Beschreibung der Playlist"
       }
     ]
   }
   ```

## 🚧 Troubleshooting
- Überprüfen Sie, ob der Bot-Token korrekt ist
- Prüfen Sie Logdateien unter `logs/bot.log`
- Stellen Sie sicher, dass alle Abhängigkeiten installiert sind
- Bei Verbindungsproblemen prüfen Sie die Firewall-Einstellungen

## 🌿 Entwicklungsstrategie

### Roadmap
[Entwicklungs-Roadmap](.github/ROADMAP.md)

### Branch-Struktur
- `main`: Stabile Produktionsversion
- `develop`: Integrations-Branch für Entwicklung
- `feature/*`: Branches für neue Funktionen
- `release/*`: Branches für Releasevorbereitungen

### Workflow
1. Neue Features werden in `feature/` Branches entwickelt
2. Features werden in den `develop` Branch gemerged
3. Vor einem Release wird ein `release/` Branch erstellt
4. Stabile Releases werden in `main` gemerged und getaggt

### Beitragen
- Forke das Projekt
- Erstelle einen Feature-Branch aus `develop`
- Committe Änderungen mit aussagekräftigen Nachrichten
- Erstelle einen Pull Request zum `develop` Branch

## ⚖️ Lizenz
Dieses Projekt ist unter der MIT-Lizenz veröffentlicht.

## 🐛 Fehler melden
Bitte melde Fehler über die GitHub Issues-Sektion.

## 📞 Kontakt
Projektlink: https://github.com/Quartel/discord-adelheit