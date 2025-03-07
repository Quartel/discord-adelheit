package com.quartel.discordbot.modules.music.commands;

import com.quartel.discordbot.modules.music.player.GuildMusicManager;
import com.quartel.discordbot.modules.music.player.PlayerManager;
import com.quartel.discordbot.modules.music.util.MusicUtil;
import com.quartel.discordbot.modules.music.util.WaitingRoomManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diese Klasse implementiert die /pause und /resume Commands,
 * die die Musikwiedergabe pausieren bzw. fortsetzen.
 */
public class PauseResumeCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(PauseResumeCommand.class);

    /**
     * Definiert die Slash-Command-Daten für den /pause Befehl.
     *
     * @return Die CommandData für den /pause Befehl
     */
    public static CommandData getPauseCommandData() {
        return Commands.slash("pause", "Pausiert die aktuelle Musikwiedergabe");
    }

    /**
     * Definiert die Slash-Command-Daten für den /resume Befehl.
     *
     * @return Die CommandData für den /resume Befehl
     */
    public static CommandData getResumeCommandData() {
        return Commands.slash("resume", "Setzt die pausierte Musikwiedergabe fort");
    }

    /**
     * Behandelt den /pause Slash-Command.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public static void handlePause(SlashCommandInteractionEvent event) {
        LOGGER.debug("PauseCommand ausgeführt von: {}", event.getUser().getName());

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Dieser Befehl kann nur auf einem Server verwendet werden.").setEphemeral(true).queue();
            return;
        }

        // Prüfe, ob der Warteraum-Modus aktiv ist
        if (WaitingRoomManager.getInstance().isWaitingRoomActive(guild.getIdLong())) {
            event.reply("❌ Der `/pause` Befehl ist während des Warteraum-Modus deaktiviert. " +
                    "Verwende `/warteraum deaktivieren`, um den Warteraum-Modus zu beenden.").setEphemeral(true).queue();
            return;
        }

        // Prüfe, ob der Benutzer im gleichen Sprachkanal ist
        if (!MusicUtil.isInSameVoiceChannel(event)) {
            return;
        }

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);

        // Prüfe, ob etwas abgespielt wird
        if (musicManager.getAudioPlayer().getPlayingTrack() == null) {
            event.reply("Es wird derzeit nichts abgespielt.").setEphemeral(true).queue();
            return;
        }

        // Prüfe, ob bereits pausiert
        if (musicManager.getAudioPlayer().isPaused()) {
            event.reply("Die Wiedergabe ist bereits pausiert. Verwende `/resume` zum Fortsetzen.").setEphemeral(true).queue();
            return;
        }

        // Pausiere die Wiedergabe
        musicManager.getAudioPlayer().setPaused(true);

        // Aktualisiere die Aktivitätszeit
        musicManager.updateActivity();

        event.reply("⏸️ Wiedergabe pausiert. Verwende `/resume` zum Fortsetzen.").queue();
    }

    /**
     * Behandelt den /resume Slash-Command.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public static void handleResume(SlashCommandInteractionEvent event) {
        LOGGER.debug("ResumeCommand ausgeführt von: {}", event.getUser().getName());

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Dieser Befehl kann nur auf einem Server verwendet werden.").setEphemeral(true).queue();
            return;
        }

        // Prüfe, ob der Warteraum-Modus aktiv ist
        if (WaitingRoomManager.getInstance().isWaitingRoomActive(guild.getIdLong())) {
            event.reply("❌ Der `/resume` Befehl ist während des Warteraum-Modus deaktiviert. " +
                    "Verwende `/warteraum deaktivieren`, um den Warteraum-Modus zu beenden.").setEphemeral(true).queue();
            return;
        }

        // Prüfe, ob der Benutzer im gleichen Sprachkanal ist
        if (!MusicUtil.isInSameVoiceChannel(event)) {
            return;
        }

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);

        // Prüfe, ob etwas abgespielt wird
        if (musicManager.getAudioPlayer().getPlayingTrack() == null) {
            event.reply("Es wird derzeit nichts abgespielt.").setEphemeral(true).queue();
            return;
        }

        // Prüfe, ob tatsächlich pausiert
        if (!musicManager.getAudioPlayer().isPaused()) {
            event.reply("Die Wiedergabe ist bereits aktiv.").setEphemeral(true).queue();
            return;
        }

        // Setze die Wiedergabe fort
        musicManager.getAudioPlayer().setPaused(false);

        // Aktualisiere die Aktivitätszeit
        musicManager.updateActivity();

        event.reply("▶️ Wiedergabe fortgesetzt.").queue();
    }
}