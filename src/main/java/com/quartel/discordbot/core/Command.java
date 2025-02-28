package com.quartel.discordbot.core;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.util.List;

/**
 * Interface für alle Bot-Befehle.
 * Jeder Befehl muss dieses Interface implementieren.
 */
public interface Command {

    /**
     * Führt den Befehl aus.
     *
     * @param args Die Argumente, die dem Befehl übergeben wurden
     * @param event Das ursprüngliche MessageReceivedEvent, das den Befehl ausgelöst hat
     */
    void execute(List<String> args, MessageReceivedEvent event);

    /**
     * Gibt den Namen des Befehls zurück.
     * Dies ist der Name, der nach dem Präfix eingegeben wird, um den Befehl aufzurufen.
     *
     * @return Der Name des Befehls
     */
    String getName();

    /**
     * Gibt alternative Namen (Aliase) für den Befehl zurück.
     *
     * @return Eine Liste mit alternativen Namen oder eine leere Liste, wenn keine vorhanden sind
     */
    List<String> getAliases();

    /**
     * Gibt eine kurze Beschreibung des Befehls zurück.
     *
     * @return Die Beschreibung des Befehls
     */
    String getDescription();

    /**
     * Gibt die Syntax des Befehls zurück.
     * Dies ist die korrekte Verwendung des Befehls mit allen möglichen Argumenten.
     *
     * @return Die Syntax des Befehls
     */
    String getSyntax();

    /**
     * Gibt die Kategorie des Befehls zurück (z.B. "Musik", "Moderation").
     *
     * @return Die Kategorie des Befehls
     */
    String getCategory();
}