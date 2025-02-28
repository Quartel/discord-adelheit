package com.quartel.discordbot.modules.music.commands;

import com.quartel.discordbot.modules.music.player.GuildMusicManager;
import com.quartel.discordbot.modules.music.player.PlayerManager;
import com.quartel.discordbot.modules.music.util.MusicUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diese Klasse implementiert den /nowplaying Command, der Informationen zum aktuellen Track anzeigt.
 */
public class NowPlayingCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(NowPlayingCommand.class);

    /**
     * Definiert die Slash-Command-Daten für den /nowplaying Befehl.
     *
     * @return Die CommandData für den /nowplaying Befehl
     */
    public static CommandData getCommandData() {
        return Commands.slash("nowplaying", "Zeigt Informationen zum aktuell spielenden Lied an");
    }

    /**
     * Behandelt den /nowplaying Slash-Command.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public static void handle(SlashCommandInteractionEvent event) {
        LOGGER.debug("NowPlayingCommand ausgeführt von: {}", event.getUser().getName());

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Dieser Befehl kann nur auf einem Server verwendet werden.").setEphemeral(true).queue();
            return;
        }

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);

        // Prüfe, ob etwas abgespielt wird
        if (musicManager.getAudioPlayer().getPlayingTrack() == null) {
            event.reply("Es wird derzeit nichts abgespielt.").queue();
            return;
        }

        // Aktualisiere die Aktivitätszeit
        musicManager.updateActivity();

        // Erstelle und sende das NowPlaying-Embed
        event.replyEmbeds(MusicUtil.createNowPlayingEmbed(guild)).queue();
    }
}