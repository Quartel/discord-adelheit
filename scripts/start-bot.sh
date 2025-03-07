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

    # Frage nach dem Bot-Token
    echo "Bitte gib deinen Discord Bot-Token ein:"
    echo "(Du findest ihn im Discord Developer Portal: https://discord.com/developers/applications)"
    read -p "> " BOT_TOKEN

    # Token trimmen (Leerzeichen am Anfang und Ende entfernen)
    BOT_TOKEN=$(echo "$BOT_TOKEN" | xargs)

    # Erstelle Konfigurationsdatei mit dem Token
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
EOF

    echo
    echo "[INFO] Konfigurationsdatei erstellt. Du kannst weitere Einstellungen in config/config.properties ändern."
    echo
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

java -Xms128m -Xmx256m -jar discord-adelheit-1.0-SNAPSHOT.jar

# Wenn der Bot unerwartet beendet wurde
echo
echo "Der Bot wurde beendet."