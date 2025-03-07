package com.quartel.discordbot.modules.music.commands;

import com.quartel.discordbot.modules.music.player.GuildMusicManager;
import com.quartel.discordbot.modules.music.player.PlayerManager;
import com.quartel.discordbot.modules.music.util.MusicUtil;
import com.quartel.discordbot.modules.music.util.WaitingRoomManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diese Klasse implementiert den /skip Command, der das aktuelle Lied überspringt.
 */
public class SkipCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkipCommand.class);

    /**
     * Definiert die Slash-Command-Daten für den /skip Befehl.
     *
     * @return Die CommandData für den /skip Befehl
     */
    public static CommandData getCommandData() {
        return Commands.slash("skip", "Überspringt das aktuelle Lied");
    }

    /**
     * Behandelt den /skip Slash-Command.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public static void handle(SlashCommandInteractionEvent event) {
        LOGGER.debug("SkipCommand ausgeführt von: {}", event.getUser().getName());

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Dieser Befehl kann nur auf einem Server verwendet werden.").setEphemeral(true).queue();
            return;
        }

        // Prüfe, ob der Warteraum-Modus aktiv ist
        if (WaitingRoomManager.getInstance().isWaitingRoomActive(guild.getIdLong())) {
            event.reply("❌ Der `/skip` Befehl ist während des Warteraum-Modus deaktiviert. " +
                    "Verwende `/warteraum deaktivieren`, um den Warteraum-Modus zu beenden.").setEphemeral(true).queue();
            return;
        }

        // Prüfe, ob der Benutzer im gleichen Sprachkanal ist
        if (!MusicUtil.isInSameVoiceChannel(event)) {
            return;
        }

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
        AudioTrack currentTrack = musicManager.getAudioPlayer().getPlayingTrack();

        if (currentTrack == null) {
            event.reply("Es wird derzeit nichts abgespielt.").setEphemeral(true).queue();
            return;
        }

        // Speichere Informationen über den aktuellen Track, bevor wir ihn überspringen
        String skippedTrackTitle = currentTrack.getInfo().title;

        // Überspringe den aktuellen Track
        musicManager.getTrackScheduler().nextTrack(true);
        musicManager.updateActivity();

        AudioTrack newTrack = musicManager.getAudioPlayer().getPlayingTrack();

        if (newTrack != null) {
            // Es gibt einen nächsten Track
            event.reply("⏭️ **" + skippedTrackTitle + "** übersprungen.\n🎵 Spiele jetzt: **" + newTrack.getInfo().title + "**").queue();
        } else {
            // Es gibt keinen nächsten Track
            event.reply("⏭️ **" + skippedTrackTitle + "** übersprungen. Die Warteschlange ist jetzt leer.").queue();
        }
    }
}