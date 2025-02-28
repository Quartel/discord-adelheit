package com.quartel.discordbot.core;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Interface für alle Slash-Befehle des Bots.
 */
public interface SlashCommand {

    /**
     * Führt den Slash-Befehl aus.
     *
     * @param event Das SlashCommandInteractionEvent, das den Befehl ausgelöst hat
     */
    void execute(SlashCommandInteractionEvent event);

    /**
     * Gibt die CommandData für diesen Slash-Befehl zurück.
     * CommandData enthält den Namen, die Beschreibung und die Parameter des Befehls.
     *
     * @return Die CommandData für diesen Befehl
     */
    CommandData getCommandData();

    /**
     * Gibt den Namen des Slash-Befehls zurück.
     *
     * @return Der Name des Befehls
     */
    String getName();

    /**
     * Gibt eine kurze Beschreibung des Slash-Befehls zurück.
     *
     * @return Die Beschreibung des Befehls
     */
    String getDescription();

    /**
     * Gibt die Kategorie des Befehls zurück (z.B. "Musik", "Moderation").
     *
     * @return Die Kategorie des Befehls
     */
    String getCategory();
}