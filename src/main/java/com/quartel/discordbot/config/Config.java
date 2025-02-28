package com.quartel.discordbot.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lädt und verwaltet die Konfigurationseinstellungen des Bots
 * aus der config.properties-Datei.
 */
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();
    private static boolean isLoaded = false;

    /**
     * Lädt die Konfiguration aus der properties-Datei.
     * Wird beim Start der Anwendung automatisch aufgerufen.
     */
    public static void load() {
        if (isLoaded) {
            return;
        }

        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                LOGGER.error("Konnte config.properties nicht finden. Bitte stelle sicher, dass die Datei im resources-Verzeichnis existiert.");
                throw new RuntimeException("Konfigurationsdatei nicht gefunden");
            }

            // Lade die Properties aus der Datei
            properties.load(input);
            isLoaded = true;
            LOGGER.info("Konfiguration erfolgreich geladen");
        } catch (IOException e) {
            LOGGER.error("Fehler beim Laden der Konfiguration", e);
            throw new RuntimeException("Fehler beim Laden der Konfiguration", e);
        }
    }

    /**
     * Gibt einen String-Wert aus der Konfiguration zurück.
     *
     * @param key Der Schlüssel in der Konfigurationsdatei
     * @return Der Wert als String
     */
    public static String getString(String key) {
        if (!isLoaded) {
            load();
        }
        return properties.getProperty(key);
    }

    /**
     * Gibt einen String-Wert aus der Konfiguration zurück.
     * Falls der Schlüssel nicht existiert, wird der Standardwert zurückgegeben.
     *
     * @param key Der Schlüssel in der Konfigurationsdatei
     * @param defaultValue Der Standardwert, falls der Schlüssel nicht existiert
     * @return Der Wert als String
     */
    public static String getString(String key, String defaultValue) {
        if (!isLoaded) {
            load();
        }
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gibt einen Integer-Wert aus der Konfiguration zurück.
     *
     * @param key Der Schlüssel in der Konfigurationsdatei
     * @return Der Wert als Integer
     * @throws NumberFormatException Wenn der Wert kein gültiger Integer ist
     */
    public static int getInt(String key) {
        if (!isLoaded) {
            load();
        }
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Konfigurationsschlüssel nicht gefunden: " + key);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.error("Ungültiger Integer-Wert für Schlüssel {}: {}", key, value);
            throw e;
        }
    }

    /**
     * Gibt einen Integer-Wert aus der Konfiguration zurück.
     * Falls der Schlüssel nicht existiert, wird der Standardwert zurückgegeben.
     *
     * @param key Der Schlüssel in der Konfigurationsdatei
     * @param defaultValue Der Standardwert, falls der Schlüssel nicht existiert
     * @return Der Wert als Integer
     */
    public static int getInt(String key, int defaultValue) {
        if (!isLoaded) {
            load();
        }
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Ungültiger Integer-Wert für Schlüssel {}: {}. Verwende Standardwert: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Gibt einen Boolean-Wert aus der Konfiguration zurück.
     *
     * @param key Der Schlüssel in der Konfigurationsdatei
     * @return Der Wert als Boolean
     */
    public static boolean getBoolean(String key) {
        if (!isLoaded) {
            load();
        }
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Konfigurationsschlüssel nicht gefunden: " + key);
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Gibt einen Boolean-Wert aus der Konfiguration zurück.
     * Falls der Schlüssel nicht existiert, wird der Standardwert zurückgegeben.
     *
     * @param key Der Schlüssel in der Konfigurationsdatei
     * @param defaultValue Der Standardwert, falls der Schlüssel nicht existiert
     * @return Der Wert als Boolean
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        if (!isLoaded) {
            load();
        }
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}