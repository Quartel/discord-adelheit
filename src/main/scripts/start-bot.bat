@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: Adelheit Discord Bot Startskript für Windows
echo.
echo ===================================
echo Adelheit Discord Bot - Startskript
echo ===================================
echo.

:: Prüfe, ob Java installiert ist und die Version korrekt ist
echo [INFO] Prüfe Java-Installation...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [FEHLER] Java wurde nicht gefunden!
    echo Bitte installiere Java 21 oder höher und versuche es erneut.
    echo Java kannst du hier herunterladen: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

:: Prüfe Java-Version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
set JAVA_VERSION=%JAVA_VERSION:"=%
set JAVA_VERSION=%JAVA_VERSION:~0,2%

:: Versuche, die Java-Version als Zahl zu interpretieren
set /a JAVA_VERSION_NUM=0
for /f "delims=. tokens=1" %%a in ("%JAVA_VERSION%") do set /a JAVA_VERSION_NUM=%%a

if %JAVA_VERSION_NUM% LSS 21 (
    echo [FEHLER] Java-Version %JAVA_VERSION% ist zu alt!
    echo Adelheit Discord Bot benötigt Java 21 oder höher.
    echo Java kannst du hier herunterladen: https://adoptium.net/
    echo.
    pause
    exit /b 1
)
echo [INFO] Java %JAVA_VERSION% gefunden - Korrekte Version vorhanden.

:: Stelle sicher, dass die nötigen Verzeichnisse existieren
if not exist "config" mkdir config
if not exist "logs" mkdir logs
if not exist "music_library" mkdir music_library
if not exist "music_library\chill" mkdir music_library\chill
if not exist "music_library\energetic" mkdir music_library\energetic

:: Prüfe Schreibberechtigungen
echo [INFO] Prüfe Schreibberechtigungen...
echo test > config\permissions_test.tmp 2>nul
if not exist config\permissions_test.tmp (
    echo [FEHLER] Keine Schreibberechtigung im config-Verzeichnis!
    echo Bitte starte das Skript mit Administratorrechten oder
    echo wähle ein Verzeichnis mit Schreibzugriff.
    echo.
    pause
    exit /b 1
) else (
    del config\permissions_test.tmp
)

:: Prüfe, ob die config.properties Datei existiert
if not exist "config\config.properties" (
    echo [INFO] Erstelle neue Konfigurationsdatei...

    :: Kopiere die Beispielkonfiguration oder erstelle eine neue
    if exist "config\config.properties.example" (
        copy "config\config.properties.example" "config\config.properties" > nul
        echo [INFO] Konfigurationsdatei aus Vorlage erstellt.
    ) else (
        :: Hier folgt der Code zur Erstellung der config.properties-Datei
        echo # Bot Konfiguration > config\config.properties
        echo bot.token=YOUR_TOKEN_HERE >> config\config.properties
        echo bot.prefix=! >> config\config.properties
        echo bot.activity=Musik >> config\config.properties
        echo. >> config\config.properties
        echo # Modul-Konfiguration >> config\config.properties
        echo modules.enabled=music >> config\config.properties

        echo [INFO] Neue Konfigurationsdatei erstellt.
    )

    echo.
    echo [WICHTIG] Sicherheitshinweise zum Bot-Token:
    echo - Behandle deinen Bot-Token wie ein Passwort
    echo - Teile deinen Token niemals mit anderen
    echo - Veröffentliche die config.properties nicht in öffentlichen Repositories
    echo - Wenn du vermutest, dass dein Token kompromittiert wurde,
    echo   regeneriere ihn im Discord Developer Portal
    echo.
    echo [HINWEIS] Bitte trage dein Discord Bot-Token in die Datei ein:
    echo           config\config.properties
    echo.
    echo           Ändere die Zeile: bot.token=YOUR_TOKEN_HERE
    echo           zu: bot.token=dein_eigentliches_token
    echo.
    echo           Speichere die Datei und starte das Skript erneut.
    echo.
    pause
    exit /b 0
)

:: Prüfe, ob music_library.json existiert
if not exist "config\music_library.json" (
    echo [INFO] Erstelle music_library.json...

    if exist "config\music_library.json.example" (
        copy "config\music_library.json.example" "config\music_library.json" > nul
        echo [INFO] Musik-Bibliothek-Konfiguration aus Vorlage erstellt.
    ) else (
        :: Hier folgt der Code zur Erstellung der music_library.json-Datei
        echo { > config\music_library.json
        echo   "playlists": [ >> config\music_library.json
        echo     { >> config\music_library.json
        echo       "name": "chill", >> config\music_library.json
        echo       "path": "music_library/chill", >> config\music_library.json
        echo       "description": "Relaxing music collection", >> config\music_library.json
        echo       "supportedFormats": ["mp3", "wav", "flac"] >> config\music_library.json
        echo     }, >> config\music_library.json
        echo     { >> config\music_library.json
        echo       "name": "energetic", >> config\music_library.json
        echo       "path": "music_library/energetic", >> config\music_library.json
        echo       "description": "High-energy tracks", >> config\music_library.json
        echo       "supportedFormats": ["mp3", "wav", "flac"] >> config\music_library.json
        echo     } >> config\music_library.json
        echo   ] >> config\music_library.json
        echo } >> config\music_library.json
    )
)

:: Prüfe, ob das Token in der Konfigurationsdatei noch der Standardwert ist
findstr /C:"bot.token=YOUR_TOKEN_HERE" "config\config.properties" >nul
if %ERRORLEVEL% EQU 0 (
    echo [WARNUNG] Es wurde kein Discord Bot-Token eingetragen!
    echo           Bitte öffne die Datei config\config.properties
    echo           und trage dein Token ein.
    echo.
    echo [WICHTIG] Sicherheitshinweise zum Bot-Token:
    echo - Behandle deinen Bot-Token wie ein Passwort
    echo - Teile deinen Token niemals mit anderen
    echo - Veröffentliche die config.properties nicht in öffentlichen Repositories
    echo - Wenn du vermutest, dass dein Token kompromittiert wurde,
    echo   regeneriere ihn im Discord Developer Portal
    echo.
    pause
    exit /b 1
)

:: Starte den Bot
echo [INFO] Starte Adelheit Discord Bot...
echo.
echo Der Bot wird jetzt gestartet. Um ihn zu beenden, schließe dieses Fenster oder drücke Strg+C.
echo.
echo Die Log-Datei findest du unter logs\bot.log
echo.

java -Xms128m -Xmx256m -Dfile.encoding=UTF-8 -jar discord-adelheit-${project.version}.jar

:: Wenn der Bot unerwartet beendet wurde
echo.
echo Der Bot wurde beendet. Drücke eine Taste, um das Fenster zu schließen.
pause >nul