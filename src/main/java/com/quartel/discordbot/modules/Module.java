package com.quartel.discordbot.modules;

import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstrakte Basisklasse für alle Bot-Module.
 * Module bieten spezifische Funktionalitäten und können aktiviert/deaktiviert werden.
 */
public abstract class Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(Module.class);

    private final String name;
    private final String description;
    private boolean enabled = false;

    /**
     * Erstellt ein neues Modul mit dem angegebenen Namen und der Beschreibung.
     *
     * @param name        Der Name des Moduls
     * @param description Die Beschreibung des Moduls
     */
    protected Module(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Gibt den Namen des Moduls zurück.
     *
     * @return Der Name des Moduls
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt die Beschreibung des Moduls zurück.
     *
     * @return Die Beschreibung des Moduls
     */
    public String getDescription() {
        return description;
    }

    /**
     * Prüft, ob das Modul aktiviert ist.
     *
     * @return true, wenn das Modul aktiviert ist, sonst false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Aktiviert das Modul, wenn es nicht bereits aktiviert ist.
     *
     * @param jda Die JDA-Instanz
     */
    public void enable(JDA jda) {
        if (!enabled) {
            try {
                onEnable(jda);
                enabled = true;
                LOGGER.info("Modul {} aktiviert", name);
            } catch (Exception e) {
                LOGGER.error("Fehler beim Aktivieren des Moduls {}: {}", name, e.getMessage(), e);
            }
        } else {
            LOGGER.warn("Modul {} ist bereits aktiviert", name);
        }
    }

    /**
     * Deaktiviert das Modul, wenn es aktiviert ist.
     *
     * @param jda Die JDA-Instanz
     */
    public void disable(JDA jda) {
        if (enabled) {
            try {
                onDisable(jda);
                enabled = false;
                LOGGER.info("Modul {} deaktiviert", name);
            } catch (Exception e) {
                LOGGER.error("Fehler beim Deaktivieren des Moduls {}: {}", name, e.getMessage(), e);
            }
        } else {
            LOGGER.warn("Modul {} ist bereits deaktiviert", name);
        }
    }

    /**
     * Lädt das Modul.
     */
    public void load() {
        try {
            onLoad();
            LOGGER.info("Modul {} geladen", name);
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden des Moduls {}: {}", name, e.getMessage(), e);
        }
    }

    /**
     * Wird aufgerufen, wenn das Modul geladen wird.
     * Diese Methode sollte von Unterklassen überschrieben werden, um Initialisierungslogik zu implementieren.
     */
    public void onLoad() {
        // Standardimplementierung ist leer
    }

    /**
     * Wird aufgerufen, wenn das Modul aktiviert wird.
     * Diese Methode muss von Unterklassen überschrieben werden.
     *
     * @param jda Die JDA-Instanz
     */
    public abstract void onEnable(JDA jda);

    /**
     * Wird aufgerufen, wenn das Modul deaktiviert wird.
     * Diese Methode muss von Unterklassen überschrieben werden.
     *
     * @param jda Die JDA-Instanz
     */
    public abstract void onDisable(JDA jda);
}