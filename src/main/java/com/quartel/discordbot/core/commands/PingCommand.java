package com.quartel.discordbot.core.commands;

import com.quartel.discordbot.core.BaseCommand;
import com.quartel.discordbot.util.BotUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * Ein einfacher Ping-Befehl zum Testen der Bot-Funktionalität.
 */
public class PingCommand extends BaseCommand {

    public PingCommand() {
        super(
                "ping",                  // Name des Befehls
                "Prüft, ob der Bot aktiv ist und misst die Latenz", // Beschreibung
                "ping",                  // Syntax
                "Allgemein",             // Kategorie
                List.of("pong", "test")  // Aliase
        );
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();

        // Sendet eine Testnachricht und misst, wie lange die Antwort dauert
        event.getChannel().sendMessage("Messe Ping...").queue(message -> {
            // Korrigierter Zugriff auf die Erstellungszeit der Nachricht
            long ping = System.currentTimeMillis() - event.getMessage().getTimeCreated().toInstant().toEpochMilli();

            // Aktualisiere die Nachricht mit den Ping-Informationen
            message.editMessageEmbeds(
                    BotUtils.createInfoEmbed(
                            "Pong! 🏓",
                            String.format(
                                    "Gateway Ping: %dms\nAPI Ping: %dms",
                                    gatewayPing,
                                    ping
                            )
                    )
            ).queue();
        });
    }
}