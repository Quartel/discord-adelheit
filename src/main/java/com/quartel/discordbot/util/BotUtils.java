package com.quartel.discordbot.util;

import com.quartel.discordbot.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Stellt Hilfsmethoden für den Bot bereit.
 */
public class BotUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotUtils.class);

    /**
     * Standard-Farbe für erfolgreiche Operationen.
     */
    public static final Color COLOR_SUCCESS = new Color(46, 204, 113);

    /**
     * Standard-Farbe für Fehler.
     */
    public static final Color COLOR_ERROR = new Color(231, 76, 60);

    /**
     * Standard-Farbe für Informationen.
     */
    public static final Color COLOR_INFO = new Color(52, 152, 219);

    /**
     * Prüft, ob eine Nachricht mit dem Bot-Präfix beginnt.
     *
     * @param content Der Inhalt der Nachricht
     * @return true, wenn die Nachricht mit dem Präfix beginnt, sonst false
     */
    public static boolean startsWithPrefix(String content) {
        return content.startsWith(Config.getPrefix());
    }

    /**
     * Extrahiert den Befehl aus einer Nachricht.
     *
     * @param content Der Inhalt der Nachricht
     * @return Der Befehl ohne Präfix
     */
    public static String getCommand(String content) {
        String prefix = Config.getPrefix();
        if (content.startsWith(prefix)) {
            String withoutPrefix = content.substring(prefix.length()).trim();

            // Wenn es Leerzeichen gibt, nehme nur den Teil vor dem ersten Leerzeichen
            int spaceIndex = withoutPrefix.indexOf(' ');
            if (spaceIndex > 0) {
                return withoutPrefix.substring(0, spaceIndex).toLowerCase();
            }

            return withoutPrefix.toLowerCase();
        }
        return "";
    }

    /**
     * Extrahiert die Argumente aus einer Nachricht.
     *
     * @param content Der Inhalt der Nachricht
     * @return Ein Array der Argumente (nach dem Befehl)
     */
    public static String[] getArgs(String content) {
        String prefix = Config.getPrefix();
        if (content.startsWith(prefix)) {
            String withoutPrefix = content.substring(prefix.length()).trim();

            // Wenn es Leerzeichen gibt, nehme alle Teile nach dem ersten Leerzeichen
            int spaceIndex = withoutPrefix.indexOf(' ');
            if (spaceIndex > 0 && spaceIndex < withoutPrefix.length() - 1) {
                String argsString = withoutPrefix.substring(spaceIndex + 1).trim();
                return argsString.split("\\s+");
            }
        }
        return new String[0];
    }

    /**
     * Erstellt ein Embed für eine erfolgreiche Operation.
     *
     * @param title   Der Titel des Embeds
     * @param message Die Nachricht
     * @return Das erstellte MessageEmbed
     */
    public static MessageEmbed createSuccessEmbed(String title, String message) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(message)
                .setColor(COLOR_SUCCESS)
                .setTimestamp(Instant.now())
                .build();
    }

    /**
     * Erstellt ein Embed für einen Fehler.
     *
     * @param title   Der Titel des Embeds
     * @param message Die Fehlermeldung
     * @return Das erstellte MessageEmbed
     */
    public static MessageEmbed createErrorEmbed(String title, String message) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(message)
                .setColor(COLOR_ERROR)
                .setTimestamp(Instant.now())
                .build();
    }

    /**
     * Erstellt ein Embed für eine Information.
     *
     * @param title   Der Titel des Embeds
     * @param message Die Informationsnachricht
     * @return Das erstellte MessageEmbed
     */
    public static MessageEmbed createInfoEmbed(String title, String message) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(message)
                .setColor(COLOR_INFO)
                .setTimestamp(Instant.now())
                .build();
    }

    /**
     * Sendet eine Nachricht und löscht sie nach einer bestimmten Zeit.
     *
     * @param message  Die Nachricht, die geantwortet werden soll
     * @param content  Der Inhalt der neuen Nachricht
     * @param duration Die Dauer, nach der die Nachricht gelöscht werden soll
     */
    public static void sendTemporaryMessage(Message message, String content, Duration duration) {
        message.reply(content).queue(sentMessage -> {
            sentMessage.delete().queueAfter(duration.toMillis(), TimeUnit.MILLISECONDS);
        });
    }

    /**
     * Formatiert eine Dauer in ein lesbares Format.
     *
     * @param duration Die Dauer als Duration-Objekt
     * @return Ein formatierter String
     */
    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Berechnet die Ping-Zeit (Antwortzeit) in Millisekunden.
     *
     * @param message Die Nachricht, für die die Ping-Zeit berechnet werden soll
     * @return Die Ping-Zeit in Millisekunden
     */
    public static long calculatePing(Message message) {
        return System.currentTimeMillis() - message.getTimeCreated().toInstant().toEpochMilli();
    }
}