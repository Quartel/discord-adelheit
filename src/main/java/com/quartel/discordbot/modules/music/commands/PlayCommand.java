package com.quartel.discordbot.modules.music.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.quartel.discordbot.modules.music.player.PlayerManager;
import com.quartel.discordbot.modules.music.util.MusicLibraryManager;
import com.quartel.discordbot.modules.music.util.MusicUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.File;
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
        OptionData songOption = new OptionData(OptionType.STRING, "song", "Ein Lied-URL oder Suchbegriff", false);

        // Option für lokale Playlist
        OptionData playlistOption = new OptionData(OptionType.STRING, "playlist", "Name einer lokalen Playlist", false)
                .setAutoComplete(true);

        // Option für Playlist-Vorschau
        OptionData playlistPreviewOption = new OptionData(OptionType.STRING, "preview", "Zeige Details einer Playlist", false)
                .setAutoComplete(true);

        return Commands.slash("play", "Spielt Musik ab oder zeigt Playlist-Informationen")
                .addOptions(songOption, playlistOption, playlistPreviewOption);
    }

    /**
     * Behandelt den /play Slash-Command.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public static void handle(SlashCommandInteractionEvent event) {
        LOGGER.debug("PlayCommand ausgeführt von: {}", event.getUser().getName());

        // Playlist-Vorschau, wenn angefordert
        if (event.getOption("preview") != null) {
            showPlaylistPreview(event);
            return;
        }

        // Listet Playlists, wenn kein Lied/Playlist angegeben
        if (event.getOption("song") == null && event.getOption("playlist") == null) {
            listAvailablePlaylists(event);
            return;
        }

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


        // Prüfen, ob es eine URL ist, da direkte Suche nicht mehr unterstützt wird
        if (!isUrl(song)) {
            event.reply("❌ Bitte gib eine direkte URL ein. Die Suchfunktion wird momentan nicht unterstützt.").setEphemeral(true).queue();
            return;
        }

        // Lade und spiele den Track
        PlayerManager.getInstance().loadAndPlay(event, song);
    }

    /**
     * Zeigt Details einer ausgewählten Playlist.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    private static void showPlaylistPreview(SlashCommandInteractionEvent event) {
        String playlistName = event.getOption("preview").getAsString();
        List<String> files = musicLibraryManager.findAudioFilesInPlaylist(playlistName);

        if (files.isEmpty()) {
            event.reply("❌ Keine Informationen zur Playlist '" + playlistName + "' gefunden.").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Playlist: " + playlistName)
                .setColor(Color.BLUE);

        // Playlist-Beschreibung aus Konfiguration holen
        JsonObject config = musicLibraryManager.loadMusicLibraryConfig();
        if (config != null) {
            JsonArray playlists = config.getAsJsonArray("playlists");
            for (int i = 0; i < playlists.size(); i++) {
                JsonObject playlist = playlists.get(i).getAsJsonObject();
                if (playlist.get("name").getAsString().equals(playlistName)) {
                    embedBuilder.setDescription(playlist.get("description").getAsString());
                    break;
                }
            }
        }

        // Track-Informationen hinzufügen
        StringBuilder tracksInfo = new StringBuilder();
        for (int i = 0; i < Math.min(files.size(), 10); i++) {
            File file = new File(files.get(i));
            tracksInfo.append("`").append(i + 1).append(".` ").append(file.getName()).append("\n");
        }

        if (files.size() > 10) {
            tracksInfo.append("*... und ").append(files.size() - 10).append(" weitere Tracks*");
        }

        embedBuilder.addField("Tracks", tracksInfo.toString(), false)
                .addField("Anzahl der Tracks", String.valueOf(files.size()), true)
                .addField("Unterstützte Formate", "MP3, WAV, FLAC", true);

        event.replyEmbeds(embedBuilder.build()).queue();
    }

    /**
     * Listet verfügbare Playlists auf.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    private static void listAvailablePlaylists(SlashCommandInteractionEvent event) {
        JsonObject config = musicLibraryManager.loadMusicLibraryConfig();

        if (config == null || !config.has("playlists")) {
            event.reply("❌ Keine Playlists gefunden.").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Verfügbare Playlists")
                .setColor(Color.GREEN)
                .setDescription("Nutze `/play playlist:PLAYLIST_NAME`, um eine Playlist abzuspielen.\n" +
                        "Für Vorschau nutze `/play preview:PLAYLIST_NAME`");

        JsonArray playlists = config.getAsJsonArray("playlists");
        for (int i = 0; i < playlists.size(); i++) {
            JsonObject playlist = playlists.get(i).getAsJsonObject();
            String name = playlist.get("name").getAsString();
            String description = playlist.get("description").getAsString();

            embedBuilder.addField(name, description, false);
        }

        event.replyEmbeds(embedBuilder.build()).queue();
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