# Adelheit - Discord Musikbot

## 📖 Projektbeschreibung

Adelheit ist ein leistungsfähiger Discord-Musikbot, der entwickelt wurde, um nahtlose Musikwiedergabe und Verwaltung in Discord-Servern zu ermöglichen.

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

## 🚀 Einrichtung

### Voraussetzungen
- Java Development Kit (JDK) 21
- Maven
- Discord Bot Token

### Konfiguration
1. Kopiere `config.properties.example` zu `config.properties`
2. Ersetze `YOUR_TOKEN_HERE` mit deinem Discord Bot Token
3. Konfiguriere Bot-Einstellungen in `config.properties`

### Installation
```bash
# Repository klonen
git clone https://github.com/Quartel/discord-adelheit
cd adelheit

# Abhängigkeiten installieren
mvn clean install

# Bot starten
mvn exec:java
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

## 📁 Projektstruktur
```
src/
├── main/
│   ├── java/com/quartel/discordbot/
│   │   ├── core/           # Kern-Bot-Funktionalität
│   │   ├── config/         # Konfigurationsmanagement
│   │   └── modules/        # Modulsystem (z.B. Musikmodul)
│   └── resources/
│       ├── config.properties    # Bot-Konfiguration
│       └── music_library.json   # Musik-Bibliothekskonfiguration
└── test/                   # Unittest-Verzeichnis
```

## 🤝 Beitragen
1. Forke das Projekt
2. Erstelle einen Feature-Branch (`git checkout -b feature/AmazingFeature`)
3. Committe deine Änderungen (`git commit -m 'Add some AmazingFeature'`)
4. Pushe zum Branch (`git push origin feature/AmazingFeature`)
5. Öffne einen Pull Request

## ⚖️ Lizenz
Dieses Projekt ist unter der MIT-Lizenz veröffentlicht.

## 🐛 Fehler melden
Bitte melde Fehler über die GitHub Issues-Sektion.

## 📞 Kontakt
Projektlink: https://github.com/Quartel/discord-adelheit
