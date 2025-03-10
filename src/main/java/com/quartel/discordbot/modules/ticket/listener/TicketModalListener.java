package com.quartel.discordbot.modules.ticket.listener;

import com.quartel.discordbot.modules.ticket.util.TicketManager;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener für Ticket-bezogene Modal-Interaktionen (Formulare).
 */
public class TicketModalListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketModalListener.class);

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        // Prüfe, ob es sich um unser Ticket-Modal handelt
        if (event.getModalId().equals("ticket_modal")) {
            LOGGER.debug("Ticket-Modal wurde von {} abgesendet", event.getUser().getName());

            // Hole die Beschreibung aus dem Modal
            String description = event.getValue("ticket_description").getAsString();

            // Bestätige dem Benutzer, dass sein Ticket erstellt wird
            event.reply("Dein Ticket wird erstellt...").setEphemeral(true).queue();

            // Erstelle das Ticket mit den angegebenen Informationen
            TicketManager.createTicketChannel(event.getGuild(), event.getUser(), description);
        }
    }
}