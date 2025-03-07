@echo off
setlocal enabledelayedexpansion

:: Adelheit Discord Bot Startskript für Windows
echo.
echo ===================================
echo Adelheit Discord Bot - Startskript
echo ===================================
echo.

:: Prüfe, ob Java installiert ist
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java wurde nicht gefunden!
    echo Bitte installiere Java 21 oder höher und versuche es erneut.
    echo Java kannst du hier herunterladen: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

:: Prüfe, ob die config-Datei existiert
if not exist "config\config.properties" (
    echo [INFO] Keine Konfigurationsdatei gefunden.
    echo.

    :: Stelle sicher, dass das config-Verzeichnis existiert
    if not exist "config" mkdir config

    :: Prüfe, ob die Beispielkonfiguration existiert
    if exist "config\config.properties.example" (
        :: Kopiere die Beispieldatei
        copy "config\config.properties.example" "config\config.properties" > nul

        :: Frage nach dem Bot-Token
        echo Bitte gib deinen Discord Bot-Token ein:
        echo (Du findest ihn im Discord Developer Portal: https://discord.com/developers/applications)
        set /p "BOT_TOKEN=> "

        :: Token trimmen (Leerzeichen am Anfang und Ende entfernen)
        for /f "tokens=* delims= " %%a in ("!BOT_TOKEN!") do set "BOT_TOKEN=%%a"
        :trim_loop
        if "!BOT_TOKEN:~-1!"==" " (
            set "BOT_TOKEN=!BOT_TOKEN:~0,-1!"
            goto trim_loop
        )

        :: Ersetze den Platzhalter-Token in der Konfigurationsdatei
        powershell -Command "(Get-Content config\config.properties) -replace 'bot.token=YOUR_TOKEN_HERE', 'bot.token=!BOT_TOKEN!' | Set-Content config\config.properties"

        echo.
        echo [INFO] Konfigurationsdatei aus Vorlage erstellt. Du kannst Einstellungen in config\config.properties anpassen.
        echo.
    ) else (
        echo [WARNING] Beispielkonfiguration nicht gefunden. Erstelle Standard-Konfiguration.

        :: Erstelle eine minimale Konfigurationsdatei
        echo # Bot Konfiguration > config\config.properties
        echo bot.token=!BOT_TOKEN! >> config\config.properties
        echo bot.prefix=! >> config\config.properties
        echo bot.activity=Musik >> config\config.properties
        echo. >> config\config.properties
        echo # Modul-Konfiguration >> config\config.properties
        echo modules.enabled=music >> config\config.properties
        echo. >> config\config.properties
        echo # Musik-Modul-Konfiguration >> config\config.properties
        echo music.volume.default=50 >> config\config.properties
        echo music.timeout=60 >> config\config.properties
        echo music.max_queue_size=100 >> config\config.properties
        echo music.auto_leave_timeout=300 >> config\config.properties
        echo music.allowed_formats=mp3,wav,flac >> config\config.properties
        echo music.max_volume=200 >> config\config.properties
        echo music.default_playlist=chill >> config\config.properties
        echo. >> config\config.properties
        echo # Berechtigungen für Musikbefehle >> config\config.properties
        echo # Mögliche Werte: EVERYONE, DJ_ROLE, ADMIN_ROLE, SERVER_OWNER >> config\config.properties
        echo music.permissions.play=DJ_ROLE >> config\config.properties
        echo music.permissions.skip=DJ_ROLE >> config\config.properties
        echo music.permissions.stop=DJ_ROLE >> config\config.properties
        echo music.permissions.queue=EVERYONE >> config\config.properties
        echo music.permissions.nowplaying=EVERYONE >> config\config.properties
        echo music.permissions.volume=DJ_ROLE >> config\config.properties
        echo music.permissions.pause=DJ_ROLE >> config\config.properties
        echo music.permissions.resume=DJ_ROLE >> config\config.properties
        echo. >> config\config.properties
        echo # Logging >> config\config.properties
        echo logging.level=INFO >> config\config.properties

        echo.
        echo [INFO] Standard-Konfigurationsdatei erstellt. Du kannst Einstellungen in config\config.properties anpassen.
        echo.
    )
)

:: Stelle sicher, dass logs-Verzeichnis existiert
if not exist "logs" mkdir logs

:: Starte den Bot mit Speicheroptimierung für Java
echo [INFO] Starte Adelheit Discord Bot...
echo.
echo Der Bot wird jetzt gestartet. Um ihn zu beenden, schließe dieses Fenster oder drücke Strg+C.
echo.
echo Die Log-Datei findest du unter logs\bot.log
echo.

java -Xms128m -Xmx256m -jar discord-adelheit-1.0-SNAPSHOT.jar

:: Wenn der Bot unerwartet beendet wurde
echo.
echo Der Bot wurde beendet. Drücke eine Taste, um das Fenster zu schließen.
pause > nul