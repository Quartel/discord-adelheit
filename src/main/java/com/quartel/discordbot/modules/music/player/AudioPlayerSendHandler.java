package com.quartel.discordbot.modules.music.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

/**
 * Diese Klasse verbindet den LavaPlayer mit dem JDA Audio-System.
 * Sie wandelt die Audio-Daten vom LavaPlayer in ein Format um, das JDA verarbeiten kann.
 */
public class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;

    /**
     * Erstellt einen neuen AudioPlayerSendHandler.
     *
     * @param audioPlayer Der AudioPlayer, dessen Daten gesendet werden sollen
     */
    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(1024); // Größe des Puffers für Audiodaten
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    /**
     * Überprüft, ob der AudioPlayer Audiodaten zum Senden hat.
     *
     * @return true, wenn Audiodaten verfügbar sind, sonst false
     */
    @Override
    public boolean canProvide() {
        // Versucht, den nächsten Frame zu schreiben und prüft, ob Daten verfügbar sind
        return audioPlayer.provide(frame);
    }

    /**
     * Gibt die Audiodaten als ByteBuffer zurück.
     *
     * @return ByteBuffer mit Audiodaten
     */
    @Override
    public ByteBuffer provide20MsAudio() {
        // Puffer auf die Leseposition zurücksetzen (wichtig für JDA)
        buffer.flip();
        return buffer;
    }

    /**
     * Gibt an, ob die Audiodaten in Opus-Format codiert sind.
     *
     * @return true, da wir JDA anweisen möchten, die Daten nicht erneut zu codieren
     */
    @Override
    public boolean isOpus() {
        // Wir geben true zurück, damit JDA die Daten nicht erneut codiert
        return true;
    }
}