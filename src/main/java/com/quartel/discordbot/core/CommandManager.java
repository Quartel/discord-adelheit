package com.quartel.discordbot.core;

import com.quartel.discordbot.util.BotUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Verwaltet und führt Befehle aus, die der Bot verarbeiten kann.
 * Registriert sowohl Textnachrichten-Befehle als auch Slash-Befehle.
 */
public class CommandManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);

    // Maps zur Verwaltung von Textnachrichten-Befehlen nach Name und Alias
    private final Map<String, Command> commandsByName = new HashMap<>();
    private final Map<String, Command> commandsByAlias = new HashMap<>();

    // Map zur Verwaltung von Slash-Befehlen nach Name
    private final Map<String, SlashCommand> slashCommandsByName = new HashMap<>();

    // Liste aller Slash-Befehl-Daten für die JDA-Registrierung
    private final List<CommandData> slashCommandsData = new ArrayList<>();

    /**
     * Registriert einen neuen Textnachrichten-Befehl beim CommandManager.
     *
     * @param command Der zu registrierende Befehl
     */
    public void registerCommand(Command command) {
        String name = command.getName().toLowerCase();

        // Überprüfe, ob bereits ein Befehl mit diesem Namen existiert
        if (commandsByName.containsKey(name)) {
            LOGGER.warn("Ein Befehl mit dem Namen '{}' ist bereits registriert. Der neue Befehl wird ignoriert.", name);
            return;
        }

        // Registriere den Befehl mit seinem Namen
        commandsByName.put(name, command);
        LOGGER.debug("Befehl '{}' registriert", name);

        // Registriere alle Aliase des Befehls
        for (String alias : command.getAliases()) {
            String lowerAlias = alias.toLowerCase();

            // Überprüfe, ob bereits ein Befehl mit diesem Alias existiert
            if (commandsByAlias.containsKey(lowerAlias) || commandsByName.containsKey(lowerAlias)) {
                LOGGER.warn("Ein Befehl oder Alias '{}' ist bereits registriert. Der Alias wird ignoriert.", lowerAlias);
                continue;
            }

            commandsByAlias.put(lowerAlias, command);
            LOGGER.debug("Alias '{}' für Befehl '{}' registriert", lowerAlias, name);
        }
    }

    /**
     * Registriert einen neuen Slash-Befehl beim CommandManager.
     *
     * @param command Der zu registrierende Slash-Befehl
     */
    public void registerSlashCommand(SlashCommand command) {
        String name = command.getName().toLowerCase();

        // Überprüfe, ob bereits ein Slash-Befehl mit diesem Namen existiert
        if (slashCommandsByName.containsKey(name)) {
            LOGGER.warn("Ein Slash-Befehl mit dem Namen '{}' ist bereits registriert. Der neue Befehl wird ignoriert.", name);
            return;
        }

        // Registriere den Slash-Befehl
        slashCommandsByName.put(name, command);
        slashCommandsData.add(command.getCommandData());
        LOGGER.debug("Slash-Befehl '{}' registriert", name);
    }

    /**
     * Verarbeitet einen Textnachrichten-Befehlsaufruf.
     *
     * @param commandName Der Name des Befehls
     * @param args Die Argumente für den Befehl
     * @param event Das auslösende MessageReceivedEvent
     */
    public void handleCommand(String commandName, List<String> args, MessageReceivedEvent event) {
        String lowerCommandName = commandName.toLowerCase();
        Command command = commandsByName.get(lowerCommandName);

        // Falls der Befehl nicht unter seinem Hauptnamen gefunden wurde, suche nach Aliassen
        if (command == null) {
            command = commandsByAlias.get(lowerCommandName);
        }

        // Wenn der Befehl gefunden wurde, führe ihn aus
        if (command != null) {
            try {
                LOGGER.debug("Führe Befehl '{}' aus", commandName);
                command.execute(args, event);
            } catch (Exception e) {
                // Protokolliere den Fehler und sende eine Benachrichtigung an den Kanal
                LOGGER.error("Fehler bei der Ausführung des Befehls '{}'", commandName, e);
                TextChannel channel = event.getChannel().asTextChannel();
                channel.sendMessageEmbeds(
                        BotUtils.createErrorEmbed(
                                "Fehler",
                                "Bei der Ausführung des Befehls ist ein Fehler aufgetreten: " + e.getMessage()
                        )
                ).queue();
            }
        } else {
            LOGGER.debug("Unbekannter Befehl: '{}'", commandName);
            // Optional: Sende eine Nachricht, dass der Befehl nicht gefunden wurde
            TextChannel channel = event.getChannel().asTextChannel();
            channel.sendMessageEmbeds(
                    BotUtils.createErrorEmbed(
                            "Unbekannter Befehl",
                            "Der Befehl '" + commandName + "' wurde nicht gefunden. Verwende `" +
                                    BotUtils.getPrefix() + "help` für eine Liste verfügbarer Befehle."
                    )
            ).queue();
        }
    }

    /**
     * Verarbeitet einen Slash-Befehlsaufruf.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        String commandName = event.getName().toLowerCase();
        SlashCommand command = slashCommandsByName.get(commandName);

        if (command != null) {
            try {
                LOGGER.debug("Führe Slash-Befehl '{}' aus", commandName);
                command.execute(event);
            } catch (Exception e) {
                // Protokolliere den Fehler und sende eine Benachrichtigung
                LOGGER.error("Fehler bei der Ausführung des Slash-Befehls '{}'", commandName, e);
                event.reply("Ein Fehler ist bei der Ausführung des Befehls aufgetreten: " + e.getMessage())
                        .setEphemeral(true)
                        .queue();
            }
        } else {
            LOGGER.warn("Unbekannter Slash-Befehl: '{}'", commandName);
            event.reply("Unbekannter Befehl: " + commandName)
                    .setEphemeral(true)
                    .queue();
        }
    }

    /**
     * Registriert alle konfigurierten Slash-Befehle bei Discord.
     * Muss nach dem Registrieren aller Slash-Befehle und vor ihrer Verwendung aufgerufen werden.
     *
     * @param jda Die JDA-Instanz des Bots
     */
    public void registerSlashCommandsWithDiscord(JDA jda) {
        if (slashCommandsData.isEmpty()) {
            LOGGER.info("Keine Slash-Befehle zum Registrieren vorhanden");
            return;
        }

        LOGGER.info("Registriere {} Slash-Befehle bei Discord...", slashCommandsData.size());

        // Registriere Befehle global (für alle Server)
        jda.updateCommands().addCommands(slashCommandsData).queue(
                success -> LOGGER.info("Slash-Befehle erfolgreich registriert"),
                error -> LOGGER.error("Fehler beim Registrieren der Slash-Befehle", error)
        );
    }

    /**
     * Gibt eine Liste aller registrierten Textnachrichten-Befehle zurück.
     *
     * @return Eine Liste aller Befehle
     */
    public List<Command> getCommands() {
        return new ArrayList<>(commandsByName.values());
    }

    /**
     * Gibt eine Liste aller registrierten Slash-Befehle zurück.
     *
     * @return Eine Liste aller Slash-Befehle
     */
    public List<SlashCommand> getSlashCommands() {
        return new ArrayList<>(slashCommandsByName.values());
    }

    /**
     * Gibt eine Liste aller Textnachrichten-Befehle einer bestimmten Kategorie zurück.
     *
     * @param category Die Kategorie
     * @return Eine Liste aller Befehle in dieser Kategorie
     */
    public List<Command> getCommandsByCategory(String category) {
        List<Command> result = new ArrayList<>();

        for (Command command : commandsByName.values()) {
            if (command.getCategory().equalsIgnoreCase(category)) {
                result.add(command);
            }
        }

        return result;
    }

    /**
     * Gibt eine Liste aller Slash-Befehle einer bestimmten Kategorie zurück.
     *
     * @param category Die Kategorie
     * @return Eine Liste aller Slash-Befehle in dieser Kategorie
     */
    public List<SlashCommand> getSlashCommandsByCategory(String category) {
        List<SlashCommand> result = new ArrayList<>();

        for (SlashCommand command : slashCommandsByName.values()) {
            if (command.getCategory().equalsIgnoreCase(category)) {
                result.add(command);
            }
        }

        return result;
    }
}