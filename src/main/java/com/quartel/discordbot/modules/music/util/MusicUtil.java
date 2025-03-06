package com.quartel.discordbot.modules.music.util;

import com.quartel.discordbot.modules.music.player.GuildMusicManager;
import com.quartel.discordbot.modules.music.player.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Hilfsklasse für Musik-bezogene Funktionen.
 */
public class MusicUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MusicUtil.class);

    /**
     * Überprüft, ob der Bot mit dem Benutzer im gleichen Sprachkanal ist.
     *
     * @param event Das SlashCommandInteractionEvent
     * @return true, wenn der Bot mit dem Benutzer im gleichen Sprachkanal ist, sonst false
     */
    public static boolean isInSameVoiceChannel(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return false;

        Member member = event.getMember();
        if (member == null) return false;

        GuildVoiceState memberVoiceState = member.getVoiceState();
        if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
            event.reply("Du musst in einem Sprachkanal sein, um diesen Befehl zu verwenden.").setEphemeral(true).queue();
            return false;
        }

        Member selfMember = guild.getSelfMember();
        GuildVoiceState selfVoiceState = selfMember.getVoiceState();

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel()) {
            // Der Bot ist in keinem Sprachkanal
            return true;
        }

        // Prüfe, ob der Bot und der Benutzer im gleichen Kanal sind
        if (memberVoiceState.getChannel() != selfVoiceState.getChannel()) {
            event.reply("Du musst im gleichen Sprachkanal wie der Bot sein, um diesen Befehl zu verwenden.").setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    /**
     * Verbindet den Bot mit dem Sprachkanal des Benutzers.
     *
     * @param event Das SlashCommandInteractionEvent
     * @return true, wenn die Verbindung erfolgreich war, sonst false
     */
    public static boolean connectToUserVoiceChannel(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return false;

        Member member = event.getMember();
        if (member == null) return false;

        GuildVoiceState memberVoiceState = member.getVoiceState();
        if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
            event.reply("Du musst in einem Sprachkanal sein, um diesen Befehl zu verwenden.").setEphemeral(true).queue();
            return false;
        }

        AudioManager audioManager = guild.getAudioManager();
        VoiceChannel voiceChannel = memberVoiceState.getChannel().asVoiceChannel();

        try {
            audioManager.openAudioConnection(voiceChannel);
            LOGGER.info("Verbunden mit Sprachkanal: {} ({})", voiceChannel.getName(), guild.getName());
            return true;
        } catch (Exception e) {
            LOGGER.error("Fehler beim Verbinden mit dem Sprachkanal", e);
            event.reply("Ich konnte nicht mit dem Sprachkanal verbinden: " + e.getMessage()).setEphemeral(true).queue();
            return false;
        }
    }

    /**
     * Formatiert die Dauer eines Tracks in ein lesbares Format (mm:ss oder hh:mm:ss).
     *
     * @param milliseconds Die Dauer in Millisekunden
     * @return Ein formatierter String der Dauer
     */
    public static String formatDuration(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Erstellt ein Embed für den aktuell spielenden Track.
     *
     * @param guild Die Guild, für die das Embed erstellt werden soll
     * @return Ein MessageEmbed mit Informationen zum aktuellen Track, oder null, wenn nichts spielt
     */
    public static MessageEmbed createNowPlayingEmbed(Guild guild) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
        AudioTrack currentTrack = musicManager.getAudioPlayer().getPlayingTrack();

        if (currentTrack == null) {
            return new EmbedBuilder()
                    .setTitle("Keine Wiedergabe")
                    .setDescription("Derzeit wird nichts abgespielt.")
                    .setColor(Color.RED)
                    .build();
        }

        AudioTrackInfo info = currentTrack.getInfo();
        long position = currentTrack.getPosition();
        long duration = currentTrack.getDuration();

        // Fortschrittsbalken erstellen
        int totalBars = 20; // Länge des Balkens
        int progressBars = (int) ((double) position / duration * totalBars);
        StringBuilder progressBar = new StringBuilder();

        for (int i = 0; i < totalBars; i++) {
            if (i == progressBars) {
                progressBar.append("🔘"); // Aktuelle Position
            } else if (i < progressBars) {
                progressBar.append("▬"); // Fortschritt
            } else {
                progressBar.append("▭"); // Verbleibend
            }
        }

        return new EmbedBuilder()
                .setTitle("Aktuelle Wiedergabe")
                .setDescription("**[" + info.title + "](" + info.uri + ")**")
                .addField("Dauer", formatDuration(position) + " / " + formatDuration(duration), true)
                .addField("Kanal/Künstler", info.author, true)
                .addField("Lautstärke", musicManager.getVolume() + "%", true)
                .addField("Fortschritt", progressBar.toString(), false)
                .setColor(Color.GREEN)
                .setThumbnail(getMusicThumbnail(info.uri))
                .setFooter("Angefordert von " + guild.getSelfMember().getUser().getName(), guild.getSelfMember().getUser().getAvatarUrl())
                .build();
    }

    /**
     * Erstellt ein Embed für die aktuelle Warteschlange.
     *
     * @param guild Die Guild, für die das Embed erstellt werden soll
     * @return Ein MessageEmbed mit Informationen zur Warteschlange
     */
    public static MessageEmbed createQueueEmbed(Guild guild) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
        AudioTrack currentTrack = musicManager.getAudioPlayer().getPlayingTrack();
        List<AudioTrack> queue = musicManager.getTrackScheduler().getQueue();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Musik-Warteschlange")
                .setColor(Color.BLUE);

        if (currentTrack == null && queue.isEmpty()) {
            return embedBuilder
                    .setDescription("Die Warteschlange ist leer.")
                    .build();
        }

        StringBuilder description = new StringBuilder();

        // Aktueller Track
        if (currentTrack != null) {
            AudioTrackInfo info = currentTrack.getInfo();
            description.append("**Aktuell:** [")
                    .append(info.title)
                    .append("](")
                    .append(info.uri)
                    .append(") `")
                    .append(formatDuration(currentTrack.getDuration()))
                    .append("`\n\n");
        }

        // Warteschlange (maximal 10 Einträge)
        if (!queue.isEmpty()) {
            description.append("**Warteschlange:**\n");
            int trackCount = Math.min(queue.size(), 10);

            for (int i = 0; i < trackCount; i++) {
                AudioTrack track = queue.get(i);
                AudioTrackInfo info = track.getInfo();

                description.append("`")
                        .append(i + 1)
                        .append(".` [")
                        .append(info.title)
                        .append("](")
                        .append(info.uri)
                        .append(") `")
                        .append(formatDuration(track.getDuration()))
                        .append("`\n");
            }

            // Wenn mehr als 10 Tracks in der Warteschlange sind
            if (queue.size() > 10) {
                description.append("*... und ")
                        .append(queue.size() - 10)
                        .append(" weitere Tracks*");
            }
        }

        // Berechnen der Gesamtdauer
        long totalDuration = queue.stream()
                .mapToLong(AudioTrack::getDuration)
                .sum();

        if (currentTrack != null) {
            totalDuration += currentTrack.getDuration() - currentTrack.getPosition();
        }

        // Embed fertigstellen
        embedBuilder.setDescription(description.toString());
        embedBuilder.addField("Tracks insgesamt", (currentTrack != null ? "1 + " : "") + queue.size(), true);
        embedBuilder.addField("Gesamtdauer", formatDuration(totalDuration), true);
        embedBuilder.addField("Lautstärke", musicManager.getVolume() + "%", true);

        return embedBuilder.build();
    }

    /**
     * Holt die Thumbnail-URL für eine Musikquelle.
     * Gibt standardmäßig ein generisches Musiksymbol zurück, da die direkte Thumbnail-Extraktion
     * für bestimmte Quellen nicht mehr unterstützt wird.
     *
     * @param sourceUrl Die URL der Musikquelle
     * @return Die URL eines Standard-Musiksymbols
     */
    private static String getMusicThumbnail(String sourceUrl) {
        // Verwende immer das Standard-Musiksymbol
        return "https://i.imgur.com/HQoJfpG.png";
    }
}