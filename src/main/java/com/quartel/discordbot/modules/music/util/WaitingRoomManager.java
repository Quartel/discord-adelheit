package com.quartel.discordbot.modules.music.util;

import com.quartel.discordbot.config.Config;
import com.quartel.discordbot.modules.music.player.GuildMusicManager;
import com.quartel.discordbot.modules.music.player.PlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Verwaltet den Warteraum-Modus des Bots.
 * Diese Klasse ist als Singleton implementiert.
 */
public class WaitingRoomManager extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitingRoomManager.class);
    private static WaitingRoomManager INSTANCE;

    // Zustände für den Warteraum-Modus
    public enum WaitingRoomState {
        INACTIVE,      // Warteraum-Modus ist vollständig deaktiviert
        MONITORING,    // Warteraum-Modus aktiv, aber Bot nicht verbunden (wartet auf User)
        CONNECTED      // Warteraum-Modus aktiv und Bot verbunden und spielt Musik
    }

    // Map, die für jeden Server den Warteraum-Zustand speichert
    private final Map<Long, WaitingRoomState> waitingRoomStates = new HashMap<>();

    // Map, die die verwendete Playlist für jeden Server speichert
    private final Map<Long, String> activePlaylists = new HashMap<>();

    // Map, die die letzte Aktivitätszeit für jeden Server speichert
    private final Map<Long, Long> lastActivityTimes = new HashMap<>();

    // Scheduler für regelmäßige Überprüfungen
    private final ScheduledExecutorService scheduler;

    // JDA-Instanz für den Zugriff auf Guilds und Channels
    private JDA jda;

    // MusicLibraryManager für den Zugriff auf Playlists
    private final MusicLibraryManager musicLibraryManager;

    /**
     * Privater Konstruktor für Singleton-Muster.
     */
    private WaitingRoomManager() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.musicLibraryManager = new MusicLibraryManager();

        // Starte einen Timer, der alle 15 Sekunden prüft
        scheduler.scheduleAtFixedRate(this::checkWaitingRooms, 15, 15, TimeUnit.SECONDS);

        LOGGER.info("WaitingRoomManager initialisiert");
    }

    /**
     * Gibt die einzige Instanz des WaitingRoomManagers zurück oder erstellt sie, falls nicht vorhanden.
     *
     * @return Die Instanz des WaitingRoomManagers
     */
    public static synchronized WaitingRoomManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WaitingRoomManager();
        }
        return INSTANCE;
    }

    /**
     * Setzt die JDA-Instanz und registriert den Event-Listener.
     *
     * @param jda Die JDA-Instanz
     */
    public void setJDA(JDA jda) {
        this.jda = jda;

        // Entferne zuerst diesen Listener, falls er bereits registriert ist
        jda.removeEventListener(this);

        // Registriere diesen Manager als Event-Listener für Voice-Events
        jda.addEventListener(this);
        LOGGER.debug("WaitingRoomManager als Event-Listener registriert");

        // Prüfe alle Guilds auf bereits aktivierte Warteräume
        // und stelle initialen Zustand wieder her
        scheduler.schedule(this::checkWaitingRooms, 5, TimeUnit.SECONDS);
    }

    /**
     * Prüft, ob der Warteraum für ein bestimmten Server konfiguriert ist.
     *
     * @param guild Die Guild, für die die Prüfung durchgeführt werden soll
     * @return true, wenn der Warteraum konfiguriert ist, sonst false
     */
    public boolean isWaitingRoomConfigured(Guild guild) {
        if (guild == null) {
            return false;
        }

        // Hole die Channel-ID aus der Konfiguration
        String channelIdStr = Config.getProperty("warteraum.channel_id");
        if (channelIdStr == null || channelIdStr.isEmpty() || "YOUR_CHANNEL_ID_HERE".equals(channelIdStr)) {
            return false;
        }

        try {
            long channelId = Long.parseLong(channelIdStr);
            VoiceChannel waitingRoom = guild.getVoiceChannelById(channelId);
            return waitingRoom != null;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Aktiviert den Warteraum-Modus für den angegebenen Server mit der angegebenen Playlist.
     *
     * @param guild Die Guild, für die der Warteraum aktiviert werden soll
     * @param playlistName Der Name der zu spielenden Playlist
     * @return true, wenn der Warteraum erfolgreich aktiviert wurde, sonst false
     */
    public boolean activateWaitingRoom(Guild guild, String playlistName) {
        if (guild == null) {
            LOGGER.error("Guild ist null beim Aktivieren des Warteraums");
            return false;
        }

        long guildId = guild.getIdLong();

        // Prüfe, ob der Warteraum bereits aktiviert ist
        WaitingRoomState currentState = waitingRoomStates.getOrDefault(guildId, WaitingRoomState.INACTIVE);
        if (currentState != WaitingRoomState.INACTIVE) {
            LOGGER.info("Warteraum für Server {} ist bereits aktiviert mit Status: {}",
                    guild.getName(), currentState);

            // Aktualisiere die Playlist, falls sie geändert wurde
            activePlaylists.put(guildId, playlistName);

            // Wenn der Bot nicht verbunden ist, prüfe, ob User im Channel sind
            if (currentState == WaitingRoomState.MONITORING) {
                connectIfUsersPresent(guild, playlistName);
            }

            return true;
        }

        // Prüfe die Gültigkeit der Channel-ID
        String channelIdStr = Config.getProperty("warteraum.channel_id");
        if (channelIdStr == null || channelIdStr.isEmpty() || "YOUR_CHANNEL_ID_HERE".equals(channelIdStr)) {
            LOGGER.error("Keine gültige Warteraum-Channel-ID in der Konfiguration gefunden");
            return false;
        }

        try {
            long channelId = Long.parseLong(channelIdStr);
            VoiceChannel waitingRoom = guild.getVoiceChannelById(channelId);

            if (waitingRoom == null) {
                LOGGER.error("Warteraum-Channel mit ID {} nicht gefunden", channelId);
                return false;
            }

            // Playlist überprüfen
            List<String> playlistFiles = musicLibraryManager.findAudioFilesInPlaylist(playlistName);
            if (playlistFiles.isEmpty()) {
                LOGGER.error("Keine Audiodateien in der Playlist '{}' gefunden", playlistName);
                return false;
            }

            // Speichere die aktive Playlist
            activePlaylists.put(guildId, playlistName);

            // Prüfe, ob User im Channel sind
            boolean usersPresent = countHumanMembersInChannel(waitingRoom) > 0;

            if (usersPresent) {
                // Verbinden und Musik abspielen, wenn User da sind
                if (connectToChannel(guild, waitingRoom)) {
                    playPlaylist(guild, playlistFiles);
                    waitingRoomStates.put(guildId, WaitingRoomState.CONNECTED);
                    updateActivity(guildId);
                    LOGGER.info("Warteraum für Server {} aktiviert mit Playlist '{}' und direkt verbunden",
                            guild.getName(), playlistName);
                } else {
                    // Verbindung fehlgeschlagen, aber trotzdem im Monitoring-Modus
                    waitingRoomStates.put(guildId, WaitingRoomState.MONITORING);
                    LOGGER.warn("Verbindung zum Warteraum-Channel fehlgeschlagen, starte im Monitoring-Modus");
                }
            } else {
                // Keine User im Channel, starte im Monitoring-Modus
                waitingRoomStates.put(guildId, WaitingRoomState.MONITORING);
                LOGGER.info("Warteraum für Server {} aktiviert im Monitoring-Modus (keine User im Channel)",
                        guild.getName());
            }

            return true;

        } catch (NumberFormatException e) {
            LOGGER.error("Ungültige Channel-ID in der Konfiguration", e);
            return false;
        } catch (Exception e) {
            LOGGER.error("Fehler beim Aktivieren des Warteraums für Server {}", guild.getName(), e);
            return false;
        }
    }

    /**
     * Deaktiviert den Warteraum-Modus für den angegebenen Server.
     *
     * @param guild Die Guild, für die der Warteraum deaktiviert werden soll
     * @return true, wenn der Warteraum erfolgreich deaktiviert wurde, sonst false
     */
    public boolean deactivateWaitingRoom(Guild guild) {
        if (guild == null) {
            LOGGER.error("Guild ist null beim Deaktivieren des Warteraums");
            return false;
        }

        long guildId = guild.getIdLong();

        // Prüfe, ob der Warteraum überhaupt aktiviert ist
        WaitingRoomState currentState = waitingRoomStates.getOrDefault(guildId, WaitingRoomState.INACTIVE);
        if (currentState == WaitingRoomState.INACTIVE) {
            LOGGER.info("Warteraum für Server {} ist nicht aktiviert", guild.getName());
            return true;
        }

        try {
            // Wenn verbunden, trennen und Wiedergabe stoppen
            if (currentState == WaitingRoomState.CONNECTED) {
                disconnectFromChannel(guild);
            }

            // Status auf inaktiv setzen
            waitingRoomStates.put(guildId, WaitingRoomState.INACTIVE);
            activePlaylists.remove(guildId);

            LOGGER.info("Warteraum für Server {} vollständig deaktiviert", guild.getName());
            return true;

        } catch (Exception e) {
            LOGGER.error("Fehler beim Deaktivieren des Warteraums für Server {}", guild.getName(), e);
            return false;
        }
    }

    /**
     * Verbindet mit dem Warteraum-Channel, wenn User anwesend sind.
     *
     * @param guild Die Guild
     * @param playlistName Der Name der zu spielenden Playlist
     * @return true, wenn verbunden wurde, false wenn nicht
     */
    private boolean connectIfUsersPresent(Guild guild, String playlistName) {
        if (guild == null) return false;

        try {
            String channelIdStr = Config.getProperty("warteraum.channel_id");
            long channelId = Long.parseLong(channelIdStr);
            VoiceChannel waitingRoom = guild.getVoiceChannelById(channelId);

            if (waitingRoom == null) {
                LOGGER.error("Warteraum-Channel nicht gefunden");
                return false;
            }

            // Prüfe, ob User im Channel sind
            boolean usersPresent = countHumanMembersInChannel(waitingRoom) > 0;

            if (usersPresent) {
                // Verbinden und Musik abspielen
                if (connectToChannel(guild, waitingRoom)) {
                    List<String> playlistFiles = musicLibraryManager.findAudioFilesInPlaylist(playlistName);
                    playPlaylist(guild, playlistFiles);
                    waitingRoomStates.put(guild.getIdLong(), WaitingRoomState.CONNECTED);
                    updateActivity(guild.getIdLong());
                    LOGGER.info("Mit Warteraum-Channel verbunden wegen Benutzeranwesenheit");
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            LOGGER.error("Fehler beim Prüfen auf Benutzeranwesenheit", e);
            return false;
        }
    }

    /**
     * Verbindet mit einem Sprachkanal.
     *
     * @param guild Die Guild
     * @param channel Der Sprachkanal
     * @return true, wenn erfolgreich verbunden, sonst false
     */
    private boolean connectToChannel(Guild guild, VoiceChannel channel) {
        try {
            AudioManager audioManager = guild.getAudioManager();

            // Falls der Bot bereits in einem anderen Kanal ist, trenne die Verbindung
            if (audioManager.isConnected() && audioManager.getConnectedChannel().getIdLong() != channel.getIdLong()) {
                audioManager.closeAudioConnection();
            }

            // Mit dem Channel verbinden
            audioManager.openAudioConnection(channel);
            LOGGER.info("Verbunden mit Channel: {} in Guild: {}", channel.getName(), guild.getName());
            return true;
        } catch (Exception e) {
            LOGGER.error("Fehler beim Verbinden mit dem Channel", e);
            return false;
        }
    }

    /**
     * Trennt die Verbindung vom Sprachkanal.
     *
     * @param guild Die Guild
     */
    private void disconnectFromChannel(Guild guild) {
        try {
            // Hole die GuildMusicManager-Instanz
            GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);

            // Warteschlange leeren und aktuelle Wiedergabe stoppen
            musicManager.getTrackScheduler().clearQueue();

            // Audioverbindung trennen
            AudioManager audioManager = guild.getAudioManager();
            if (audioManager.isConnected()) {
                audioManager.closeAudioConnection();
                LOGGER.info("Verbindung zum Sprachkanal in Guild {} getrennt", guild.getName());
            }
        } catch (Exception e) {
            LOGGER.error("Fehler beim Trennen vom Sprachkanal", e);
        }
    }

    /**
     * Spielt eine Playlist ab.
     *
     * @param guild Die Guild
     * @param playlistFiles Die Dateien der Playlist
     */
    private void playPlaylist(Guild guild, List<String> playlistFiles) {
        try {
            GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);

            // Warteschlange leeren und aktuelle Wiedergabe stoppen
            musicManager.getTrackScheduler().clearQueue();

            // Alle Dateien der Playlist zur Warteschlange hinzufügen
            for (String file : playlistFiles) {
                PlayerManager.getInstance().loadAndPlay(guild, file);
            }

            // Aktiviere den Wiederholungsmodus für die Playlist
            musicManager.getTrackScheduler().setRepeating(true);

            LOGGER.info("Playlist mit {} Tracks wird in Guild {} abgespielt",
                    playlistFiles.size(), guild.getName());
        } catch (Exception e) {
            LOGGER.error("Fehler beim Abspielen der Playlist", e);
        }
    }

    /**
     * Zählt die Anzahl der menschlichen Mitglieder in einem Sprachkanal.
     *
     * @param channel Der Sprachkanal
     * @return Die Anzahl der menschlichen Mitglieder
     */
    private long countHumanMembersInChannel(VoiceChannel channel) {
        if (channel == null) return 0;

        return channel.getMembers().stream()
                .filter(member -> !member.getUser().isBot())
                .count();
    }

    /**
     * Prüft, ob der Warteraum-Modus für den angegebenen Server aktiviert ist.
     *
     * @param guildId Die ID der Guild
     * @return true, wenn der Warteraum aktiviert ist (egal ob verbunden oder im Monitoring-Modus), sonst false
     */
    public boolean isWaitingRoomActive(long guildId) {
        WaitingRoomState state = waitingRoomStates.getOrDefault(guildId, WaitingRoomState.INACTIVE);
        return state != WaitingRoomState.INACTIVE;
    }

    /**
     * Gibt den aktuellen Zustand des Warteraums für den angegebenen Server zurück.
     *
     * @param guildId Die ID der Guild
     * @return Der Warteraum-Zustand
     */
    public WaitingRoomState getWaitingRoomState(long guildId) {
        return waitingRoomStates.getOrDefault(guildId, WaitingRoomState.INACTIVE);
    }

    /**
     * Aktualisiert die Aktivitätszeit für den angegebenen Server.
     *
     * @param guildId Die ID der Guild
     */
    public void updateActivity(long guildId) {
        lastActivityTimes.put(guildId, System.currentTimeMillis());
    }

    /**
     * Überprüft alle aktiven Warteräume.
     * Verbindet mit Channels, in denen User sind.
     * Trennt Verbindungen zu leeren Channels (geht in Monitoring-Modus).
     */
    private void checkWaitingRooms() {
        if (jda == null) {
            return;
        }

        try {
            for (Guild guild : jda.getGuilds()) {
                long guildId = guild.getIdLong();
                WaitingRoomState currentState = waitingRoomStates.getOrDefault(guildId, WaitingRoomState.INACTIVE);

                // Überspringen, wenn der Warteraum nicht aktiv ist
                if (currentState == WaitingRoomState.INACTIVE) {
                    continue;
                }

                // Hole Channel-ID aus der Konfiguration
                String channelIdStr = Config.getProperty("warteraum.channel_id");
                if (channelIdStr == null || channelIdStr.isEmpty()) {
                    continue;
                }

                try {
                    long channelId = Long.parseLong(channelIdStr);
                    VoiceChannel waitingRoom = guild.getVoiceChannelById(channelId);

                    if (waitingRoom == null) {
                        continue;
                    }

                    // Zähle menschliche Mitglieder im Warteraum
                    long humanMembersCount = countHumanMembersInChannel(waitingRoom);

                    if (currentState == WaitingRoomState.CONNECTED) {
                        // Bot ist verbunden - prüfen ob er trennen soll
                        if (humanMembersCount == 0) {
                            // Prüfe auf Timeout
                            long currentTime = System.currentTimeMillis();
                            long lastActivity = lastActivityTimes.getOrDefault(guildId, currentTime);
                            int timeout = Integer.parseInt(Config.getProperty("warteraum.auto_leave_timeout", "60"));

                            // Wenn der Timeout überschritten wurde, trenne Verbindung (aber bleibe im Monitoring-Modus)
                            if ((currentTime - lastActivity) / 1000 >= timeout) {
                                LOGGER.info("Keine User im Warteraum für Server {} - Trenne Verbindung und wechsle in Monitoring-Modus",
                                        guild.getName());

                                disconnectFromChannel(guild);
                                waitingRoomStates.put(guildId, WaitingRoomState.MONITORING);
                            }
                        } else {
                            // User sind anwesend, aktualisiere Aktivitätszeit
                            updateActivity(guildId);
                        }
                    } else if (currentState == WaitingRoomState.MONITORING) {
                        // Bot ist im Monitoring-Modus - prüfen ob er verbinden soll
                        if (humanMembersCount > 0) {
                            LOGGER.info("User im Warteraum für Server {} entdeckt - Verbinde und starte Musik",
                                    guild.getName());

                            String playlistName = activePlaylists.getOrDefault(guildId,
                                    Config.getProperty("warteraum.default_playlist", "chill"));

                            connectIfUsersPresent(guild, playlistName);
                        }
                    }

                } catch (NumberFormatException e) {
                    LOGGER.error("Ungültige Channel-ID in der Konfiguration", e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei der Überprüfung der Warteräume", e);
        }
    }

    /**
     * Event-Handler für Voice-Update-Events.
     * Reagiert, wenn ein Benutzer einem Sprachkanal beitritt.
     */
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        // Wir sind nur an Join-Events interessiert (channelJoined != null, channelLeft != null oder null)
        if (event.getChannelJoined() == null) {
            return;
        }

        try {
            Guild guild = event.getGuild();
            long guildId = guild.getIdLong();

            // Überspringen, wenn Warteraum nicht im Monitoring-Modus
            WaitingRoomState currentState = waitingRoomStates.getOrDefault(guildId, WaitingRoomState.INACTIVE);
            if (currentState != WaitingRoomState.MONITORING) {
                return;
            }

            // Hole Channel-ID aus der Konfiguration
            String channelIdStr = Config.getProperty("warteraum.channel_id");
            if (channelIdStr == null || channelIdStr.isEmpty()) {
                return;
            }

            long channelId = Long.parseLong(channelIdStr);

            // Überprüfe, ob der User dem Warteraum beigetreten ist
            if (event.getChannelJoined().getIdLong() == channelId) {
                // Vermeide Reaktion auf Bots
                if (event.getMember().getUser().isBot()) {
                    return;
                }

                LOGGER.info("User {} ist dem Warteraum in Guild {} beigetreten - Verbinde",
                        event.getMember().getEffectiveName(), guild.getName());

                String playlistName = activePlaylists.getOrDefault(guildId,
                        Config.getProperty("warteraum.default_playlist", "chill"));

                // Mit einiger Verzögerung verbinden, um sicherzustellen, dass der Join vollständig ist
                scheduler.schedule(() -> connectIfUsersPresent(guild, playlistName), 1, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            LOGGER.error("Fehler beim Verarbeiten des Voice-Update-Events", e);
        }
    }

    /**
     * Bereinigt Ressourcen beim Herunterfahren.
     */
    public void shutdown() {
        scheduler.shutdown();

        // Deaktiviere alle aktiven Warteräume
        for (Map.Entry<Long, WaitingRoomState> entry : waitingRoomStates.entrySet()) {
            if (entry.getValue() != WaitingRoomState.INACTIVE && jda != null) {
                Guild guild = jda.getGuildById(entry.getKey());
                if (guild != null) {
                    disconnectFromChannel(guild);
                }
            }
        }

        LOGGER.info("WaitingRoomManager heruntergefahren");
    }
}