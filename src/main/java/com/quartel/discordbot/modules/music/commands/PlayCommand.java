package com.quartel.discordbot.modules.music.commands;

import com.quartel.discordbot.modules.music.player.PlayerManager;
import com.quartel.discordbot.modules.music.util.MusicUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diese Klasse implementiert den /play Command, der Musik abspielt oder zur Warteschlange hinzufügt.
 */
public class PlayCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayCommand.class);

    /**
     * Definiert die Slash-Command-Daten für den /play Befehl.
     *
     * @return Die CommandData für den /play Befehl
     */
    public static CommandData getCommandData() {
        return Commands.slash("play", "Spielt Musik ab oder fügt sie zur Warteschlange hinzu")
                .addOptions(
                        new OptionData(OptionType.STRING, "song", "Ein Lied-URL oder Suchbegriff", true)
                );
    }

    /**
     * Behandelt den /play Slash-Command.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public static void handle(SlashCommandInteractionEvent event) {
        LOGGER.debug("PlayCommand ausgeführt von: {}", event.getUser().getName());

        // Prüfe, ob der Benutzer in einem Sprachkanal ist und der Bot verbinden kann
        if (!MusicUtil.isInSameVoiceChannel(event)) {
            return;
        }

        // Mit dem Sprachkanal des Benutzers verbinden, falls noch nicht verbunden
        if (!MusicUtil.connectToUserVoiceChannel(event)) {
            return;
        }

        // Extrahiere den Song-Parameter aus dem Befehl
        String song = event.getOption("song").getAsString();

        // Wenn es keine vollständige URL ist, behandle es als YouTube-Suche
        if (!isUrl(song)) {
            song = "ytsearch:" + song;
            LOGGER.debug("Suche nach: {}", song);
        }

        // Lade und spiele den Track
        PlayerManager.getInstance().loadAndPlay(event, song);
    }

    /**
     * Überprüft, ob ein String eine URL ist.
     *
     * @param url Der zu überprüfende String
     * @return true, wenn der String eine URL ist, sonst false
     */
    private static boolean isUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
}