package com.quartel.discordbot.core.commands;

import com.quartel.discordbot.core.BaseSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Ein einfacher Ping-Slash-Befehl zum Testen der Bot-Funktionalität.
 */
public class PingSlashCommand extends BaseSlashCommand {

    public PingSlashCommand() {
        super(
                "ping",                                    // Name des Befehls
                "Prüft, ob der Bot aktiv ist und misst die Latenz", // Beschreibung
                "Allgemein"                                // Kategorie
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Bestätige den Befehl und zeige "Messe Ping..." an
        event.deferReply().queue();

        // Hole den Gateway-Ping
        long gatewayPing = event.getJDA().getGatewayPing();

        // Berechne die Zeit zwischen dem Empfang des Befehls und der Antwort
        long apiPing = System.currentTimeMillis() - event.getTimeCreated().toInstant().toEpochMilli();

        // Sende die Antwort
        event.getHook().sendMessage(
                String.format("**Pong!** 🏓\nGateway Ping: %dms\nAPI Ping: %dms", gatewayPing, apiPing)
        ).queue();
    }
}