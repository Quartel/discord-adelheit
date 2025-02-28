package com.quartel.discordbot.core;

import com.quartel.discordbot.core.listeners.MessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse verwaltet die Registrierung und Ausführung von Befehlen.
 */
public class CommandManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);

    private final JDA jda;
    private final List<ListenerAdapter> commandListeners = new ArrayList<>();
    private final List<CommandData> globalCommands = new ArrayList<>();

    /**
     * Erstellt einen neuen CommandManager.
     *
     * @param jda Die JDA-Instanz
     */
    public CommandManager(JDA jda) {
        this.jda = jda;
        LOGGER.info("CommandManager initialisiert");
    }

    /**
     * Registriert die Standard-Befehlslistener.
     */
    public void registerDefaultListeners() {
        // Registriere den MessageListener für Text-Befehle (z.B. !ping)
        addListener(new MessageListener());

        LOGGER.info("Standard-Befehlslistener registriert");
    }

    /**
     * Fügt einen Listener hinzu und registriert ihn bei JDA.
     *
     * @param listener Der hinzuzufügende ListenerAdapter
     */
    public void addListener(ListenerAdapter listener) {
        commandListeners.add(listener);
        jda.addEventListener(listener);
        LOGGER.debug("Listener {} registriert", listener.getClass().getSimpleName());
    }

    /**
     * Entfernt einen Listener und deregistriert ihn bei JDA.
     *
     * @param listener Der zu entfernende ListenerAdapter
     */
    public void removeListener(ListenerAdapter listener) {
        commandListeners.remove(listener);
        jda.removeEventListener(listener);
        LOGGER.debug("Listener {} entfernt", listener.getClass().getSimpleName());
    }

    /**
     * Entfernt alle registrierten Listener.
     */
    public void removeAllListeners() {
        for (ListenerAdapter listener : commandListeners) {
            jda.removeEventListener(listener);
        }
        commandListeners.clear();
        LOGGER.info("Alle Listener entfernt");
    }

    /**
     * Fügt einen globalen Slash-Command hinzu.
     *
     * @param command Die hinzuzufügende CommandData
     */
    public void addGlobalCommand(CommandData command) {
        globalCommands.add(command);
        LOGGER.debug("Globaler Command {} hinzugefügt", command.getName());
    }

    /**
     * Fügt mehrere globale Slash-Commands hinzu.
     *
     * @param commands Die hinzuzufügenden CommandData-Objekte
     */
    public void addGlobalCommands(List<CommandData> commands) {
        globalCommands.addAll(commands);
        LOGGER.debug("{} globale Commands hinzugefügt", commands.size());
    }

    /**
     * Registriert alle globalen Commands bei Discord.
     */
    public void registerGlobalCommands() {
        if (globalCommands.isEmpty()) {
            LOGGER.info("Keine globalen Commands zu registrieren");
            return;
        }

        jda.updateCommands().addCommands(globalCommands).queue(
                success -> LOGGER.info("{} globale Commands erfolgreich registriert", globalCommands.size()),
                error -> LOGGER.error("Fehler beim Registrieren globaler Commands: {}", error.getMessage())
        );
    }

    /**
     * Löscht alle globalen Commands.
     */
    public void deleteGlobalCommands() {
        jda.updateCommands().queue(
                success -> LOGGER.info("Alle globalen Commands gelöscht"),
                error -> LOGGER.error("Fehler beim Löschen globaler Commands: {}", error.getMessage())
        );

        globalCommands.clear();
    }

    /**
     * Gibt die JDA-Instanz zurück.
     *
     * @return Die JDA-Instanz
     */
    public JDA getJda() {
        return jda;
    }
}