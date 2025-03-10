package com.quartel.discordbot.modules.ticket.listener;

import com.quartel.discordbot.config.Config;
import com.quartel.discordbot.modules.ticket.util.TicketManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
            TicketManager.createTicket(event);
        }
        // Button zum Schließen eines Tickets
        else if (buttonId.equals("close_ticket")) {
            LOGGER.debug("Ticket-Schließen-Button geklickt von {}", event.getUser().getName());

            Guild guild = event.getGuild();
            Member member = event.getMember();
            TextChannel channel = event.getChannel().asTextChannel();

            if (guild == null || member == null) {
                event.reply("Fehler: Guild oder Member ist null").setEphemeral(true).queue();
                return;
            }

            // Prüfe, ob der Benutzer berechtigt ist, das Ticket zu schließen
            boolean canCloseTicket = member.hasPermission(Permission.ADMINISTRATOR);

            // Prüfe zusätzlich die Staff-Rolle
            String staffRoleIdStr = Config.getProperty("ticket.staff_role_id");
            if (!canCloseTicket && staffRoleIdStr != null && !staffRoleIdStr.isEmpty()) {
                try {
                    long staffRoleId = Long.parseLong(staffRoleIdStr);
                    Role staffRole = guild.getRoleById(staffRoleId);

                    if (staffRole != null && member.getRoles().contains(staffRole)) {
                        canCloseTicket = true;
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warn("Ungültige Staff-Rollen-ID in der Konfiguration", e);
                }
            }

            if (!canCloseTicket) {
                event.reply("Du hast keine Berechtigung, dieses Ticket zu schließen.")
                        .setEphemeral(true).queue();
                return;
            }

            // Bestätige dem Benutzer, dass das Ticket geschlossen wird
            event.reply("Das Ticket wird geschlossen und ein Transkript erstellt...")
                    .setEphemeral(true).queue();

            // Schließe das Ticket
            TicketManager.closeTicket(guild, channel, event.getUser())
                    .exceptionally(e -> {
                        LOGGER.error("Fehler beim Schließen des Tickets: {}", e.getMessage(), e);
                        return null;
                    });
        }
    }
}