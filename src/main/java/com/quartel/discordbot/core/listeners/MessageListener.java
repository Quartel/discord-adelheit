package com.quartel.discordbot.core.listeners;

import com.quartel.discordbot.Bot;
import com.quartel.discordbot.util.BotUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Listener für eingehende Nachrichten im Discord.
 * Erkennt Befehle und leitet sie an den CommandManager weiter.
 */
public class MessageListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

    /**
     * Wird aufgerufen, wenn eine neue Nachricht im Discord empfangen wird.
     *
     * @param event Das MessageReceivedEvent mit Informationen zur Nachricht
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignoriere Nachrichten von Bots (inklusive diesem Bot)
        if (event.getAuthor().isBot()) {
            return;
        }

        // Hole die Nachricht und überprüfe, ob es ein Befehl ist
        Message message = event.getMessage();
        String content = message.getContentRaw();

        // Überprüfe, ob die Nachricht mit dem konfigurierten Präfix beginnt
        String prefix = BotUtils.getPrefix();
        if (!content.startsWith(prefix)) {
            return; // Keine Befehlsnachricht, ignorieren
        }

        // Extrahiere den Befehlsnamen und die Argumente
        String commandString = content.substring(prefix.length()).trim();
        if (commandString.isEmpty()) {
            return; // Nur das Präfix wurde eingegeben, ignorieren
        }

        // Teile den Befehl in Befehlsname und Argumente auf
        String[] splitCommand = commandString.split("\\s+");
        String commandName = splitCommand[0].toLowerCase();

        // Extrahiere Argumente (falls vorhanden)
        List<String> args = splitCommand.length > 1
                ? Arrays.asList(Arrays.copyOfRange(splitCommand, 1, splitCommand.length))
                : List.of();

        // Protokolliere den Befehlsaufruf
        User author = event.getAuthor();
        TextChannel channel = event.getChannel().asTextChannel();
        LOGGER.info("Befehl erkannt: '{}' von Benutzer {} in Kanal #{} mit Argumenten: {}",
                commandName, author.getName(), channel.getName(), args);

        // Leite den Befehl an den CommandManager weiter (wenn verfügbar)
        try {
            if (Bot.getCommandManager() != null) {
                Bot.getCommandManager().handleCommand(commandName, args, event);
            } else {
                LOGGER.warn("CommandManager ist noch nicht initialisiert. Befehl '{}' wird ignoriert.", commandName);
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei der Verarbeitung des Befehls '{}'", commandName, e);
            channel.sendMessage("Ein Fehler ist aufgetreten bei der Ausführung des Befehls: " + e.getMessage())
                    .queue();
        }
    }
}