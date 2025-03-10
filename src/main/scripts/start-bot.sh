#!/bin/bash

# Setze explizit UTF-8 Zeichenkodierung
export LANG=de_DE.UTF-8
export LC_ALL=de_DE.UTF-8

# Adelheit Discord Bot Startskript für Linux

echo
echo "==================================="
echo "Adelheit Discord Bot - Startskript"
echo "==================================="
echo

# Prüfe, ob Java installiert ist
echo "[INFO] Prüfe Java-Installation..."
if ! command -v java &> /dev/null; then
    echo "[FEHLER] Java wurde nicht gefunden!"
    echo "Bitte installiere Java 21 oder höher und versuche es erneut."
    echo "Unter Ubuntu/Debian: sudo apt install openjdk-21-jre"
    echo "Unter Fedora/RHEL: sudo dnf install java-21-openjdk"
    echo
    exit 1
fi

# Prüfe Java-Version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
JAVA_VERSION_NUM=$(echo $JAVA_VERSION | cut -d'.' -f1)

if [[ "$JAVA_VERSION_NUM" -lt 21 ]]; then
    echo "[FEHLER] Java-Version $JAVA_VERSION ist zu alt!"
    echo "Adelheit Discord Bot benötigt Java 21 oder höher."
    echo "Bitte aktualisiere deine Java-Installation."
    echo
    exit 1
fi
echo "[INFO] Java $JAVA_VERSION gefunden - Korrekte Version vorhanden."

# Stelle sicher, dass die nötigen Verzeichnisse existieren
mkdir -p config logs
mkdir -p music_library/chill music_library/energetic

# Prüfe Schreibberechtigungen
echo "[INFO] Prüfe Schreibberechtigungen..."
if ! touch config/permissions_test.tmp 2>/dev/null; then
    echo "[FEHLER] Keine Schreibberechtigung im config-Verzeichnis!"
    echo "Bitte starte das Skript mit Administratorrechten oder"
    echo "wähle ein Verzeichnis mit Schreibzugriff."
    echo
    exit 1
else
    rm config/permissions_test.tmp
fi

# Prüfe, ob die config.properties Datei existiert
if [ ! -f "config/config.properties" ]; then
    echo "[INFO] Erstelle neue Konfigurationsdatei..."

    # Kopiere die Beispielkonfiguration oder erstelle eine neue
    if [ -f "config/config.properties.example" ]; then
        cp config/config.properties.example config/config.properties
        echo "[INFO] Konfigurationsdatei aus Vorlage erstellt."
    else
        # Hier würde der Code zur Erstellung der config.properties-Datei folgen
        # wie in unserem vorherigen Skript
        :
    fi

    echo
    echo "[WICHTIG] Sicherheitshinweise zum Bot-Token:"
    echo "- Behandle deinen Bot-Token wie ein Passwort"
    echo "- Teile deinen Token niemals mit anderen"
    echo "- Veröffentliche die config.properties nicht in öffentlichen Repositories"
    echo "- Wenn du vermutest, dass dein Token kompromittiert wurde,"
    echo "  regeneriere ihn im Discord Developer Portal"
    echo
    echo "[HINWEIS] Bitte trage dein Discord Bot-Token in die Datei ein:"
    echo "          config/config.properties"
    echo
    echo "          Ändere die Zeile: bot.token=YOUR_TOKEN_HERE"
    echo "          zu: bot.token=dein_eigentliches_token"
    echo
    echo "          Speichere die Datei und starte das Skript erneut."
    echo
    read -p "Drücke Enter zum Beenden..." dummy
    exit 0
fi

# Prüfe, ob music_library.json existiert
if [ ! -f "config/music_library.json" ]; then
    echo "[INFO] Erstelle music_library.json..."

    if [ -f "config/music_library.json.example" ]; then
        cp config/music_library.json.example config/music_library.json
        echo "[INFO] Musik-Bibliothek-Konfiguration aus Vorlage erstellt."
    else
        # Hier würde der Code zur Erstellung der music_library.json-Datei folgen
        # wie in unserem vorherigen Skript
        :
    fi
fi

# Prüfe, ob das Token in der Konfigurationsdatei noch der Standardwert ist
if grep -q "bot.token=YOUR_TOKEN_HERE" "config/config.properties"; then
    echo "[WARNUNG] Es wurde kein Discord Bot-Token eingetragen!"
    echo "          Bitte öffne die Datei config/config.properties"
    echo "          und trage dein Token ein."
    echo
    echo "[WICHTIG] Sicherheitshinweise zum Bot-Token:"
    echo "- Behandle deinen Bot-Token wie ein Passwort"
    echo "- Teile deinen Token niemals mit anderen"
    echo "- Veröffentliche die config.properties nicht in öffentlichen Repositories"
    echo "- Wenn du vermutest, dass dein Token kompromittiert wurde,"
    echo "  regeneriere ihn im Discord Developer Portal"
    echo
    read -p "Drücke Enter zum Beenden..." dummy
    exit 1
fi

# Stelle sicher, dass das Skript ausführbar ist
chmod +x start-bot.sh

# Starte den Bot
echo "[INFO] Starte Adelheit Discord Bot..."
echo
echo "Der Bot wird jetzt gestartet. Um ihn zu beenden, drücke Strg+C."
echo
echo "Die Log-Datei findest du unter logs/bot.log"
echo

java -Xms128m -Xmx256m -Dfile.encoding=UTF-8 -jar discord-adelheit-${project.version}.jar

# Wenn der Bot unerwartet beendet wurde
echo
echo "Der Bot wurde beendet."