package com.quartel.discordbot.modules.music.commands;

import com.quartel.discordbot.modules.music.player.PlayerManager;
import com.quartel.discordbot.modules.music.util.MusicLibraryManager;
import com.quartel.discordbot.modules.music.util.MusicUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Diese Klasse implementiert den /play Command, der Musik abspielt oder zur Warteschlange hinzufügt.
 */
public class PlayCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayCommand.class);
    private static final MusicLibraryManager musicLibraryManager = new MusicLibraryManager();

    /**
     * Definiert die Slash-Command-Daten für den /play Befehl.
     *
     * @return Die CommandData für den /play Befehl
     */
    public static CommandData getCommandData() {
        // Option für Soundcloud/YouTube
        OptionData songOption = new OptionData(OptionType.STRING, "song", "Ein Lied-URL oder Suchbegriff", true);

        // Option für lokale Playlist
        OptionData playlistOption = new OptionData(OptionType.STRING, "playlist", "Name einer lokalen Playlist", false);

        return Commands.slash("play", "Spielt Musik ab oder fügt sie zur Warteschlange hinzu")
                .addOptions(songOption, playlistOption);
    }

    /**
     * Behandelt den /play Slash-Command.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public static void handle(SlashCommandInteractionEvent event) {
        LOGGER.debug("PlayCommand ausgeführt von: {}", event.getUser().getName());

        // Prüfe, ob der Benutzer in einem Sprachkanal ist und der Bot verbinden kann
        if (!MusicUtil.isInSameVoiceChannel(event)) {
            return;
        }

        // Mit dem Sprachkanal des Benutzers verbinden, falls noch nicht verbunden
        if (!MusicUtil.connectToUserVoiceChannel(event)) {
            return;
        }

        // Überprüfe, ob eine lokale Playlist angegeben wurde
        if (event.getOption("playlist") != null) {
            String playlistName = event.getOption("playlist").getAsString();
            List<String> playlistFiles = musicLibraryManager.findAudioFilesInPlaylist(playlistName);

            if (playlistFiles.isEmpty()) {
                event.reply("❌ Keine Audiodateien in der Playlist '" + playlistName + "' gefunden.").setEphemeral(true).queue();
                return;
            }

            // Deferred Reply senden, da das Laden mehrerer Dateien länger dauern kann
            event.deferReply().queue();

            // Alle Dateien der Playlist zur Warteschlange hinzufügen
            playlistFiles.forEach(file -> {
                PlayerManager.getInstance().loadAndPlay(event, file);
            });

            event.getHook().sendMessage("🎵 Playlist '" + playlistName + "' wird abgespielt.").queue();
            return;
        }

        // Extrahiere den Song-Parameter aus dem Befehl
        String song = event.getOption("song").getAsString();

        // Wenn es keine vollständige URL ist, behandle es als YouTube-Suche
        if (!isUrl(song)) {
            song = "ytsearch:" + song;
            LOGGER.debug("Suche nach: {}", song);
        }

        // Lade und spiele den Track
        PlayerManager.getInstance().loadAndPlay(event, song);
    }

    /**
     * Überprüft, ob ein String eine URL ist.
     *
     * @param url Der zu überprüfende String
     * @return true, wenn der String eine URL ist, sonst false
     */
    private static boolean isUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
}