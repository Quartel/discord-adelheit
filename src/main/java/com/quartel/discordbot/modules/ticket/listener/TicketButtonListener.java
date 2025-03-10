package com.quartel.discordbot.modules.ticket.listener;

import com.quartel.discordbot.config.Config;
import com.quartel.discordbot.modules.ticket.util.TicketManager;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener für Ticket-bezogene Button-Interaktionen.
 */
public class TicketButtonListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketButtonListener.class);

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();

        // Prüfe, ob es sich um einen Ticket-Button handelt
        if (buttonId.equals("create_ticket")) {
            LOGGER.debug("Ticket-Erstellungs-Button geklickt von {}", event.getUser().getName());

            // Diese Methode werden wir später implementieren
            TicketManager.createTicket(event);
        }
        // Hinweis: Die Buttons zum Schließen von Tickets werden wir später hinzufügen
    }
}