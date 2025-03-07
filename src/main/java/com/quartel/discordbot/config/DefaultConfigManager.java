package com.quartel.discordbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Verwaltet die Standardwerte für die Konfiguration.
 * Diese Klasse wird verwendet, um neue Konfigurationseinstellungen zu registrieren
 * und sicherzustellen, dass sie in allen Konfigurationsdateien vorhanden sind.
 */
public class DefaultConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfigManager.class);

    // Map für alle bekannten Konfigurationsschlüssel und ihre Standardwerte
    private static final Map<String, String> DEFAULT_VALUES = new HashMap<>();

    // Initialisierung der Standardwerte
    static {
        // Bot-Grundeinstellungen
        registerDefault("bot.token", "");
        registerDefault("bot.prefix", "!");
        registerDefault("bot.activity", "Musik");

        // Modul-Konfiguration
        registerDefault("modules.enabled", "music");

        // Musik-Modul
        registerDefault("music.volume.default", "50");
        registerDefault("music.timeout", "60");
        registerDefault("music.max_queue_size", "100");
        registerDefault("music.auto_leave_timeout", "300");
        registerDefault("music.allowed_formats", "mp3,wav,flac");
        registerDefault("music.max_volume", "200");
        registerDefault("music.default_playlist", "chill");

        // Logging
        registerDefault("logging.level", "INFO");

        // Berechtigungen für Musikbefehle
        registerDefault("music.permissions.play", "EVERYONE");
        registerDefault("music.permissions.skip", "DJ_ROLE");
        registerDefault("music.permissions.stop", "DJ_ROLE");
        registerDefault("music.permissions.queue", "EVERYONE");
        registerDefault("music.permissions.nowplaying", "EVERYONE");
        registerDefault("music.permissions.volume", "DJ_ROLE");
        registerDefault("music.permissions.pause", "DJ_ROLE");
        registerDefault("music.permissions.resume", "DJ_ROLE");

        // Warteraum-Konfiguration
        registerDefault("warteraum.channel_id", "");
        registerDefault("warteraum.auto_leave_timeout", "60");
        registerDefault("warteraum.default_playlist", "chill");

        // Hier neue Konfigurationsoptionen hinzufügen:
        // BEISPIEL: registerDefault("feature.new.option", "default value");
    }

    /**
     * Registriert einen Standardwert für einen Konfigurationsschlüssel.
     *
     * @param key Der Konfigurationsschlüssel
     * @param defaultValue Der Standardwert
     */
    private static void registerDefault(String key, String defaultValue) {
        DEFAULT_VALUES.put(key, defaultValue);
    }

    /**
     * Gibt den Standardwert für einen Konfigurationsschlüssel zurück.
     *
     * @param key Der Konfigurationsschlüssel
     * @return Der Standardwert oder null, wenn kein Standardwert registriert ist
     */
    public static String getDefault(String key) {
        return DEFAULT_VALUES.get(key);
    }

    /**
     * Gibt alle bekannten Konfigurationsschlüssel zurück.
     *
     * @return Eine Map mit allen Schlüsseln und ihren Standardwerten
     */
    public static Map<String, String> getAllDefaults() {
        return new HashMap<>(DEFAULT_VALUES);
    }

    /**
     * Gibt an, ob ein Konfigurationsschlüssel bekannt ist.
     *
     * @param key Der zu prüfende Schlüssel
     * @return true, wenn der Schlüssel bekannt ist
     */
    public static boolean isKnownKey(String key) {
        return DEFAULT_VALUES.containsKey(key);
    }
}