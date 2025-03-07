package com.quartel.discordbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Diese Klasse verwaltet die Konfiguration des Bots.
 * Sie lädt die Einstellungen aus der config.properties-Datei, die an verschiedenen Orten gesucht wird.
 */
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private static final String CONFIG_FILE = "config.properties";
    private static final String CONFIG_EXAMPLE_FILE = "config.properties.example";
    private static final Properties properties = new Properties();
    private static boolean isLoaded = false;
    private static Path loadedConfigPath = null;

    /**
     * Lädt die Konfigurationsdatei beim ersten Zugriff.
     * Versucht mehrere Orte, bis eine gültige Konfiguration gefunden wird.
     */
    private static synchronized void loadConfig() {
        if (isLoaded) {
            return;
        }

        LOGGER.debug("Starte Laden der Konfiguration...");
        List<Path> configPaths = new ArrayList<>();

        // Liste aller möglichen Konfigurationspfade
        configPaths.add(Paths.get(CONFIG_FILE));
        configPaths.add(Paths.get("config", CONFIG_FILE));
        configPaths.add(Paths.get(System.getProperty("user.home"), ".adelheit", CONFIG_FILE));

        boolean configFound = false;

        // Versuche alle Pfade, bis eine gültige Konfiguration gefunden wird
        for (Path configPath : configPaths) {
            if (Files.exists(configPath)) {
                LOGGER.debug("Konfigurationsdatei gefunden: {}", configPath);
                try (InputStream input = Files.newInputStream(configPath)) {
                    // Bestehende Properties löschen und neu laden
                    properties.clear();
                    properties.load(input);

                    // Prüfe, ob ein Token existiert
                    String token = properties.getProperty("bot.token");
                    if (token != null && !token.trim().isEmpty() && !token.equals("YOUR_TOKEN_HERE") &&
                            !token.equals("BITTE_HIER_DEIN_BOT_TOKEN_EINFÜGEN")) {
                        loadedConfigPath = configPath;
                        LOGGER.info("Konfiguration erfolgreich geladen von {}", configPath.toAbsolutePath());
                        configFound = true;
                        isLoaded = true;

                        // Aktualisiere die Konfiguration mit neuen Optionen
                        checkMissingProperties();

                        break;
                    } else {
                        LOGGER.warn("Konfiguration in {} enthält keinen gültigen Token", configPath);
                    }
                } catch (IOException e) {
                    LOGGER.error("Fehler beim Laden der Konfigurationsdatei von {}", configPath, e);
                }
            }
        }

        // Wenn keine gültige Konfiguration gefunden wurde, versuche aus Ressourcen zu laden
        if (!configFound) {
            LOGGER.debug("Keine gültige Konfiguration in Dateisystem gefunden, versuche eingebettete Ressource");
            try (InputStream resourceStream = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (resourceStream != null) {
                    properties.clear();
                    properties.load(resourceStream);

                    String token = properties.getProperty("bot.token");
                    if (token != null && !token.trim().isEmpty() && !token.equals("YOUR_TOKEN_HERE") &&
                            !token.equals("BITTE_HIER_DEIN_BOT_TOKEN_EINFÜGEN")) {
                        LOGGER.info("Konfiguration aus eingebetteter Ressource geladen");
                        loadedConfigPath = Paths.get("EMBEDDED_RESOURCE");
                        isLoaded = true;

                        // Speichere die Konfiguration für spätere Bearbeitungen
                        extractConfigToFile();
                    } else {
                        LOGGER.warn("Eingebettete Konfiguration enthält keinen gültigen Token");
                        extractConfigExample();
                    }
                } else {
                    LOGGER.warn("Keine eingebettete Konfiguration gefunden");
                    extractConfigExample();
                }
            } catch (IOException e) {
                LOGGER.error("Fehler beim Laden der eingebetteten Konfiguration", e);
                extractConfigExample();
            }
        }

        // Wenn immer noch keine Konfiguration geladen wurde
        if (!isLoaded) {
            LOGGER.warn("Keine gültige Konfiguration gefunden. Bot kann nicht starten.");
            LOGGER.info("Bitte bearbeite eine der folgenden Dateien und füge deinen Bot-Token hinzu:");
            for (Path path : configPaths) {
                LOGGER.info(" - {}", path.toAbsolutePath());
            }
        }
    }

    /**
     * ACHTUNG: Diese Methode nur manuell aufrufen, wenn alle fehlenden Eigenschaften
     * zur Konfigurationsdatei hinzugefügt werden sollen. Dies überschreibt das Dateiformat.
     *
     * Aktualisiert eine bestehende Konfigurationsdatei mit neuen Standardwerten.
     * Bestehende Werte werden nicht überschrieben.
     */
    public static void applyMissingProperties() {
        if (loadedConfigPath == null || loadedConfigPath.equals(Paths.get("EMBEDDED_RESOURCE"))) {
            return; // Keine Migration nötig, wenn keine Datei geladen wurde
        }

        LOGGER.debug("Prüfe auf fehlende Konfigurationsoptionen...");

        // Zähler für hinzugefügte Optionen
        int addedOptions = 0;

        // Prüfe auf fehlende Optionen basierend auf den registrierten Standardwerten
        for (Map.Entry<String, String> entry : DefaultConfigManager.getAllDefaults().entrySet()) {
            String key = entry.getKey();
            String defaultValue = entry.getValue();

            if (!properties.containsKey(key)) {
                // Option fehlt in der aktuellen Konfiguration, füge sie hinzu
                properties.setProperty(key, defaultValue);
                LOGGER.debug("Neue Konfigurationsoption hinzugefügt: {} = {}", key, defaultValue);
                addedOptions++;
            }
        }

        // Speichere die aktualisierte Konfiguration, wenn Änderungen vorgenommen wurden
        if (addedOptions > 0) {
            try (OutputStream output = Files.newOutputStream(loadedConfigPath)) {
                properties.store(output, "Konfiguration für Adelheit Discord Bot - Automatisch migriert");
                LOGGER.info("{} neue Konfigurationsoptionen hinzugefügt und in {} gespeichert",
                        addedOptions, loadedConfigPath);
            } catch (IOException e) {
                LOGGER.error("Fehler beim Speichern der migrierten Konfiguration", e);
            }
        } else {
            LOGGER.debug("Keine fehlenden Konfigurationsoptionen gefunden");
        }
    }

    /**
     * Überprüft, ob in der aktuellen Konfiguration Eigenschaften fehlen und loggt diese.
     * Fehlende Eigenschaften werden nur protokolliert, aber nicht automatisch hinzugefügt.
     */
    private static void checkMissingProperties() {
        if (loadedConfigPath == null || loadedConfigPath.equals(Paths.get("EMBEDDED_RESOURCE"))) {
            return; // Keine Überprüfung möglich, wenn keine Datei geladen wurde
        }

        LOGGER.debug("Prüfe auf fehlende Konfigurationsoptionen...");

        // Sammle alle fehlenden Eigenschaften
        List<String> missingProperties = new ArrayList<>();

        // Prüfe auf fehlende Optionen basierend auf den registrierten Standardwerten
        for (Map.Entry<String, String> entry : DefaultConfigManager.getAllDefaults().entrySet()) {
            String key = entry.getKey();
            String defaultValue = entry.getValue();

            if (!properties.containsKey(key)) {
                // Eigenschaft fehlt in der aktuellen Konfiguration
                missingProperties.add(key);
                LOGGER.debug("Fehlende Konfigurationsoption gefunden: {} (Standardwert: {})", key, defaultValue);
            }
        }

        // Wenn fehlende Eigenschaften gefunden wurden, logge eine Nachricht
        if (!missingProperties.isEmpty()) {
            LOGGER.info("Es wurden {} fehlende Konfigurationsoptionen gefunden:", missingProperties.size());
            for (String key : missingProperties) {
                String defaultValue = DefaultConfigManager.getDefault(key);
                LOGGER.info("  - {}: \"{}\" (wird als Standardwert verwendet)", key, defaultValue);
            }
            LOGGER.info("Füge diese Optionen manuell zu deiner Konfigurationsdatei hinzu, um diese Meldung zu vermeiden.");
        } else {
            LOGGER.debug("Keine fehlenden Konfigurationsoptionen gefunden");
        }
    }

    /**
     * Extrahiert die Beispielkonfiguration.
     */
    private static boolean extractConfigExample() {
        try (InputStream exampleStream = Config.class.getClassLoader().getResourceAsStream(CONFIG_EXAMPLE_FILE)) {
            if (exampleStream != null) {
                // Stelle sicher, dass das Verzeichnis existiert
                Path configDir = Paths.get("config");
                if (!Files.exists(configDir)) {
                    Files.createDirectories(configDir);
                }

                Path configPath = configDir.resolve(CONFIG_FILE);

                // Nur kopieren, wenn die Datei noch nicht existiert
                if (!Files.exists(configPath)) {
                    Files.copy(exampleStream, configPath, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Beispielkonfiguration nach {} extrahiert", configPath.toAbsolutePath());
                    LOGGER.info("Bitte füge deinen Bot-Token in diese Datei ein und starte den Bot neu");
                    return true;
                }
            } else {
                LOGGER.error("Beispielkonfigurationsdatei nicht gefunden in den Ressourcen");
            }
        } catch (IOException e) {
            LOGGER.error("Fehler beim Extrahieren der Beispielkonfiguration", e);
        }
        return false;
    }

    /**
     * Extrahiert die eingebettete Konfiguration in eine Datei.
     */
    private static boolean extractConfigToFile() {
        try {
            // Stelle sicher, dass das Verzeichnis existiert
            Path configDir = Paths.get("config");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path configPath = configDir.resolve(CONFIG_FILE);

            // Nur speichern, wenn die Datei noch nicht existiert
            if (!Files.exists(configPath)) {
                LOGGER.info("Speichere Konfiguration in {}", configPath.toAbsolutePath());
                try (OutputStream output = Files.newOutputStream(configPath)) {
                    properties.store(output, "Konfiguration für Adelheit Discord Bot");
                    return true;
                }
            }
        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern der Konfiguration", e);
        }
        return false;
    }

    /**
     * Gibt den Wert für den angegebenen Schlüssel zurück.
     * Wenn der Schlüssel nicht in der Konfiguration gefunden wird, wird der registrierte
     * Standardwert zurückgegeben.
     *
     * @param key Der Schlüssel der Eigenschaft
     * @return Der Wert für den Schlüssel, der registrierte Standardwert oder null
     */
    public static String getProperty(String key) {
        loadConfig();
        String value = properties.getProperty(key);

        if (value == null) {
            // Wenn der Wert nicht in der Konfiguration gefunden wurde,
            // verwende den registrierten Standardwert
            return DefaultConfigManager.getDefault(key);
        }

        return value;
    }

    /**
     * Gibt den Wert für den angegebenen Schlüssel zurück oder den angegebenen Standardwert,
     * wenn der Schlüssel nicht existiert oder keinen registrierten Standardwert hat.
     *
     * @param key          Der Schlüssel der Eigenschaft
     * @param defaultValue Der Standardwert, der zurückgegeben werden soll, wenn der Schlüssel nicht existiert
     * @return Der Wert für den Schlüssel oder der angegebene Standardwert
     */
    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return (value != null) ? value : defaultValue;
    }

    /**
     * Aktualisiert eine Eigenschaft und speichert sie in der Konfigurationsdatei.
     *
     * @param key Der Schlüssel der Eigenschaft
     * @param value Der neue Wert der Eigenschaft
     * @return true, wenn die Aktualisierung erfolgreich war
     */
    public static boolean updateProperty(String key, String value) {
        loadConfig();
        properties.setProperty(key, value);

        // Wenn keine Konfigurationsdatei geladen wurde, speichere in config/config.properties
        Path savePath = (loadedConfigPath != null && !loadedConfigPath.equals(Paths.get("EMBEDDED_RESOURCE")))
                ? loadedConfigPath
                : Paths.get("config", CONFIG_FILE);

        // Stelle sicher, dass das Verzeichnis existiert
        if (!Files.exists(savePath.getParent())) {
            try {
                Files.createDirectories(savePath.getParent());
            } catch (IOException e) {
                LOGGER.error("Fehler beim Erstellen des Konfigurationsverzeichnisses", e);
                return false;
            }
        }

        try {
            try (OutputStream output = Files.newOutputStream(savePath)) {
                properties.store(output, "Konfiguration für Adelheit Discord Bot");
            }
            LOGGER.info("Konfiguration aktualisiert und in {} gespeichert", savePath);
            return true;
        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern der aktualisierten Konfiguration", e);
            return false;
        }
    }

    /**
     * Lädt die Konfiguration neu.
     */
    public static void reloadConfig() {
        isLoaded = false;
        loadConfig();
        LOGGER.info("Konfiguration neu geladen");
    }

    /**
     * Gibt den Bot-Token zurück, nachdem Leerzeichen entfernt wurden.
     *
     * @return Der Bot-Token aus der Konfiguration, ohne führende oder nachfolgende Leerzeichen
     */
    public static String getToken() {
        String token = getProperty("bot.token");
        if (token != null) {
            // Leerzeichen am Anfang und Ende entfernen
            token = token.trim();

            // Prüfen, ob der Token nach dem Trimmen geändert wurde und ggf. aktualisieren
            String originalToken = getProperty("bot.token");
            if (!token.equals(originalToken)) {
                LOGGER.info("Token enthielt Leerzeichen, die entfernt wurden");
                updateProperty("bot.token", token);
            }
        }
        return token;
    }

    /**
     * Gibt das Bot-Präfix zurück.
     *
     * @return Das Bot-Präfix aus der Konfiguration oder "!" als Standardwert
     */
    public static String getPrefix() {
        return getProperty("bot.prefix", "!");
    }

    /**
     * Gibt den Bot-Aktivitätstext zurück.
     *
     * @return Der Aktivitätstext aus der Konfiguration oder "mit Discord" als Standardwert
     */
    public static String getActivity() {
        return getProperty("bot.activity", "mit Discord");
    }

    /**
     * Gibt die Liste der aktivierten Module zurück.
     *
     * @return Ein Array mit den Namen der aktivierten Module
     */
    public static String[] getEnabledModules() {
        String modules = getProperty("modules.enabled", "");
        if (modules.isEmpty()) {
            return new String[0];
        }
        return modules.split(",");
    }

    /**
     * Prüft, ob ein bestimmtes Modul aktiviert ist.
     *
     * @param moduleName Der Name des Moduls
     * @return true, wenn das Modul aktiviert ist, sonst false
     */
    public static boolean isModuleEnabled(String moduleName) {
        String[] modules = getEnabledModules();
        for (String module : modules) {
            if (module.trim().equalsIgnoreCase(moduleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt die maximale Warteschlangengröße zurück.
     *
     * @return Die maximale Warteschlangengröße
     */
    public static int getMaxQueueSize() {
        return Integer.parseInt(getProperty("music.max_queue_size", "100"));
    }

    /**
     * Gibt die Standard-Playlist zurück.
     *
     * @return Der Name der Standard-Playlist
     */
    public static String getDefaultPlaylist() {
        return getProperty("music.default_playlist", "chill");
    }

    /**
     * Gibt den Auto-Leave-Timeout zurück.
     *
     * @return Der Timeout in Sekunden
     */
    public static int getAutoLeaveTimeout() {
        return Integer.parseInt(getProperty("music.auto_leave_timeout", "300"));
    }

    /**
     * Gibt die erlaubten Musikformate zurück.
     *
     * @return Ein Array mit erlaubten Musikformaten
     */
    public static String[] getAllowedMusicFormats() {
        String formats = getProperty("music.allowed_formats", "mp3,wav,flac");
        return formats.split(",");
    }

    /**
     * Gibt die maximale Lautstärke zurück.
     *
     * @return Die maximale Lautstärke
     */
    public static int getMaxVolume() {
        return Integer.parseInt(getProperty("music.max_volume", "200"));
    }

    /**
     * Gibt das Logging-Level zurück.
     *
     * @return Das Logging-Level
     */
    public static String getLoggingLevel() {
        return getProperty("logging.level", "INFO");
    }

    /**
     * Prüft, ob die Konfiguration erfolgreich geladen wurde.
     *
     * @return true, wenn die Konfiguration geladen wurde
     */
    public static boolean isConfigLoaded() {
        loadConfig();
        return isLoaded;
    }
}