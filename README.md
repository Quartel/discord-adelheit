# Adelheit - Discord Musikbot

## 📖 Projektbeschreibung

Adelheit ist ein Discord-Musikbot, der entwickelt wurde, um nahtlose Musikwiedergabe und Verwaltung in Discord-Servern zu ermöglichen.

## 🚧 Pre-Release Hinweis
**Version: 0.1.0-alpha**

⚠️ **Achtung:** Dies ist eine frühe Alpha-Version. Der Bot kann Fehler enthalten und die Funktionalität ist möglicherweise eingeschränkt.

### Bekannte Einschränkungen
- Begrenzte Musikwiedergabe-Funktionen
- Möglicherweise instabile Verbindungen
- Eingeschränkte Fehlerbehandlung
- Nur grundlegende Slash-Commands implementiert

## 🆕 Was ist neu in 0.1.0-alpha
- Grundlegende Musikwiedergabe
- Lokale und Remote-Playlist-Unterstützung
- Modulares Bot-Design
- Erste Slash-Commands implementiert

## ✨ Funktionen

- 🎵 Musik von YouTube, SoundCloud und lokalen Bibliotheken abspielen
- ⏭️ Tracks überspringen
- ⏸️ Wiedergabe pausieren und fortsetzen
- 🔊 Lautstärke anpassen
- 📋 Warteschlange verwalten
- 🎼 Lokale Playlists abspielen und durchsuchen

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
- Linux-System (getestet auf Ubuntu)
- Internetverbindung
- Discord Bot Token

### Installationsschritte

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
java -jar target/discord-adelheit-0.2.0-alpha-jar-with-dependencies.jar
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
ExecStart=/usr/bin/java -jar /home/DEIN_BENUTZERNAME/Projects/discord-adelheit/target/discord-adelheit-1.0-SNAPSHOT-jar-with-dependencies.jar
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
- `/skip`: Aktuellen Track überspringen
- `/stop`: Wiedergabe stoppen
- `/queue`: Aktuelle Warteschlange anzeigen
- `/nowplaying`: Aktuellen Track anzeigen
- `/volume [level]`: Lautstärke anpassen
- `/pause`: Wiedergabe pausieren
- `/resume`: Wiedergabe fortsetzen

## 🚧 Troubleshooting
- Überprüfen Sie, ob der Bot-Token korrekt ist
- Prüfen Sie Logdateien unter `logs/`
- Stellen Sie sicher, dass alle Abhängigkeiten installiert sind

## 🌿 Entwicklungsstrategie

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