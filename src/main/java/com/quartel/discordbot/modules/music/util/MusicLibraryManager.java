package com.quartel.discordbot.modules.music.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

    // Pfade für die Konfigurationsdatei
    private static final String[] POSSIBLE_CONFIG_PATHS = {
            "config/music_library.json",                    // Relativer Pfad im Hauptverzeichnis
            "music_library.json",                           // Direkt im Hauptverzeichnis
            "src/main/resources/music_library.json"         // Entwicklungspfad (für IDE)
    };

    private static final String[] SUPPORTED_AUDIO_FORMATS = {"mp3", "wav", "flac"};

    // Standard-Playlist-Verzeichnisse (werden nur verwendet, wenn keine Konfiguration existiert)
    private static final String MUSIC_LIBRARY_ROOT = "music_library";
    private static final String[] DEFAULT_PLAYLISTS = {"chill", "energetic"};

    /**
     * Konstruktor, der auch direkt die Bibliotheksordner initialisiert
     */
    public MusicLibraryManager() {
        // Beim Erstellen des MusicLibraryManager die Ordner initialisieren
        initializeLibraries();
    }

    /**
     * Initialisiert die Musikbibliothek-Ordner basierend auf der Konfiguration oder Standardwerten.
     */
    public void initializeLibraries() {
        logger.info("Initialisiere Musikbibliothek-Ordner...");

        // Erstelle das Hauptverzeichnis, falls es nicht existiert
        File rootDir = new File(MUSIC_LIBRARY_ROOT);
        if (!rootDir.exists()) {
            if (rootDir.mkdir()) {
                logger.info("Hauptverzeichnis für Musikbibliothek erstellt: {}", rootDir.getAbsolutePath());
            } else {
                logger.error("Konnte Hauptverzeichnis für Musikbibliothek nicht erstellen: {}", rootDir.getAbsolutePath());
                return;
            }
        }

        // Versuche die Konfigurationsdatei zu laden
        JsonObject config = tryLoadConfig();

        if (config != null && config.has("playlists")) {
            // Wenn eine gültige Konfiguration existiert, erstelle Ordner basierend auf der Konfiguration
            JsonArray playlists = config.getAsJsonArray("playlists");

            for (JsonElement element : playlists) {
                JsonObject playlist = element.getAsJsonObject();
                String name = playlist.get("name").getAsString();
                String pathStr = playlist.get("path").getAsString();

                // Normalisiere den Pfad (entferne ggf. führenden Slash oder Backslash)
                if (pathStr.startsWith("/") || pathStr.startsWith("\\")) {
                    pathStr = pathStr.substring(1);
                }

                // Erstelle den Ordner
                File playlistDir = new File(pathStr);
                if (!playlistDir.exists()) {
                    // Stelle sicher, dass das übergeordnete Verzeichnis existiert
                    playlistDir.getParentFile().mkdirs();

                    if (playlistDir.mkdir()) {
                        logger.info("Playlist-Verzeichnis erstellt: {}", playlistDir.getAbsolutePath());
                        createReadmeFile(playlistDir, name);
                    } else {
                        logger.error("Konnte Playlist-Verzeichnis nicht erstellen: {}", playlistDir.getAbsolutePath());
                    }
                }
            }

        } else {
            // Wenn keine gültige Konfiguration existiert, erstelle Standardordner und Konfigurationsdatei
            createDefaultFoldersAndConfig();
        }
    }

    /**
     * Erstellt eine README.txt-Datei im angegebenen Verzeichnis.
     */
    private void createReadmeFile(File directory, String playlistName) {
        try {
            File readmeFile = new File(directory, "README.txt");
            try (FileWriter writer = new FileWriter(readmeFile)) {
                writer.write(String.format(
                        "# %s Playlist\n\n" +
                                "In diesen Ordner können Sie Audiodateien für die Playlist '%s' ablegen.\n\n" +
                                "Unterstützte Formate: MP3, WAV, FLAC\n\n" +
                                "Um die Playlist abzuspielen, verwenden Sie den Befehl:\n" +
                                "/play playlist:%s\n",
                        playlistName.substring(0, 1).toUpperCase() + playlistName.substring(1),
                        playlistName,
                        playlistName
                ));
            }
            logger.info("README.txt für Playlist erstellt: {}", readmeFile.getAbsolutePath());
        } catch (IOException e) {
            logger.warn("Konnte README.txt für Playlist nicht erstellen: {}", e.getMessage());
        }
    }

    /**
     * Erstellt Standard-Playlist-Ordner und eine Standard-Konfigurationsdatei.
     */
    private void createDefaultFoldersAndConfig() {
        logger.info("Erstelle Standard-Playlist-Ordner und Konfiguration...");

        // Erstelle die Standard-Playlist-Verzeichnisse
        for (String playlist : DEFAULT_PLAYLISTS) {
            File playlistDir = new File(MUSIC_LIBRARY_ROOT, playlist);
            if (!playlistDir.exists()) {
                if (playlistDir.mkdir()) {
                    logger.info("Standard-Playlist-Verzeichnis erstellt: {}", playlistDir.getAbsolutePath());
                    createReadmeFile(playlistDir, playlist);
                } else {
                    logger.error("Konnte Standard-Playlist-Verzeichnis nicht erstellen: {}", playlistDir.getAbsolutePath());
                }
            }
        }

        // Erstelle eine Standard-Konfigurationsdatei
        createDefaultConfigFile();
    }

    /**
     * Versucht, die Konfigurationsdatei zu laden.
     * @return Die geladene Konfiguration oder null, wenn keine gefunden wurde
     */
    private JsonObject tryLoadConfig() {
        for (String configPath : POSSIBLE_CONFIG_PATHS) {
            File configFile = new File(configPath);
            if (configFile.exists() && configFile.isFile()) {
                try (FileReader reader = new FileReader(configFile)) {
                    logger.info("Lade Musikbibliothek-Konfiguration aus: {}", configFile.getAbsolutePath());
                    return new Gson().fromJson(reader, JsonObject.class);
                } catch (IOException e) {
                    logger.error("Fehler beim Laden der Musikbibliotheks-Konfiguration von {}", configPath, e);
                }
            }
        }
        return null;
    }

    /**
     * Erstellt eine Standard-Konfigurationsdatei.
     */
    private void createDefaultConfigFile() {
        // Erstelle das config-Verzeichnis, falls es nicht existiert
        File configDir = new File("config");
        if (!configDir.exists()) {
            if (!configDir.mkdir()) {
                logger.error("Konnte Konfigurationsverzeichnis nicht erstellen");
                return;
            }
        }

        // Erstelle eine neue Konfigurationsdatei
        File configFile = new File("config/music_library.json");
        try {
            JsonObject config = new JsonObject();
            JsonArray playlists = new JsonArray();

            // Füge die Standardplaylists hinzu
            for (String playlist : DEFAULT_PLAYLISTS) {
                JsonObject playlistObj = new JsonObject();
                playlistObj.addProperty("name", playlist);
                playlistObj.addProperty("path", "music_library/" + playlist);

                // Füge eine Beschreibung basierend auf dem Playlist-Namen hinzu
                String description;
                if (playlist.equals("chill")) {
                    description = "Relaxing music collection";
                } else if (playlist.equals("energetic")) {
                    description = "High-energy tracks";
                } else {
                    description = "Music playlist";
                }
                playlistObj.addProperty("description", description);

                // Füge unterstützte Formate hinzu
                JsonArray formats = new JsonArray();
                for (String format : SUPPORTED_AUDIO_FORMATS) {
                    formats.add(format);
                }
                playlistObj.add("supportedFormats", formats);

                playlists.add(playlistObj);
            }

            config.add("playlists", playlists);

            // Schreibe die Konfiguration in die Datei
            try (FileWriter writer = new FileWriter(configFile)) {
                new Gson().toJson(config, writer);
            }

            logger.info("Standard-Konfigurationsdatei erstellt: {}", configFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Konnte Konfigurationsdatei nicht erstellen: {}", e.getMessage());
        }
    }

    /**
     * Lädt die Musik-Bibliothekskonfiguration.
     * @return JsonObject mit Playlist-Konfigurationen
     */
    public JsonObject loadMusicLibraryConfig() {
        // Versuche die Konfigurationsdatei zu laden
        JsonObject config = tryLoadConfig();

        // Wenn keine Konfiguration gefunden wurde, erstelle die Standard-Ordner und Konfiguration
        if (config == null) {
            createDefaultFoldersAndConfig();
            config = tryLoadConfig(); // Versuche erneut zu laden
        }

        return config;
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

        // Überprüfe, ob der Pfad existiert
        File playlistDir = new File(playlistPath);
        if (!playlistDir.exists() || !playlistDir.isDirectory()) {
            logger.error("Playlist-Verzeichnis existiert nicht: {}", playlistDir.getAbsolutePath());

            // Versuche, den Ordner zu erstellen
            if (playlistDir.mkdirs()) {
                logger.info("Playlist-Verzeichnis wurde erstellt: {}", playlistDir.getAbsolutePath());
                createReadmeFile(playlistDir, playlistName);
            } else {
                logger.error("Konnte Playlist-Verzeichnis nicht erstellen: {}", playlistDir.getAbsolutePath());
            }

            return new ArrayList<>();
        }

        try {
            logger.info("Suche nach Audiodateien in: {}", playlistDir.getAbsolutePath());
            List<String> files = Files.walk(Paths.get(playlistPath))
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(this::isSupportedAudioFile)
                    .collect(Collectors.toList());

            logger.info("Gefunden: {} Audiodateien in Playlist {}", files.size(), playlistName);

            // Wenn keine Audiodateien gefunden wurden, gib einen Hinweis aus
            if (files.isEmpty()) {
                logger.warn("Keine Audiodateien in Playlist '{}' gefunden. "
                                + "Bitte füge MP3, WAV oder FLAC-Dateien zum Ordner '{}' hinzu.",
                        playlistName, playlistDir.getAbsolutePath());
            }

            return files;
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