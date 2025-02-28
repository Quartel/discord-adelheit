package com.quartel.discordbot.modules;

import com.quartel.discordbot.core.CommandManager;
import net.dv8tion.jda.api.JDA;

/**
 * Interface für alle Module des Bots.
 * Ein Modul ist eine Sammlung von zusammengehörigen Funktionen und Befehlen.
 */
public interface Module {

    /**
     * Initialisiert das Modul und registriert seine Befehle beim CommandManager.
     *
     * @param jda Die JDA-Instanz des Bots
     * @param commandManager Der CommandManager, bei dem Befehle registriert werden
     */
    void initialize(JDA jda, CommandManager commandManager);

    /**
     * Gibt den Namen des Moduls zurück.
     *
     * @return Der Name des Moduls
     */
    String getName();

    /**
     * Gibt eine Beschreibung des Moduls zurück.
     *
     * @return Die Beschreibung des Moduls
     */
    String getDescription();

    /**
     * Bereinigt Ressourcen, die vom Modul verwendet werden.
     * Wird aufgerufen, wenn das Modul deaktiviert wird.
     */
    void cleanup();
}