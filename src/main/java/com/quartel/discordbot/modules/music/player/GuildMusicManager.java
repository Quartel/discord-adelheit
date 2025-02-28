package com.quartel.discordbot.modules.music.player;

import com.quartel.discordbot.config.Config;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diese Klasse verwaltet den AudioPlayer und TrackScheduler für einen bestimmten Discord-Server (Guild).
 * Für jeden Server gibt es eine eigene Instanz.
 */
public class GuildMusicManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildMusicManager.class);

    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final AudioPlayerSendHandler sendHandler;

    // Timer für automatischen Timeout (in Sekunden)
    private int disconnectTimeout;
    private long lastActivityTime;

    /**
     * Erstellt einen neuen GuildMusicManager mit dem gegebenen PlayerManager.
     *
     * @param playerManager Der AudioPlayerManager, der die Audio-Ressourcen verwaltet
     */
    public GuildMusicManager(AudioPlayerManager playerManager) {
        this.audioPlayer = playerManager.createPlayer();
        this.trackScheduler = new TrackScheduler(audioPlayer);
        this.sendHandler = new AudioPlayerSendHandler(audioPlayer);

        // Standard-Timeout aus der Konfiguration laden (Fallback: 60 Sekunden)
        try {
            this.disconnectTimeout = Integer.parseInt(Config.getProperty("music.timeout", "60"));
        } catch (NumberFormatException e) {
            LOGGER.warn("Ungültiger Wert für music.timeout in config.properties, verwende Standardwert: 60");
            this.disconnectTimeout = 60;
        }

        // Standard-Lautstärke setzen
        int defaultVolume = 50;
        try {
            defaultVolume = Integer.parseInt(Config.getProperty("music.volume.default", "50"));
        } catch (NumberFormatException e) {
            LOGGER.warn("Ungültiger Wert für music.volume.default in config.properties, verwende Standardwert: 50");
        }
        audioPlayer.setVolume(defaultVolume);

        // AudioPlayer mit dem TrackScheduler verbinden
        audioPlayer.addListener(trackScheduler);

        // Aktivitätszeit initialisieren
        updateActivity();

        LOGGER.debug("GuildMusicManager erstellt. Timeout: {}s, Standard-Lautstärke: {}", disconnectTimeout, defaultVolume);
    }

    /**
     * Gibt den AudioPlayer zurück.
     *
     * @return Der AudioPlayer dieser Guild
     */
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    /**
     * Gibt den TrackScheduler zurück.
     *
     * @return Der TrackScheduler dieser Guild
     */
    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    /**
     * Gibt den AudioPlayerSendHandler zurück, der für die Kommunikation mit JDA benötigt wird.
     *
     * @return Der AudioPlayerSendHandler dieser Guild
     */
    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }

    /**
     * Aktualisiert die Zeit der letzten Aktivität.
     */
    public void updateActivity() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Prüft, ob der Bot inaktiv ist und getrennt werden sollte.
     *
     * @return true, wenn der Bot länger als der konfigurierte Timeout inaktiv ist
     */
    public boolean shouldDisconnect() {
        if (disconnectTimeout <= 0) {
            return false; // Timeout deaktiviert
        }

        // Wenn ein Track abgespielt wird, nicht trennen
        if (audioPlayer.getPlayingTrack() != null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long inactiveTime = (currentTime - lastActivityTime) / 1000; // in Sekunden

        return inactiveTime >= disconnectTimeout;
    }

    /**
     * Setzt die Lautstärke des Players.
     *
     * @param volume Die neue Lautstärke (0-100)
     */
    public void setVolume(int volume) {
        // Lautstärke auf gültigen Bereich begrenzen
        int boundedVolume = Math.max(0, Math.min(100, volume));
        audioPlayer.setVolume(boundedVolume);
        LOGGER.info("Lautstärke auf {} gesetzt", boundedVolume);
    }

    /**
     * Gibt die aktuelle Lautstärke zurück.
     *
     * @return Die aktuelle Lautstärke (0-100)
     */
    public int getVolume() {
        return audioPlayer.getVolume();
    }
}