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
 * Sie lädt die Einstellungen aus der config.properties-Datei, die an verschiedenen Orten gesucht wird.
 */
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private static final String CONFIG_FILE = "config.properties";
    private static final String CONFIG_EXAMPLE_FILE = "config.properties.example";
    private static final Properties properties = new Properties();
    private static boolean isLoaded = false;

    /**
     * Lädt die Konfigurationsdatei beim ersten Zugriff.
     * Sucht die Datei in dieser Reihenfolge:
     * 1. Im aktuellen Arbeitsverzeichnis
     * 2. Im Unterverzeichnis "config"
     * 3. Im .adelheit Verzeichnis im Benutzerverzeichnis
     * 4. In den Ressourcen (eingebettet in die JAR)
     */
    private static void loadConfig() {
        if (isLoaded) {
            return;
        }

        // Versuch 1: Im aktuellen Arbeitsverzeichnis
        Path configPath = Paths.get(CONFIG_FILE);
        boolean configFound = tryLoadConfig(configPath);

        // Versuch 2: Im config Unterverzeichnis
        if (!configFound) {
            configPath = Paths.get("config", CONFIG_FILE);
            configFound = tryLoadConfig(configPath);
        }

        // Versuch 3: Im Benutzerverzeichnis unter .adelheit
        if (!configFound) {
            configPath = Paths.get(System.getProperty("user.home"), ".adelheit", CONFIG_FILE);
            configFound = tryLoadConfig(configPath);
        }

        // Versuch 4: In resources (eingebettet in die JAR)
        if (!configFound) {
            try (InputStream resourceStream = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (resourceStream != null) {
                    properties.load(resourceStream);
                    isLoaded = true;
                    LOGGER.info("Konfiguration aus eingebetteter Ressource geladen");

                    // Wir kopieren die eingebettete Konfiguration ins Arbeitsverzeichnis für zukünftige Bearbeitungen
                    extractConfigExample();

                    return;
                }
            } catch (IOException e) {
                LOGGER.error("Fehler beim Laden der eingebetteten Konfiguration", e);
            }
        }

        // Wenn immer noch keine Konfiguration geladen wurde, extrahieren wir die Beispielkonfiguration
        if (!isLoaded) {
            LOGGER.warn("Keine Konfigurationsdatei gefunden. Erstelle Beispielkonfiguration...");

            if (extractConfigExample()) {
                // Versuche nochmals zu laden
                configPath = Paths.get(CONFIG_FILE);
                configFound = tryLoadConfig(configPath);

                if (configFound) {
                    LOGGER.info("Beispielkonfiguration wurde erstellt und geladen");
                    LOGGER.warn("Bitte bearbeite die Datei {} und starte den Bot neu", configPath.toAbsolutePath());

                    // Token setzen, um sicherzustellen, dass Benutzer weiß, dass er es ändern muss
                    properties.setProperty("bot.token", "BITTE_HIER_DEIN_BOT_TOKEN_EINFÜGEN");
                } else {
                    LOGGER.error("Konnte die Beispielkonfiguration nicht laden");
                }
            }
        }
    }

    /**
     * Versucht, die Konfiguration aus dem angegebenen Pfad zu laden.
     *
     * @param configPath Pfad zur Konfigurationsdatei
     * @return true, wenn die Konfiguration erfolgreich geladen wurde
     */
    private static boolean tryLoadConfig(Path configPath) {
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
                isLoaded = true;
                LOGGER.info("Konfiguration erfolgreich geladen von {}", configPath.toAbsolutePath());
                return true;
            } catch (IOException e) {
                LOGGER.error("Fehler beim Laden der Konfigurationsdatei von {}", configPath, e);
            }
        }
        return false;
    }

    /**
     * Extrahiert die Beispielkonfiguration aus den Ressourcen ins aktuelle Verzeichnis.
     *
     * @return true, wenn die Extraktion erfolgreich war
     */
    private static boolean extractConfigExample() {
        try (InputStream exampleStream = Config.class.getClassLoader().getResourceAsStream(CONFIG_EXAMPLE_FILE)) {
            if (exampleStream != null) {
                Path configDir = Paths.get("config");
                if (!Files.exists(configDir)) {
                    Files.createDirectories(configDir);
                }

                Path examplePath = configDir.resolve(CONFIG_FILE);
                Files.copy(exampleStream, examplePath, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Beispielkonfiguration nach {} extrahiert", examplePath.toAbsolutePath());
                return true;
            } else {
                LOGGER.error("Beispielkonfigurationsdatei nicht gefunden in den Ressourcen");
            }
        } catch (IOException e) {
            LOGGER.error("Fehler beim Extrahieren der Beispielkonfiguration", e);
        }
        return false;
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
     * Aktualisiert eine Eigenschaft und speichert sie in der Konfigurationsdatei.
     *
     * @param key Der Schlüssel der Eigenschaft
     * @param value Der neue Wert der Eigenschaft
     * @return true, wenn die Aktualisierung erfolgreich war
     */
    public static boolean updateProperty(String key, String value) {
        loadConfig();
        properties.setProperty(key, value);

        // Bestimme den Pfad zum Speichern
        Path configPath = Paths.get("config", CONFIG_FILE);
        if (!Files.exists(configPath.getParent())) {
            try {
                Files.createDirectories(configPath.getParent());
            } catch (IOException e) {
                LOGGER.error("Fehler beim Erstellen des Konfigurationsverzeichnisses", e);
                return false;
            }
        }

        try {
            properties.store(Files.newOutputStream(configPath), "Konfiguration für Adelheit Discord Bot");
            LOGGER.info("Konfiguration aktualisiert und in {} gespeichert", configPath);
            return true;
        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern der aktualisierten Konfiguration", e);
            return false;
        }
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
}