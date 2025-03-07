#!/bin/bash

# Adelheit Discord Bot Startskript für Linux

echo
echo "==================================="
echo "Adelheit Discord Bot - Startskript"
echo "==================================="
echo

# Prüfe, ob Java installiert ist
if ! command -v java &> /dev/null; then
    echo "[ERROR] Java wurde nicht gefunden!"
    echo "Bitte installiere Java 21 oder höher und versuche es erneut."
    echo "Unter Ubuntu/Debian: sudo apt install openjdk-21-jre"
    echo "Unter Fedora/RHEL: sudo dnf install java-21-openjdk"
    echo
    exit 1
fi

# Prüfe, ob die config-Datei existiert
if [ ! -f "config/config.properties" ]; then
    echo "[INFO] Keine Konfigurationsdatei gefunden."
    echo

    # Stelle sicher, dass das config-Verzeichnis existiert
    mkdir -p config
    
    # Prüfe, ob die Beispielkonfiguration existiert
    if [ -f "config/config.properties.example" ]; then
        # Kopiere die Beispieldatei
        cp config/config.properties.example config/config.properties
        
        # Frage nach dem Bot-Token
        echo "Bitte gib deinen Discord Bot-Token ein:"
        echo "(Du findest ihn im Discord Developer Portal: https://discord.com/developers/applications)"
        read -p "> " BOT_TOKEN
        
        # Token trimmen (Leerzeichen am Anfang und Ende entfernen)
        BOT_TOKEN=$(echo "$BOT_TOKEN" | xargs)
        
        # Ersetze den Platzhalter-Token in der Konfigurationsdatei
        sed -i "s/bot.token=YOUR_TOKEN_HERE/bot.token=$BOT_TOKEN/g" config/config.properties
        
        echo
        echo "[INFO] Konfigurationsdatei aus Vorlage erstellt. Du kannst Einstellungen in config/config.properties anpassen."
        echo
    else
        echo "[WARNING] Beispielkonfiguration nicht gefunden. Erstelle Standard-Konfiguration."
        
        # Erstelle eine vollständige Konfigurationsdatei
        cat > config/config.properties << EOF
# Bot Konfiguration
bot.token=$BOT_TOKEN
bot.prefix=!
bot.activity=Musik

# Modul-Konfiguration
modules.enabled=music

# Musik-Modul-Konfiguration
music.volume.default=50
music.timeout=60
music.max_queue_size=100
music.auto_leave_timeout=300
music.allowed_formats=mp3,wav,flac
music.max_volume=200
music.default_playlist=chill

# Berechtigungen für Musikbefehle
# Mögliche Werte: EVERYONE, DJ_ROLE, ADMIN_ROLE, SERVER_OWNER
music.permissions.play=DJ_ROLE
music.permissions.skip=DJ_ROLE
music.permissions.stop=DJ_ROLE
music.permissions.queue=EVERYONE
music.permissions.nowplaying=EVERYONE
music.permissions.volume=DJ_ROLE
music.permissions.pause=DJ_ROLE
music.permissions.resume=DJ_ROLE

# Logging
logging.level=INFO
EOF
        
        echo
        echo "[INFO] Standard-Konfigurationsdatei erstellt. Du kannst Einstellungen in config/config.properties anpassen."
        echo
    fi
fi

# Stelle sicher, dass logs-Verzeichnis existiert
mkdir -p logs

# Mache das Skript ausführbar
chmod +x start-bot.sh

# Starte den Bot mit Speicheroptimierung für Java
echo "[INFO] Starte Adelheit Discord Bot..."
echo
echo "Der Bot wird jetzt gestartet. Um ihn zu beenden, drücke Strg+C."
echo
echo "Die Log-Datei findest du unter logs/bot.log"
echo

java -Xms128m -Xmx256m -jar discord-adelheit-0.3.0-alpha.jar

# Wenn der Bot unerwartet beendet wurde
echo
echo "Der Bot wurde beendet."