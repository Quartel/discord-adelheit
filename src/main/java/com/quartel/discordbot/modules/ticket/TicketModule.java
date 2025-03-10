package com.quartel.discordbot.modules.ticket;

import com.quartel.discordbot.modules.Module;
import com.quartel.discordbot.modules.ticket.commands.SetupTicketCommand;
import com.quartel.discordbot.modules.ticket.listener.TicketButtonListener;
import com.quartel.discordbot.modules.ticket.listener.TicketModalListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse implementiert das Ticket-Modul, das die Ticket-Funktionalität für Discord-Server bereitstellt.
 */
public class TicketModule extends Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketModule.class);
    private final TicketCommandListener commandListener;
    private final TicketButtonListener buttonListener;
    private final TicketModalListener modalListener;

    /**
     * Initialisiert das Ticket-Modul.
     */
    public TicketModule() {
        // Der Name des Moduls und eine kurze Beschreibung
        super("ticket", "Ticket-System für Support-Anfragen");
        this.commandListener = new TicketCommandListener();
        this.buttonListener = new TicketButtonListener();
        this.modalListener = new TicketModalListener();
        LOGGER.info("Ticket-Modul initialisiert");
    }

    /**
     * Wird aufgerufen, wenn das Modul geladen wird.
     */
    @Override
    public void onLoad() {
        LOGGER.info("Lade Ticket-Modul...");
        // Hier könnten wir z.B. Tickets aus einer Datenbank laden
    }

    /**
     * Wird aufgerufen, wenn das Modul aktiviert wird.
     *
     * @param jda Die JDA-Instanz
     */
    @Override
    public void onEnable(JDA jda) {
        LOGGER.info("Aktiviere Ticket-Modul...");

        // Listener registrieren
        jda.addEventListener(commandListener);
        jda.addEventListener(buttonListener);
        jda.addEventListener(modalListener);

        try {
            // Liste der Ticket-Befehle erstellen
            List<CommandData> commands = new ArrayList<>();
            commands.add(SetupTicketCommand.getCommandData());
            // Hier weitere Befehle hinzufügen

            // Registriere die Befehle für alle Guilds
            for (net.dv8tion.jda.api.entities.Guild guild : jda.getGuilds()) {
                guild.updateCommands().addCommands(commands).queue(
                        success -> LOGGER.info("Ticket-Befehle erfolgreich für Guild {} registriert", guild.getName()),
                        error -> LOGGER.error("Fehler beim Registrieren der Ticket-Befehle für Guild {}: {}",
                                guild.getName(), error.getMessage())
                );
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei der Befehlsregistrierung: {}", e.getMessage(), e);
        }
    }

    /**
     * Wird aufgerufen, wenn das Modul deaktiviert wird.
     *
     * @param jda Die JDA-Instanz
     */
    @Override
    public void onDisable(JDA jda) {
        LOGGER.info("Deaktiviere Ticket-Modul...");

        // Entferne die Event-Listener
        jda.removeEventListener(commandListener);
        jda.removeEventListener(buttonListener);
        jda.removeEventListener(modalListener);
    }

    /**
     * Event-Listener für Ticket-bezogene Slash-Commands.
     */
    private static class TicketCommandListener extends ListenerAdapter {
        /**
         * Wird aufgerufen, wenn ein Slash-Command ausgeführt wird.
         *
         * @param event Das SlashCommandInteractionEvent
         */
        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
            String commandName = event.getName();

            switch (commandName) {
                case "setupticket":
                    SetupTicketCommand.handle(event);
                    break;
                // Hier weitere Command-Fälle hinzufügen
            }
        }
    }
}