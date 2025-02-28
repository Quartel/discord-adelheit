package com.quartel.discordbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * Diese Klasse verwaltet die Konfiguration des Bots.
 * Sie lädt die Einstellungen aus der config.properties-Datei.
 */
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private static final String CONFIG_FILE = "config.properties";
    private static final String CONFIG_EXAMPLE_FILE = "config.properties.example";
    private static final Properties properties = new Properties();
    private static boolean isLoaded = false;

    /**
     * Lädt die Konfigurationsdatei beim ersten Zugriff.
     */
    private static void loadConfig() {
        if (isLoaded) {
            return;
        }

        Path configPath = Paths.get(CONFIG_FILE);

        // Versuche verschiedene Pfade
        if (!Files.exists(configPath)) {
            // Versuche im Ressourcenverzeichnis
            configPath = Paths.get("src/main/resources/" + CONFIG_FILE);
        }

        if (!Files.exists(configPath)) {
            // Versuche im Benutzerverzeichnis
            configPath = Paths.get(System.getProperty("user.home"), ".adelheit", CONFIG_FILE);
        }

        // Wenn Konfigurationsdatei nicht existiert, kopiere Beispieldatei
        if (!Files.exists(configPath)) {
            try {
                // Stelle sicher, dass das Verzeichnis existiert
                Files.createDirectories(configPath.getParent());

                // Kopiere Beispieldatei
                Path examplePath = Paths.get("src/main/resources/" + CONFIG_EXAMPLE_FILE);
                if (Files.exists(examplePath)) {
                    LOGGER.info("Konfigurationsdatei nicht gefunden. Erstelle aus Beispieldatei...");
                    Files.copy(examplePath, configPath, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Beispielkonfiguration nach {} kopiert. Bitte konfiguriere die Datei.", configPath);
                } else {
                    LOGGER.error("Weder Konfigurationsdatei noch Beispieldatei gefunden!");
                    return;
                }
            } catch (IOException e) {
                LOGGER.error("Fehler beim Erstellen der Konfigurationsdatei", e);
                return;
            }
        }

        // Lade die Eigenschaften aus der Datei
        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);
            isLoaded = true;
            LOGGER.info("Konfiguration erfolgreich geladen von {}", configPath);
        } catch (IOException e) {
            LOGGER.error("Fehler beim Laden der Konfigurationsdatei", e);
        }
    }

    /**
     * Gibt den Wert für den angegebenen Schlüssel zurück.
     *
     * @param key Der Schlüssel der Eigenschaft
     * @return Der Wert für den Schlüssel oder null, wenn der Schlüssel nicht existiert
     */
    public static String getProperty(String key) {
        loadConfig();
        return properties.getProperty(key);
    }

    /**
     * Gibt den Wert für den angegebenen Schlüssel zurück oder den Standardwert,
     * wenn der Schlüssel nicht existiert.
     *
     * @param key          Der Schlüssel der Eigenschaft
     * @param defaultValue Der Standardwert, der zurückgegeben werden soll, wenn der Schlüssel nicht existiert
     * @return Der Wert für den Schlüssel oder der Standardwert, wenn der Schlüssel nicht existiert
     */
    public static String getProperty(String key, String defaultValue) {
        loadConfig();
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gibt den Bot-Token zurück.
     *
     * @return Der Bot-Token aus der Konfiguration
     */
    public static String getToken() {
        return getProperty("bot.token");
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
        return Integer.parseInt(getProperty("bot.max_volume", "200"));
    }

    /**
     * Gibt das Logging-Level zurück.
     *
     * @return Das Logging-Level
     */
    public static String getLoggingLevel() {
        return getProperty("logging.level", "INFO");
    }
}