package com.quartel.discordbot.modules.ticket.commands;

import com.quartel.discordbot.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;

/**
 * Befehl zum Einrichten des Ticket-Panels.
 */
public class SetupTicketCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetupTicketCommand.class);

    /**
     * Erstellt die CommandData für den Befehl.
     *
     * @return Die CommandData für den Slash-Befehl
     */
    public static CommandData getCommandData() {
        return Commands.slash("setupticket", "Erstellt ein Ticket-Panel im aktuellen Kanal");
    }

    /**
     * Behandelt den Slash-Command.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public static void handle(SlashCommandInteractionEvent event) {
        LOGGER.debug("SetupTicketCommand ausgeführt von: {}", event.getUser().getName());

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Dieser Befehl kann nur auf einem Server verwendet werden.").setEphemeral(true).queue();
            return;
        }

        // Prüfe Berechtigungen
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("Du benötigst Administrator-Berechtigungen, um diesen Befehl zu verwenden.")
                    .setEphemeral(true).queue();
            return;
        }

        // Erstelle das Ticket-Panel
        String title = Config.getProperty("ticket.panel_title", "Support-Ticket erstellen");
        String description = Config.getProperty("ticket.panel_description",
                "Klicke auf den Button unten, um ein Support-Ticket zu erstellen.");
        String buttonLabel = Config.getProperty("ticket.button_label", "Ticket erstellen");

        MessageEmbed embed = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.BLUE)
                .build();

        // Sende das Panel mit Button
        event.getChannel().sendMessageEmbeds(embed)
                .addActionRow(Button.primary("create_ticket", buttonLabel))
                .queue(success -> {
                    LOGGER.info("Ticket-Panel erfolgreich im Kanal {} erstellt", event.getChannel().getName());
                    event.reply("Ticket-Panel wurde erfolgreich erstellt!").setEphemeral(true).queue();
                }, error -> {
                    LOGGER.error("Fehler beim Erstellen des Ticket-Panels: {}", error.getMessage(), error);
                    event.reply("Fehler beim Erstellen des Ticket-Panels. Bitte prüfe die Logs.").setEphemeral(true).queue();
                });
    }
}