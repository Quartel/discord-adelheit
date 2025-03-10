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
            // WICHTIG: Zuerst ALLE globalen Commands explizit löschen, um doppelte Einträge zu vermeiden
            LOGGER.info("Lösche alle globalen Befehle, um Duplikate zu vermeiden...");
            jda.updateCommands().queue(
                    success -> {
                        LOGGER.info("Globale Befehle erfolgreich gelöscht");

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

                        // Nach dem Löschen globaler Befehle NUR guild-spezifische Befehle registrieren
                        for (Guild guild : jda.getGuilds()) {
                            // Zuerst alle bestehenden Guild-Befehle löschen
                            guild.updateCommands().queue(
                                    guildCleanSuccess -> {
                                        LOGGER.info("Bestehende Befehle für Guild {} gelöscht", guild.getName());

                                        // Dann neue Befehle hinzufügen
                                        guild.updateCommands().addCommands(commands).queue(
                                                guildSuccess -> LOGGER.info("Musik-Befehle erfolgreich für Guild {} registriert", guild.getName()),
                                                guildError -> LOGGER.error("Fehler beim Registrieren der Musik-Befehle für Guild {}: {}",
                                                        guild.getName(), guildError.getMessage())
                                        );
                                    },
                                    guildCleanError -> LOGGER.error("Fehler beim Löschen von Befehlen für Guild {}: {}",
                                            guild.getName(), guildCleanError.getMessage())
                            );
                        }
                    },
                    error -> LOGGER.error("Fehler beim Löschen globaler Befehle: {}", error.getMessage())
            );
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