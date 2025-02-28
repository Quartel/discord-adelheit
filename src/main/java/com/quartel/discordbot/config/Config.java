package com.quartel.discordbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
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
    private static final String CONFIG_FILE = "src/main/resources/config.properties";
    private static final String CONFIG_EXAMPLE_FILE = "src/main/resources/config.properties.example";
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

        // Wenn die Konfigurationsdatei nicht existiert, erstelle sie aus dem Beispiel
        if (!Files.exists(configPath)) {
            try {
                Path examplePath = Paths.get(CONFIG_EXAMPLE_FILE);
                if (Files.exists(examplePath)) {
                    LOGGER.info("Konfigurationsdatei nicht gefunden. Erstelle aus Beispieldatei...");
                    Files.copy(examplePath, configPath, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Beispielkonfiguration nach {} kopiert. Bitte konfiguriere die Datei.", CONFIG_FILE);
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
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            isLoaded = true;
            LOGGER.info("Konfiguration erfolgreich geladen");
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
}