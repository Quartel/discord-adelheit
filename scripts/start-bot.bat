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

    :: Erstelle Konfigurationsdatei mit dem Token
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

    echo.
    echo [INFO] Konfigurationsdatei erstellt. Du kannst weitere Einstellungen in config\config.properties ändern.
    echo.
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