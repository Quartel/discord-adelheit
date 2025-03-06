package com.quartel.discordbot.modules.music.commands;

import com.quartel.discordbot.config.Config;
import com.quartel.discordbot.modules.music.util.MusicLibraryManager;
import com.quartel.discordbot.modules.music.util.WaitingRoomManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse implementiert den /warteraum Command, der den automatischen Warteraum-Modus
 * des Bots aktiviert und deaktiviert.
 */
public class WarteraumCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(WarteraumCommand.class);
    private static final MusicLibraryManager musicLibraryManager = new MusicLibraryManager();

    /**
     * Definiert die Slash-Command-Daten für den /warteraum Befehl.
     *
     * @return Die CommandData für den /warteraum Befehl
     */
    public static CommandData getCommandData() {
        // Option für die Playlist
        OptionData playlistOption = new OptionData(OptionType.STRING, "playlist",
                "Name der abzuspielenden Playlist", true)
                .setAutoComplete(true);

        // Subcommands erstellen
        SubcommandData activateCmd = new SubcommandData("aktivieren",
                "Aktiviert den Warteraum-Modus mit einer bestimmten Playlist")
                .addOptions(playlistOption);

        SubcommandData deactivateCmd = new SubcommandData("deaktivieren",
                "Deaktiviert den Warteraum-Modus");

        SubcommandData statusCmd = new SubcommandData("status",
                "Zeigt den aktuellen Status des Warteraum-Modus an");

        // Hauptbefehl mit Subcommands erstellen
        return Commands.slash("warteraum", "Verwaltet den automatischen Warteraum-Modus")
                .addSubcommands(activateCmd, deactivateCmd, statusCmd);
    }

    /**
     * Behandelt den /warteraum Slash-Command.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public static void handle(SlashCommandInteractionEvent event) {
        LOGGER.debug("WarteraumCommand ausgeführt von: {}", event.getUser().getName());

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Dieser Befehl kann nur auf einem Server verwendet werden.").setEphemeral(true).queue();
            return;
        }

        // Prüfe Berechtigungen
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("Du benötigst Administrator-Berechtigungen, um diesen Befehl zu verwenden.")
                    .setEphemeral(true).queue();
            return;
        }

        // Hole den Subcommand
        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            event.reply("Bitte gib einen gültigen Unterbefehl an.").setEphemeral(true).queue();
            return;
        }

        WaitingRoomManager waitingRoomManager = WaitingRoomManager.getInstance();

        // Prüfe, ob der Warteraum konfiguriert ist
        if (!waitingRoomManager.isWaitingRoomConfigured(guild) && !subcommand.equals("status")) {
            event.reply("Der Warteraum ist nicht konfiguriert. Bitte konfiguriere zuerst den Channel in der config.properties.")
                    .setEphemeral(true).queue();
            return;
        }

        switch (subcommand) {
            case "aktivieren":
                handleActivate(event, guild, waitingRoomManager);
                break;
            case "deaktivieren":
                handleDeactivate(event, guild, waitingRoomManager);
                break;
            case "status":
                handleStatus(event, guild, waitingRoomManager);
                break;
            default:
                event.reply("Unbekannter Unterbefehl: " + subcommand).setEphemeral(true).queue();
        }
    }

    /**
     * Behandelt den Unterbefehl zum Aktivieren des Warteraum-Modus.
     *
     * @param event Das SlashCommandInteractionEvent
     * @param guild Die Guild, für die der Warteraum aktiviert werden soll
     * @param waitingRoomManager Der WaitingRoomManager
     */
    private static void handleActivate(SlashCommandInteractionEvent event, Guild guild,
                                       WaitingRoomManager waitingRoomManager) {
        String playlistName = event.getOption("playlist").getAsString();

        // Prüfe, ob die Playlist existiert
        List<String> playlistFiles = musicLibraryManager.findAudioFilesInPlaylist(playlistName);
        if (playlistFiles.isEmpty()) {
            event.reply("❌ Keine Audiodateien in der Playlist '" + playlistName + "' gefunden.")
                    .setEphemeral(true).queue();
            return;
        }

        // Aktiviere den Warteraum
        boolean success = waitingRoomManager.activateWaitingRoom(guild, playlistName);

        if (success) {
            // Hole den Channel-Namen für die Bestätigungsnachricht
            String channelIdStr = Config.getProperty("warteraum.channel_id");
            String channelName = "Unbekannt";

            try {
                long channelId = Long.parseLong(channelIdStr);
                VoiceChannel channel = guild.getVoiceChannelById(channelId);
                if (channel != null) {
                    channelName = channel.getName();
                }
            } catch (NumberFormatException e) {
                // Ignorieren
            }

            event.reply("✅ Warteraum-Modus aktiviert! Der Bot spielt jetzt die Playlist '" +
                            playlistName + "' im Channel '" + channelName + "' ab.\n" +
                            "Der `/play` Befehl ist deaktiviert, solange der Warteraum-Modus aktiv ist.")
                    .queue();
        } else {
            event.reply("❌ Fehler beim Aktivieren des Warteraum-Modus. Überprüfe die Logs für Details.")
                    .setEphemeral(true).queue();
        }
    }

    /**
     * Behandelt den Unterbefehl zum Deaktivieren des Warteraum-Modus.
     *
     * @param event Das SlashCommandInteractionEvent
     * @param guild Die Guild, für die der Warteraum deaktiviert werden soll
     * @param waitingRoomManager Der WaitingRoomManager
     */
    private static void handleDeactivate(SlashCommandInteractionEvent event, Guild guild,
                                         WaitingRoomManager waitingRoomManager) {
        // Prüfe, ob der Warteraum überhaupt aktiviert ist
        if (!waitingRoomManager.isWaitingRoomActive(guild.getIdLong())) {
            event.reply("Der Warteraum-Modus ist nicht aktiviert.").setEphemeral(true).queue();
            return;
        }

        // Deaktiviere den Warteraum
        boolean success = waitingRoomManager.deactivateWaitingRoom(guild);

        if (success) {
            event.reply("✅ Warteraum-Modus deaktiviert! Der Bot hat den Sprachkanal verlassen und " +
                    "der `/play` Befehl ist wieder verfügbar.").queue();
        } else {
            event.reply("❌ Fehler beim Deaktivieren des Warteraum-Modus. Überprüfe die Logs für Details.")
                    .setEphemeral(true).queue();
        }
    }

    /**
     * Behandelt den Unterbefehl zum Anzeigen des Warteraum-Status.
     *
     * @param event Das SlashCommandInteractionEvent
     * @param guild Die Guild, für die der Status angezeigt werden soll
     * @param waitingRoomManager Der WaitingRoomManager
     */
    private static void handleStatus(SlashCommandInteractionEvent event, Guild guild,
                                     WaitingRoomManager waitingRoomManager) {
        boolean isActive = waitingRoomManager.isWaitingRoomActive(guild.getIdLong());
        boolean isConfigured = waitingRoomManager.isWaitingRoomConfigured(guild);

        StringBuilder statusMessage = new StringBuilder();
        statusMessage.append("**Warteraum-Status:**\n");

        if (isConfigured) {
            String channelIdStr = Config.getProperty("warteraum.channel_id");
            String channelName = "Unbekannt";

            try {
                long channelId = Long.parseLong(channelIdStr);
                VoiceChannel channel = guild.getVoiceChannelById(channelId);
                if (channel != null) {
                    channelName = channel.getName();
                }
            } catch (NumberFormatException e) {
                // Ignorieren
            }

            statusMessage.append("- Konfigurierter Kanal: ").append(channelName).append("\n");
            statusMessage.append("- Timeout: ").append(Config.getProperty("warteraum.auto_leave_timeout", "60")).append(" Sekunden\n");
        } else {
            statusMessage.append("⚠️ Der Warteraum ist nicht konfiguriert. Bitte konfiguriere den Channel in der config.properties.\n");
        }

        statusMessage.append("- Status: ").append(isActive ? "✅ **Aktiviert**" : "❌ **Deaktiviert**").append("\n");

        if (isActive) {
            statusMessage.append("- `/play` Befehl ist temporär deaktiviert\n");
            statusMessage.append("\nVerwende `/warteraum deaktivieren`, um den Warteraum-Modus zu beenden.");
        } else if (isConfigured) {
            statusMessage.append("\nVerwende `/warteraum aktivieren`, um den Warteraum-Modus zu starten.");
        }

        event.reply(statusMessage.toString()).queue();
    }
}