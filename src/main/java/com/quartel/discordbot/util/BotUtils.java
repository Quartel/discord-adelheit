package com.quartel.discordbot.util;

import com.quartel.discordbot.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

/**
 * Nützliche Hilfsfunktionen für den Bot.
 * Stellt verschiedene Utility-Methoden bereit, die im gesamten Bot verwendet werden können.
 */
public class BotUtils {

    /**
     * Gibt das konfigurierte Präfix für Bot-Befehle zurück.
     *
     * @return Das Präfix aus der Konfiguration oder "!" als Standardwert
     */
    public static String getPrefix() {
        return Config.getString("bot.prefix", "!");
    }

    /**
     * Sendet eine Nachricht an einen Kanal und löscht sie nach einer bestimmten Zeit.
     * Nützlich für temporäre Benachrichtigungen oder Statusmeldungen.
     *
     * @param channel Der Kanal, an den die Nachricht gesendet werden soll
     * @param message Die zu sendende Nachricht
     * @param duration Die Dauer in Sekunden, nach der die Nachricht gelöscht wird
     */
    public static void sendTemporaryMessage(MessageChannel channel, String message, int duration) {
        channel.sendMessage(message).queue(msg -> {
            msg.delete().queueAfter(duration, TimeUnit.SECONDS);
        });
    }

    /**
     * Erstellt ein Standard-Embed für Erfolgsmeldungen.
     *
     * @param title Der Titel des Embeds
     * @param description Die Beschreibung des Embeds
     * @return Das erstellte MessageEmbed
     */
    public static MessageEmbed createSuccessEmbed(String title, String description) {
        return new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle(title)
                .setDescription(description)
                .setFooter("Adelheit Bot")
                .build();
    }

    /**
     * Erstellt ein Standard-Embed für Fehlermeldungen.
     *
     * @param title Der Titel des Embeds
     * @param description Die Beschreibung des Embeds
     * @return Das erstellte MessageEmbed
     */
    public static MessageEmbed createErrorEmbed(String title, String description) {
        return new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(title)
                .setDescription(description)
                .setFooter("Adelheit Bot")
                .build();
    }

    /**
     * Erstellt ein Standard-Embed für Informationen.
     *
     * @param title Der Titel des Embeds
     * @param description Die Beschreibung des Embeds
     * @return Das erstellte MessageEmbed
     */
    public static MessageEmbed createInfoEmbed(String title, String description) {
        return new EmbedBuilder()
                .setColor(Color.BLUE)
                .setTitle(title)
                .setDescription(description)
                .setFooter("Adelheit Bot")
                .build();
    }

    /**
     * Formatiert eine Zeitdauer in Millisekunden in ein lesbares Format (mm:ss).
     *
     * @param milliseconds Die Zeitdauer in Millisekunden
     * @return Die formatierte Zeit im Format mm:ss
     */
    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }
}