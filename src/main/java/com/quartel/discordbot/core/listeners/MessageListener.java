package com.quartel.discordbot.core.listeners;

import com.quartel.discordbot.util.BotUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener für Text-Nachrichten.
 * Verarbeitet Befehle, die als normale Textnachrichten gesendet werden.
 */
public class MessageListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

    /**
     * Wird aufgerufen, wenn eine Nachricht empfangen wird.
     *
     * @param event Das MessageReceivedEvent
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Ignoriere Nachrichten von Bots (inklusive sich selbst)
        if (event.getAuthor().isBot()) {
            return;
        }

        String content = event.getMessage().getContentRaw();

        // Prüfe, ob die Nachricht mit dem Bot-Präfix beginnt
        if (BotUtils.startsWithPrefix(content)) {
            String command = BotUtils.getCommand(content);
            String[] args = BotUtils.getArgs(content);

            // Verarbeite den Befehl
            handleCommand(event, command, args);
        }
    }

    /**
     * Verarbeitet einen Befehl.
     *
     * @param event   Das MessageReceivedEvent
     * @param command Der Befehl (ohne Präfix)
     * @param args    Die Argumente des Befehls
     */
    private void handleCommand(MessageReceivedEvent event, String command, String[] args) {
        LOGGER.debug("Verarbeite Befehl: {} mit {} Argumenten von {}",
                command, args.length, event.getAuthor().getName());

        switch (command.toLowerCase()) {
            case "ping":
                handlePingCommand(event);
                break;
            // Weitere Text-Befehle können hier hinzugefügt werden
        }
    }

    /**
     * Behandelt den !ping Befehl.
     *
     * @param event Das MessageReceivedEvent
     */
    private void handlePingCommand(MessageReceivedEvent event) {
        long startTime = System.currentTimeMillis();

        event.getChannel().sendMessage("Pong! Berechne Latenz...").queue(message -> {
            long endTime = System.currentTimeMillis();
            long ping = endTime - startTime;
            long gatewayPing = event.getJDA().getGatewayPing();

            message.editMessage(String.format("Pong! 🏓\nBot-Latenz: %dms\nGateway-Ping: %dms",
                    ping, gatewayPing)).queue();

            LOGGER.debug("Ping-Befehl ausgeführt. Latenz: {}ms, Gateway: {}ms", ping, gatewayPing);
        });
    }
}