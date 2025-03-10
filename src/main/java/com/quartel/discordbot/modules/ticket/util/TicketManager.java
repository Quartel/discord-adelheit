package com.quartel.discordbot.modules.ticket.util;

import com.quartel.discordbot.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Random;

/**
 * Verwaltet die Erstellung und Verwaltung von Tickets.
 */
public class TicketManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketManager.class);
    private static final Random RANDOM = new Random();

    /**
     * Erstellt ein Modal zur Eingabe von Ticket-Informationen.
     *
     * @param event Das ButtonInteractionEvent
     */
    public static void createTicket(ButtonInteractionEvent event) {
        // Erstelle ein Modal (Popup-Formular) für weitere Ticket-Informationen
        TextInput descriptionInput = TextInput.create("ticket_description", "Beschreibe dein Anliegen", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Bitte beschreibe dein Problem oder deine Frage ausführlich...")
                .setMinLength(10)
                .setMaxLength(1000)
                .setRequired(true)
                .build();

        Modal modal = Modal.create("ticket_modal", "Ticket erstellen")
                .addActionRow(descriptionInput)
                .build();

        // Zeige das Modal dem Benutzer an
        event.replyModal(modal).queue();

        // Hinweis: Die Behandlung des Modal-Submits werden wir im nächsten Schritt implementieren
    }

    /**
     * Erstellt einen Ticket-Kanal.
     * Diese Methode implementieren wir im nächsten Schritt.
     */
    public static void createTicketChannel(Guild guild, User user, String description) {
        // Wird später implementiert
    }
}