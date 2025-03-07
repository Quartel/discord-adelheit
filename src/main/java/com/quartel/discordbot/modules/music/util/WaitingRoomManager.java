package com.quartel.discordbot.modules.music.util;

import com.quartel.discordbot.config.Config;
import com.quartel.discordbot.modules.music.player.GuildMusicManager;
import com.quartel.discordbot.modules.music.player.PlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
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
public class WaitingRoomManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitingRoomManager.class);
    private static WaitingRoomManager INSTANCE;

    // Map, die für jeden Server speichert, ob der Warteraum-Modus aktiv ist
    private final Map<Long, Boolean> activeWaitingRooms = new HashMap<>();

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

        // Starte einen Timer, der alle 30 Sekunden prüft, ob der Bot den Warteraum verlassen sollte
        scheduler.scheduleAtFixedRate(this::checkWaitingRooms, 30, 30, TimeUnit.SECONDS);

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
     * Setzt die JDA-Instanz.
     *
     * @param jda Die JDA-Instanz
     */
    public void setJDA(JDA jda) {
        this.jda = jda;
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
        if (Boolean.TRUE.equals(activeWaitingRooms.get(guildId))) {
            LOGGER.info("Warteraum für Server {} ist bereits aktiviert", guild.getName());
            return true;
        }

        // Hole die Channel-ID aus der Konfiguration
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

            // Verbinde mit dem Warteraum
            AudioManager audioManager = guild.getAudioManager();

            // Falls der Bot bereits in einem anderen Kanal ist, trenne die Verbindung
            if (audioManager.isConnected() && audioManager.getConnectedChannel().getIdLong() != channelId) {
                audioManager.closeAudioConnection();
            }

            // Mit dem Warteraum verbinden
            audioManager.openAudioConnection(waitingRoom);

            // Playlist abspielen
            List<String> playlistFiles = musicLibraryManager.findAudioFilesInPlaylist(playlistName);
            if (playlistFiles.isEmpty()) {
                LOGGER.error("Keine Audiodateien in der Playlist '{}' gefunden", playlistName);
                return false;
            }

            GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);

            // Warteschlange leeren und aktuelle Wiedergabe stoppen
            musicManager.getTrackScheduler().clearQueue();

            // Alle Dateien der Playlist zur Warteschlange hinzufügen
            for (String file : playlistFiles) {
                PlayerManager.getInstance().loadAndPlay(guild, file);
            }

            // Aktiviere den Wiederholungsmodus für die Playlist
            musicManager.getTrackScheduler().setRepeating(true);

            // Setze den Warteraum-Status auf aktiv
            activeWaitingRooms.put(guildId, true);

            // Aktualisiere die Aktivitätszeit
            updateActivity(guildId);

            LOGGER.info("Warteraum für Server {} aktiviert mit Playlist '{}'", guild.getName(), playlistName);
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
        if (!Boolean.TRUE.equals(activeWaitingRooms.get(guildId))) {
            LOGGER.info("Warteraum für Server {} ist nicht aktiviert", guild.getName());
            return true;
        }

        try {
            // Hole die GuildMusicManager-Instanz
            GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);

            // Setze den Wiederholungsmodus zurück
            musicManager.getTrackScheduler().setRepeating(false);

            // Warteschlange leeren und aktuelle Wiedergabe stoppen
            musicManager.getTrackScheduler().clearQueue();

            // Audioverbindung trennen
            AudioManager audioManager = guild.getAudioManager();
            if (audioManager.isConnected()) {
                audioManager.closeAudioConnection();
            }

            // Warteraum-Status auf inaktiv setzen
            activeWaitingRooms.put(guildId, false);

            LOGGER.info("Warteraum für Server {} deaktiviert", guild.getName());
            return true;

        } catch (Exception e) {
            LOGGER.error("Fehler beim Deaktivieren des Warteraums für Server {}", guild.getName(), e);
            return false;
        }
    }

    /**
     * Prüft, ob der Warteraum-Modus für den angegebenen Server aktiviert ist.
     *
     * @param guildId Die ID der Guild
     * @return true, wenn der Warteraum aktiviert ist, sonst false
     */
    public boolean isWaitingRoomActive(long guildId) {
        return Boolean.TRUE.equals(activeWaitingRooms.get(guildId));
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
     * Überprüft alle aktiven Warteräume und deaktiviert sie, wenn bestimmte Bedingungen erfüllt sind:
     * - Keine menschlichen Mitglieder im Warteraum
     * - Der Timeout wurde überschritten
     */
    private void checkWaitingRooms() {
        if (jda == null) {
            return;
        }

        try {
            for (Guild guild : jda.getGuilds()) {
                long guildId = guild.getIdLong();

                // Überspringen, wenn der Warteraum nicht aktiv ist
                if (!Boolean.TRUE.equals(activeWaitingRooms.get(guildId))) {
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

                    // Prüfe, ob der Bot überhaupt im Warteraum ist
                    Member selfMember = guild.getSelfMember();
                    GuildVoiceState selfVoiceState = selfMember.getVoiceState();

                    if (selfVoiceState == null || !selfVoiceState.inAudioChannel() ||
                            selfVoiceState.getChannel().getIdLong() != channelId) {
                        continue;
                    }

                    // Zähle menschliche Mitglieder im Warteraum
                    long humanMembersCount = waitingRoom.getMembers().stream()
                            .filter(member -> !member.getUser().isBot())
                            .count();

                    // Wenn keine menschlichen Mitglieder da sind, prüfe auf Timeout
                    if (humanMembersCount == 0) {
                        long currentTime = System.currentTimeMillis();
                        long lastActivity = lastActivityTimes.getOrDefault(guildId, currentTime);
                        int timeout = Integer.parseInt(Config.getProperty("warteraum.auto_leave_timeout", "60"));

                        // Wenn der Timeout überschritten wurde, deaktiviere den Warteraum
                        if ((currentTime - lastActivity) / 1000 >= timeout) {
                            LOGGER.info("Warteraum für Server {} wird automatisch deaktiviert (keine User anwesend)",
                                    guild.getName());
                            deactivateWaitingRoom(guild);
                        }
                    } else {
                        // Wenn menschliche Mitglieder da sind, aktualisiere die Aktivitätszeit
                        updateActivity(guildId);
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
     * Bereinigt Ressourcen beim Herunterfahren.
     */
    public void shutdown() {
        scheduler.shutdown();

        // Deaktiviere alle aktiven Warteräume
        for (Map.Entry<Long, Boolean> entry : activeWaitingRooms.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue()) && jda != null) {
                Guild guild = jda.getGuildById(entry.getKey());
                if (guild != null) {
                    deactivateWaitingRoom(guild);
                }
            }
        }

        LOGGER.info("WaitingRoomManager heruntergefahren");
    }
}