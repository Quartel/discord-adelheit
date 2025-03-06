package com.quartel.discordbot.modules.music;

import com.quartel.discordbot.config.Config;
import com.quartel.discordbot.core.permissions.PermissionManager;
import com.quartel.discordbot.modules.Module;
import com.quartel.discordbot.modules.music.commands.*;
import com.quartel.discordbot.modules.music.player.PlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Diese Klasse implementiert das Musik-Modul, das alle Musik-bezogenen Befehle und Funktionen verwaltet.
 */
public class MusicModule extends Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(MusicModule.class);
    private final MusicCommandListener commandListener;
    private final PermissionManager permissionManager;
    private JDA jda;

    private static final String[] MUSIC_COMMANDS = {
            "play", "skip", "stop", "queue",
            "nowplaying", "volume", "pause", "resume"
    };

    /**
     * Initialisiert das Musik-Modul.
     */
    public MusicModule() {
        super("music", "Musik-Modul für Discord-Bot");
        this.commandListener = new MusicCommandListener();
        this.permissionManager = PermissionManager.getInstance();
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
     * Konfiguriert Standardberechtigungen für Musikbefehle.
     */
    private void configureDefaultPermissions() {
        if (jda == null) {
            LOGGER.warn("JDA nicht initialisiert für Berechtigungskonfiguration");
            return;
        }

        Set<String> djCommands = Set.of("play", "skip", "stop", "volume", "pause", "resume");

        for (Guild guild : jda.getGuilds()) {
            long guildId = guild.getIdLong();

            for (String command : MUSIC_COMMANDS) {
                PermissionManager.PermissionLevel level =
                        djCommands.contains(command)
                                ? PermissionManager.PermissionLevel.DJ_ROLE
                                : PermissionManager.PermissionLevel.EVERYONE;

                permissionManager.setCommandPermissionLevel(guildId, command, level);

                LOGGER.info("Berechtigungsstufe für Befehl {} auf Server {} gesetzt auf {}",
                        command, guildId, level);
            }
        }
    }

    /**
     * Wird aufgerufen, wenn das Modul aktiviert wird.
     *
     * @param jda Die JDA-Instanz
     */
    @Override
    public void onEnable(JDA jda) {
        LOGGER.info("Aktiviere Musik-Modul...");

        // Speichere JDA-Instanz
        this.jda = jda;

        // Setze die JDA-Instanz im PlayerManager
        PlayerManager.getInstance().setJDA(jda);

        // Konfiguriere Berechtigungen
        configureDefaultPermissions();

        // Registriere den Event-Listener
        jda.addEventListener(commandListener);
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
        private final PermissionManager permissionManager = PermissionManager.getInstance();

        /**
         * Wird aufgerufen, wenn ein Slash-Command ausgeführt wird.
         *
         * @param event Das SlashCommandInteractionEvent
         */
        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
            String commandName = event.getName();

            // Hole die Berechtigungsstufe für diesen spezifischen Befehl
            PermissionManager.PermissionLevel requiredLevel =
                    permissionManager.getCommandPermissionLevel(event.getGuild().getIdLong(), commandName);

            // Prüfe die Berechtigung für diesen spezifischen Befehl
            if (!permissionManager.hasPermission(event.getMember(), requiredLevel, commandName)) {
                event.reply("Du hast keine Berechtigung, den Befehl " + commandName + " auszuführen.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            // Command-Handling
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
    }
}