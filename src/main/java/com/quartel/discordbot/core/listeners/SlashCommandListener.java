package com.quartel.discordbot.core.listeners;

import com.quartel.discordbot.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener für Slash-Commands.
 * Leitet die Slash-Commands an die entsprechenden Handler weiter.
 */
public class SlashCommandListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommandListener.class);

    private final Bot bot;

    /**
     * Erstellt einen neuen SlashCommandListener mit Referenz auf die Bot-Instanz.
     *
     * @param bot Die Bot-Instanz
     */
    public SlashCommandListener(Bot bot) {
        this.bot = bot;
        LOGGER.debug("SlashCommandListener initialisiert");
    }

    /**
     * Wird aufgerufen, wenn ein Slash-Command ausgeführt wird.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        LOGGER.debug("Slash-Command erhalten: {} von {}", commandName, event.getUser().getName());

        // Commands direkt an ihre Handler in den Modulen delegieren
        // Die Module registrieren ihre eigenen Listener für ihre Commands
    }
}