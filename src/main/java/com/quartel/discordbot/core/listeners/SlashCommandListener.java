package com.quartel.discordbot.core.listeners;

import com.quartel.discordbot.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener für Slash-Command-Interaktionen.
 * Verarbeitet SlashCommandInteractionEvents und leitet sie an den CommandManager weiter.
 */
public class SlashCommandListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommandListener.class);

    /**
     * Wird aufgerufen, wenn ein Slash-Befehl ausgeführt wird.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Protokolliere den Slash-Befehl
        LOGGER.info("Slash-Befehl '{}' von Benutzer {} in {}",
                event.getName(),
                event.getUser().getName(),
                event.getGuild() != null ? "Server " + event.getGuild().getName() : "DM");

        // Leite den Befehl an den CommandManager weiter
        try {
            if (Bot.getCommandManager() != null) {
                Bot.getCommandManager().handleSlashCommand(event);
            } else {
                LOGGER.warn("CommandManager ist noch nicht initialisiert. Slash-Befehl '{}' wird ignoriert.", event.getName());
                event.reply("Der Bot ist noch nicht vollständig initialisiert. Bitte versuche es später erneut.")
                        .setEphemeral(true) // Nur für den Benutzer sichtbar
                        .queue();
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei der Verarbeitung des Slash-Befehls '{}'", event.getName(), e);
            event.reply("Ein Fehler ist bei der Ausführung des Befehls aufgetreten: " + e.getMessage())
                    .setEphemeral(true)
                    .queue();
        }
    }
}