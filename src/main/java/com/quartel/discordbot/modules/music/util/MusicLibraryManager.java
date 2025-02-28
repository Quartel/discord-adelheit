package com.quartel.discordbot.modules.music.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Verwaltet die Musikbibliothek und Playlist-Funktionen.
 */
public class MusicLibraryManager {
    private static final Logger logger = LoggerFactory.getLogger(MusicLibraryManager.class);
    private static final String MUSIC_LIBRARY_CONFIG = "src/main/resources/music_library.json";
    private static final String MUSIC_LIBRARY_PATH = "music_library";
    private static final String[] SUPPORTED_AUDIO_FORMATS = {"mp3", "wav", "flac"};

    /**
     * Lädt die Musik-Bibliothekskonfiguration.
     * @return JsonObject mit Playlist-Konfigurationen
     */
    public JsonObject loadMusicLibraryConfig() {
        try (FileReader reader = new FileReader(MUSIC_LIBRARY_CONFIG)) {
            return new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            logger.error("Fehler beim Laden der Musikbibliotheks-Konfiguration", e);
            return null;
        }
    }

    /**
     * Findet alle Audiodateien in einer bestimmten Playlist.
     * @param playlistName Name der Playlist
     * @return Liste der Audiodateipfade
     */
    public List<String> findAudioFilesInPlaylist(String playlistName) {
        JsonObject config = loadMusicLibraryConfig();
        if (config == null) return new ArrayList<>();

        // Finde Pfad der Playlist
        String playlistPath = config.getAsJsonArray("playlists")
                .asList()
                .stream()
                .filter(pl -> pl.getAsJsonObject().get("name").getAsString().equals(playlistName))
                .findFirst()
                .map(pl -> pl.getAsJsonObject().get("path").getAsString())
                .orElse(null);

        if (playlistPath == null) {
            logger.warn("Playlist {} nicht gefunden", playlistName);
            return new ArrayList<>();
        }

        try {
            return Files.walk(Paths.get(playlistPath))
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(this::isSupportedAudioFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Fehler beim Durchsuchen der Playlist {}", playlistName, e);
            return new ArrayList<>();
        }
    }

    /**
     * Prüft, ob eine Datei ein unterstütztes Audioformat hat.
     * @param filePath Dateipfad
     * @return true, wenn Audiodatei unterstützt wird
     */
    private boolean isSupportedAudioFile(String filePath) {
        String fileName = filePath.toLowerCase();
        for (String format : SUPPORTED_AUDIO_FORMATS) {
            if (fileName.endsWith("." + format)) {
                return true;
            }
        }
        return false;
    }
}