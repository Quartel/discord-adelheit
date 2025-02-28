package com.quartel.discordbot.modules.music.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Diese Klasse verwaltet die Warteschlange der Musik-Tracks und
 * behandelt Ereignisse, die während der Wiedergabe auftreten.
 */
public class TrackScheduler extends AudioEventAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackScheduler.class);

    private final AudioPlayer player;
    private final Queue<AudioTrack> queue;
    private boolean repeating = false;

    /**
     * Erstellt einen neuen TrackScheduler.
     *
     * @param player Der AudioPlayer, für den dieser Scheduler zuständig ist
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedList<>();
    }

    /**
     * Fügt einen Track zur Warteschlange hinzu.
     * Wenn nichts gespielt wird, wird der Track sofort abgespielt.
     *
     * @param track Der hinzuzufügende AudioTrack
     * @return true, wenn der Track sofort abgespielt wird, false wenn er in die Warteschlange eingefügt wurde
     */
    public boolean queue(AudioTrack track) {
        // Wenn der Player gerade nichts abspielt, spielen wir den Track sofort
        if (!player.startTrack(track, true)) {
            // Ansonsten fügen wir ihn der Warteschlange hinzu
            queue.offer(track);
            return false;
        }
        return true;
    }

    /**
     * Spielt den nächsten Track in der Warteschlange ab.
     *
     * @param skipCurrent Ob der aktuelle Track übersprungen werden soll
     */
    public void nextTrack(boolean skipCurrent) {
        if (skipCurrent) {
            player.stopTrack();
        }

        // Nimmt den nächsten Track aus der Warteschlange und spielt ihn ab
        AudioTrack track = queue.poll();
        if (track != null) {
            LOGGER.info("Spiele nächsten Track: {}", track.getInfo().title);
            player.startTrack(track, false);
        } else {
            LOGGER.info("Keine weiteren Tracks in der Warteschlange.");
        }
    }

    /**
     * Leert die Warteschlange und stoppt die aktuelle Wiedergabe.
     */
    public void clearQueue() {
        queue.clear();
        player.stopTrack();
        LOGGER.info("Warteschlange geleert und Wiedergabe gestoppt.");
    }

    /**
     * Gibt die aktuelle Warteschlange zurück.
     *
     * @return Eine Liste aller Tracks in der Warteschlange
     */
    public List<AudioTrack> getQueue() {
        return new LinkedList<>(queue);
    }

    /**
     * Schaltet den Wiederholungsmodus ein oder aus.
     *
     * @param repeating true, um Wiederholung zu aktivieren, false um sie zu deaktivieren
     */
    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
        LOGGER.info("Wiederholungsmodus: {}", repeating ? "aktiviert" : "deaktiviert");
    }

    /**
     * Prüft, ob der Wiederholungsmodus aktiviert ist.
     *
     * @return true, wenn der Wiederholungsmodus aktiviert ist, sonst false
     */
    public boolean isRepeating() {
        return repeating;
    }

    /**
     * Wird aufgerufen, wenn ein Track endet.
     * Wenn der Track normal zu Ende ging, wird der nächste Track in der Warteschlange abgespielt.
     */
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        LOGGER.debug("Track beendet: {} - Grund: {}", track.getInfo().title, endReason);

        // Wenn der Track beendet wurde, weil er zu Ende war (nicht wegen eines Fehlers oder manuellen Stopps)
        if (endReason.mayStartNext) {
            if (repeating) {
                // Im Wiederholungsmodus spielen wir den gleichen Track nochmal
                LOGGER.info("Wiederhole Track: {}", track.getInfo().title);
                player.startTrack(track.makeClone(), false);
            } else {
                // Sonst spielen wir den nächsten Track
                nextTrack(false);
            }
        }
    }

    /**
     * Wird aufgerufen, wenn ein Fehler während der Wiedergabe auftritt.
     */
    public void onTrackException(AudioPlayer player, AudioTrack track, Exception exception) {
        LOGGER.error("Fehler bei der Wiedergabe von {}: {}", track.getInfo().title, exception.getMessage(), exception);
    }

    /**
     * Wird aufgerufen, wenn ein Track stecken bleibt.
     */
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        LOGGER.warn("Track hängt: {} ({}ms)", track.getInfo().title, thresholdMs);
        nextTrack(true);
    }

    /**
     * Wird aufgerufen, wenn ein Track nicht gestartet werden kann.
     * Diese Methode ist zusätzlich und nicht Teil des AudioEventAdapter.
     */
    public void onTrackError(AudioPlayer player, AudioTrack track, Exception exception) {
        LOGGER.error("Fehler beim Starten des Tracks: {}", track.getInfo().title, exception);
        nextTrack(true);
    }
}