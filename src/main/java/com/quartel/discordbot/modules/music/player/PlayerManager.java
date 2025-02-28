package com.quartel.discordbot.modules.music.player;

import com.quartel.discordbot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Diese Klasse verwaltet zentral alle Musik-Sessions für alle Server.
 * Sie ist als Singleton implementiert, um einen globalen Zugriffspunkt zu bieten.
 */
public class PlayerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerManager.class);
    private static PlayerManager INSTANCE;

    // AudioPlayerManager für die gesamte Anwendung
    private final AudioPlayerManager audioPlayerManager;

    // JDA-Instanz für den Zugriff auf die Guilds
    private JDA jda;

    // Map, die jedem Server seinen eigenen GuildMusicManager zuordnet
    private final Map<Long, GuildMusicManager> musicManagers;

    // Für regelmäßige Überprüfung auf inaktive Verbindungen
    private final ScheduledExecutorService scheduler;

    /**
     * Privater Konstruktor für Singleton-Muster.
     */
    private PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Registriere alle Quellen, von denen Musik geladen werden kann
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);

        // Starte einen Timer, der inaktive Verbindungen überprüft
        scheduler.scheduleAtFixedRate(this::checkInactiveConnections, 1, 1, TimeUnit.MINUTES);

        LOGGER.info("PlayerManager initialisiert");
    }

    /**
     * Gibt die einzige Instanz des PlayerManagers zurück oder erstellt sie, falls sie nicht existiert.
     *
     * @return Die Instanz des PlayerManagers
     */
    public static synchronized PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    /**
     * Gibt den GuildMusicManager für einen bestimmten Server zurück oder erstellt ihn, falls er nicht existiert.
     *
     * @param guild Der Server, für den der Manager abgerufen werden soll
     * @return Der GuildMusicManager für den Server
     */
    public synchronized GuildMusicManager getMusicManager(Guild guild) {
        long guildId = guild.getIdLong();

        // Prüfe, ob bereits ein Manager für diesen Server existiert
        GuildMusicManager musicManager = musicManagers.get(guildId);

        // Wenn nicht, erstelle einen neuen
        if (musicManager == null) {
            musicManager = new GuildMusicManager(audioPlayerManager);
            musicManagers.put(guildId, musicManager);

            // Verbinde den AudioSendHandler mit dem Guild
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
            LOGGER.debug("Neuer GuildMusicManager für Server {} erstellt", guild.getName());
        }

        return musicManager;
    }

    /**
     * Lädt und spielt eine Audioquelle auf einem Server.
     *
     * @param event     Der SlashCommandInteractionEvent, der den Befehl ausgelöst hat
     * @param trackUrl  Die URL oder Suchbegriff für den abzuspielenden Track
     */
    public void loadAndPlay(SlashCommandInteractionEvent event, String trackUrl) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Dieser Befehl kann nur auf einem Server verwendet werden.").setEphemeral(true).queue();
            return;
        }

        TextChannel channel = event.getChannel().asTextChannel();
        GuildMusicManager musicManager = getMusicManager(guild);
        musicManager.updateActivity();

        // Deferred Reply senden, da Laden länger dauern kann
        event.deferReply().queue();

        // Lade den Track mit LavaPlayer
        audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                // Ein einzelner Track wurde geladen
                LOGGER.info("Track geladen: {} - {}", track.getInfo().title, track.getInfo().uri);

                boolean playingNow = musicManager.getTrackScheduler().queue(track);
                String message = playingNow
                        ? "🎵 Spiele jetzt: **" + track.getInfo().title + "**"
                        : "🎵 Zur Warteschlange hinzugefügt: **" + track.getInfo().title + "**";

                event.getHook().sendMessage(message).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                // Eine Playlist wurde geladen
                LOGGER.info("Playlist geladen: {} mit {} Tracks", playlist.getName(), playlist.getTracks().size());

                // Bei Suche wird der erste Track verwendet
                if (playlist.isSearchResult()) {
                    AudioTrack firstTrack = playlist.getTracks().get(0);
                    boolean playingNow = musicManager.getTrackScheduler().queue(firstTrack);

                    String message = playingNow
                            ? "🎵 Spiele jetzt: **" + firstTrack.getInfo().title + "**"
                            : "🎵 Zur Warteschlange hinzugefügt: **" + firstTrack.getInfo().title + "**";

                    event.getHook().sendMessage(message).queue();
                    return;
                }

                // Bei einer tatsächlichen Playlist, alle Tracks zur Warteschlange hinzufügen
                int addedTracks = 0;
                boolean firstTrackStarted = false;

                for (AudioTrack track : playlist.getTracks()) {
                    if (addedTracks == 0) {
                        firstTrackStarted = musicManager.getTrackScheduler().queue(track);
                    } else {
                        musicManager.getTrackScheduler().queue(track);
                    }
                    addedTracks++;
                }

                String message = String.format("🎵 **%d** Tracks aus Playlist **%s** zur Warteschlange hinzugefügt",
                        addedTracks, playlist.getName());

                event.getHook().sendMessage(message).queue();
            }

            @Override
            public void noMatches() {
                // Keine Treffer gefunden
                LOGGER.info("Keine Treffer gefunden für: {}", trackUrl);
                event.getHook().sendMessage("❌ Ich konnte nichts für `" + trackUrl + "` finden.").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                // Laden fehlgeschlagen
                LOGGER.error("Fehler beim Laden des Tracks: {}", exception.getMessage(), exception);
                event.getHook().sendMessage("❌ Fehler beim Laden: " + exception.getMessage()).queue();
            }
        });
    }

    /**
     * Überprüft alle Server auf inaktive Verbindungen und trennt sie gegebenenfalls.
     */
    private void checkInactiveConnections() {
        LOGGER.debug("Überprüfe inaktive Verbindungen...");

        musicManagers.forEach((guildId, manager) -> {
            Guild guild = null;
            try {
                // Versuche, die Guild-Instanz zu bekommen
                for (Guild g : PlayerManager.getInstance().getConnectedGuilds()) {
                    if (g.getIdLong() == guildId) {
                        guild = g;
                        break;
                    }
                }

                if (guild != null && manager.shouldDisconnect()) {
                    LOGGER.info("Trenne inaktive Verbindung für Server: {}", guild.getName());
                    guild.getAudioManager().closeAudioConnection();
                }
            } catch (Exception e) {
                LOGGER.error("Fehler bei der Überprüfung inaktiver Verbindungen für Guild ID {}", guildId, e);
            }
        });
    }

    /**
     * Gibt alle verbundenen Server zurück.
     *
     * @return Eine Sammlung aller Server, mit denen der Bot verbunden ist
     */
    public Iterable<Guild> getConnectedGuilds() {
        if (jda == null) {
            jda = Bot.getInstance().getJda();
        }
        return jda.getGuilds();
    }

    /**
     * Setzt die JDA-Instanz.
     *
     * @param jda Die zu setzende JDA-Instanz
     */
    public void setJDA(JDA jda) {
        this.jda = jda;
    }

    /**
     * Bereinigt Ressourcen beim Herunterfahren.
     */
    public void shutdown() {
        scheduler.shutdown();
        audioPlayerManager.shutdown();
        LOGGER.info("PlayerManager heruntergefahren");
    }
}