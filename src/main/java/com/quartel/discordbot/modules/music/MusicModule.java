package com.quartel.discordbot.modules.music;

import com.quartel.discordbot.modules.Module;
import com.quartel.discordbot.modules.music.commands.*;
import com.quartel.discordbot.modules.music.player.PlayerManager;
import com.quartel.discordbot.modules.music.util.WaitingRoomManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Diese Klasse implementiert das Musik-Modul, das alle Musik-bezogenen Befehle und Funktionen verwaltet.
 */
public class MusicModule extends Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(MusicModule.class);
    private final MusicCommandListener commandListener;

    /**
     * Initialisiert das Musik-Modul.
     */
    public MusicModule() {
        super("music", "Musik-Modul für Discord-Bot");
        this.commandListener = new MusicCommandListener();
        LOGGER.info("Musik-Modul initialisiert");
    }

    /**
     * Wird aufgerufen, wenn das Modul geladen wird.
     */
    @Override
    public void onLoad() {
        LOGGER.info("Lade Musik-Modul...");

        // Stelle sicher, dass der PlayerManager initialisiert ist
        PlayerManager.getInstance();
    }

    /**
     * Wird aufgerufen, wenn das Modul aktiviert wird.
     *
     * @param jda Die JDA-Instanz
     */
    @Override
    public void onEnable(JDA jda) {
        LOGGER.info("Aktiviere Musik-Modul...");

        // Setze die JDA-Instanz im PlayerManager
        PlayerManager.getInstance().setJDA(jda);

        // WaitingRoomManager initialisieren
        WaitingRoomManager.getInstance().setJDA(jda);

        // Registriere den Event-Listener
        jda.addEventListener(commandListener);

        try {
            // Liste der Musik-Befehle erstellen
            List<CommandData> commands = new ArrayList<>();
            commands.add(PlayCommand.getCommandData());
            commands.add(SkipCommand.getCommandData());
            commands.add(StopCommand.getCommandData());
            commands.add(QueueCommand.getCommandData());
            commands.add(NowPlayingCommand.getCommandData());
            commands.add(VolumeCommand.getCommandData());
            commands.add(PauseResumeCommand.getPauseCommandData());
            commands.add(PauseResumeCommand.getResumeCommandData());
            commands.add(WarteraumCommand.getCommandData());

            // Registriere die Musik-Befehle für jede Guild
            for (Guild guild : jda.getGuilds()) {
                // Bestehende Befehle abrufen
                guild.retrieveCommands().queue(existingCommands -> {
                    // Erstelle eine Liste mit allen bestehenden Befehlsdaten
                    List<CommandData> updatedCommands = new ArrayList<>();

                    // Behalte alle Befehle, die nicht zu Musik gehören
                    for (net.dv8tion.jda.api.interactions.commands.Command cmd : existingCommands) {
                        String name = cmd.getName();
                        // Wenn es kein Musik-Befehl ist, behalte ihn
                        if (!name.equals("play") && !name.equals("skip") && !name.equals("stop") &&
                                !name.equals("queue") && !name.equals("nowplaying") && !name.equals("volume") &&
                                !name.equals("pause") && !name.equals("resume") && !name.equals("warteraum")) {

                            // Konvertiere den bestehenden Befehl in CommandData
                            CommandData cmdData = net.dv8tion.jda.api.interactions.commands.build.Commands.slash(
                                    cmd.getName(), cmd.getDescription());
                            updatedCommands.add(cmdData);
                        }
                    }

                    // Füge die neuen Musik-Befehle hinzu
                    updatedCommands.addAll(commands);

                    // Aktualisiere die Befehle auf dem Server
                    guild.updateCommands().addCommands(updatedCommands).queue(
                            success -> LOGGER.info("Musik-Befehle erfolgreich für Guild {} aktualisiert", guild.getName()),
                            error -> LOGGER.error("Fehler beim Aktualisieren der Befehle für Guild {}: {}",
                                    guild.getName(), error.getMessage())
                    );
                }, error -> LOGGER.error("Fehler beim Abrufen der Befehle für Guild {}: {}",
                        guild.getName(), error.getMessage()));
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
        LOGGER.info("Deaktiviere Musik-Modul...");

        // Entferne den Event-Listener
        jda.removeEventListener(commandListener);

        // Bereinige Ressourcen
        PlayerManager.getInstance().shutdown();

        // WaitingRoomManager herunterfahren
        WaitingRoomManager.getInstance().shutdown();
    }

    /**
     * Event-Listener für Musik-bezogene Slash-Commands.
     */
    private static class MusicCommandListener extends ListenerAdapter {
        /**
         * Wird aufgerufen, wenn ein Slash-Command ausgeführt wird.
         *
         * @param event Das SlashCommandInteractionEvent
         */
        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
            String commandName = event.getName();

            switch (commandName) {
                case "play":
                    PlayCommand.handle(event);
                    break;
                case "skip":
                    SkipCommand.handle(event);
                    break;
                case "stop":
                    StopCommand.handle(event);
                    break;
                case "queue":
                    QueueCommand.handle(event);
                    break;
                case "nowplaying":
                    NowPlayingCommand.handle(event);
                    break;
                case "volume":
                    VolumeCommand.handle(event);
                    break;
                case "pause":
                    PauseResumeCommand.handlePause(event);
                    break;
                case "resume":
                    PauseResumeCommand.handleResume(event);
                    break;
                case "warteraum":
                    WarteraumCommand.handle(event);
                    break;
            }
        }

        /**
         * Wird aufgerufen, wenn JDA heruntergefahren wird.
         *
         * @param event Das ShutdownEvent
         */
        @Override
        public void onShutdown(@NotNull ShutdownEvent event) {
            // Bereinige Ressourcen beim Herunterfahren
            PlayerManager.getInstance().shutdown();
        }
    }
}