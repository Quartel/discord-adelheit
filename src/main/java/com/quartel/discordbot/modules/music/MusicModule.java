package com.quartel.discordbot.modules.music;

import com.quartel.discordbot.modules.Module;
import com.quartel.discordbot.modules.music.commands.*;
import com.quartel.discordbot.modules.music.player.PlayerManager;
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

        // Registriere den Event-Listener
        jda.addEventListener(commandListener);

        // Zuerst alle globalen Commands löschen
        jda.updateCommands().queue(
                success -> {
                    LOGGER.info("Globale Commands gelöscht");

                    // Registriere die Slash-Commands
                    List<CommandData> commands = new ArrayList<>();
                    commands.add(PlayCommand.getCommandData());
                    commands.add(SkipCommand.getCommandData());
                    commands.add(StopCommand.getCommandData());
                    commands.add(QueueCommand.getCommandData());
                    commands.add(NowPlayingCommand.getCommandData());
                    commands.add(VolumeCommand.getCommandData());
                    commands.add(PauseResumeCommand.getPauseCommandData());
                    commands.add(PauseResumeCommand.getResumeCommandData());

                    // Guild-spezifische Commands registrieren für jede Guild
                    for (Guild guild : jda.getGuilds()) {
                        guild.updateCommands().addCommands(commands).queue(
                                guildSuccess -> LOGGER.info("Musik-Befehle erfolgreich für Guild {} registriert", guild.getName()),
                                guildError -> LOGGER.error("Fehler beim Registrieren der Musik-Befehle für Guild {}: {}",
                                        guild.getName(), guildError.getMessage())
                        );
                    }
                },
                error -> LOGGER.error("Fehler beim Löschen globaler Commands: {}", error.getMessage())
        );
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