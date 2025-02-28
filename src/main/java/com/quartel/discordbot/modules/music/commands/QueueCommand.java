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
 * Diese Klasse implementiert den /queue Command, der die aktuelle Warteschlange anzeigt.
 */
public class QueueCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueCommand.class);

    /**
     * Definiert die Slash-Command-Daten für den /queue Befehl.
     *
     * @return Die CommandData für den /queue Befehl
     */
    public static CommandData getCommandData() {
        return Commands.slash("queue", "Zeigt die aktuelle Musik-Warteschlange an");
    }

    /**
     * Behandelt den /queue Slash-Command.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public static void handle(SlashCommandInteractionEvent event) {
        LOGGER.debug("QueueCommand ausgeführt von: {}", event.getUser().getName());

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Dieser Befehl kann nur auf einem Server verwendet werden.").setEphemeral(true).queue();
            return;
        }

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);

        // Prüfe, ob die Warteschlange leer ist
        if (musicManager.getAudioPlayer().getPlayingTrack() == null &&
                musicManager.getTrackScheduler().getQueue().isEmpty()) {
            event.reply("Die Warteschlange ist leer.").queue();
            return;
        }

        // Erstelle und sende das Warteschlangen-Embed
        event.replyEmbeds(MusicUtil.createQueueEmbed(guild)).queue();
    }
}