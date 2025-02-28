package com.quartel.discordbot.modules.music.commands;

import com.quartel.discordbot.modules.music.player.GuildMusicManager;
import com.quartel.discordbot.modules.music.player.PlayerManager;
import com.quartel.discordbot.modules.music.util.MusicUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diese Klasse implementiert den /stop Command, der die Musikwiedergabe stoppt
 * und die Warteschlange leert.
 */
public class StopCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(StopCommand.class);

    /**
     * Definiert die Slash-Command-Daten für den /stop Befehl.
     *
     * @return Die CommandData für den /stop Befehl
     */
    public static CommandData getCommandData() {
        return Commands.slash("stop", "Stoppt die Musikwiedergabe und leert die Warteschlange");
    }

    /**
     * Behandelt den /stop Slash-Command.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public static void handle(SlashCommandInteractionEvent event) {
        LOGGER.debug("StopCommand ausgeführt von: {}", event.getUser().getName());

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Dieser Befehl kann nur auf einem Server verwendet werden.").setEphemeral(true).queue();
            return;
        }

        // Prüfe, ob der Benutzer im gleichen Sprachkanal ist
        if (!MusicUtil.isInSameVoiceChannel(event)) {
            return;
        }

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);

        if (musicManager.getAudioPlayer().getPlayingTrack() == null &&
                musicManager.getTrackScheduler().getQueue().isEmpty()) {
            event.reply("Es wird derzeit nichts abgespielt.").setEphemeral(true).queue();
            return;
        }

        // Leere die Warteschlange und stoppe die Wiedergabe
        musicManager.getTrackScheduler().clearQueue();

        // Trennen vom Sprachkanal
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected()) {
            audioManager.closeAudioConnection();
            LOGGER.info("Audioverbindung für Server {} getrennt", guild.getName());
        }

        event.reply("⏹️ Wiedergabe gestoppt und Warteschlange geleert.").queue();
    }
}