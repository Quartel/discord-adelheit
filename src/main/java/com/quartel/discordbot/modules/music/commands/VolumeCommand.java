package com.quartel.discordbot.modules.music.commands;

import com.quartel.discordbot.modules.music.player.GuildMusicManager;
import com.quartel.discordbot.modules.music.player.PlayerManager;
import com.quartel.discordbot.modules.music.util.MusicUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diese Klasse implementiert den /volume Command, der die Lautstärke der Musikwiedergabe ändert.
 */
public class VolumeCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(VolumeCommand.class);

    /**
     * Definiert die Slash-Command-Daten für den /volume Befehl.
     *
     * @return Die CommandData für den /volume Befehl
     */
    public static CommandData getCommandData() {
        return Commands.slash("volume", "Ändert die Lautstärke der Musikwiedergabe")
                .addOptions(
                        new OptionData(OptionType.INTEGER, "level", "Lautstärke (0-100)", true)
                                .setMinValue(0)
                                .setMaxValue(100)
                );
    }

    /**
     * Behandelt den /volume Slash-Command.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    public static void handle(SlashCommandInteractionEvent event) {
        LOGGER.debug("VolumeCommand ausgeführt von: {}", event.getUser().getName());

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Dieser Befehl kann nur auf einem Server verwendet werden.").setEphemeral(true).queue();
            return;
        }

        // Prüfe, ob der Benutzer im gleichen Sprachkanal ist
        if (!MusicUtil.isInSameVoiceChannel(event)) {
            return;
        }

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
        int oldVolume = musicManager.getVolume();

        // Setze die neue Lautstärke
        int newVolume = event.getOption("level").getAsInt();
        musicManager.setVolume(newVolume);

        // Aktualisiere die Aktivitätszeit
        musicManager.updateActivity();

        // Erstelle eine Nachricht je nach Lautstärkeänderung
        String volumeIcon;
        if (newVolume == 0) {
            volumeIcon = "🔇"; // Stumm
        } else if (newVolume < 30) {
            volumeIcon = "🔈"; // Leise
        } else if (newVolume < 70) {
            volumeIcon = "🔉"; // Mittel
        } else {
            volumeIcon = "🔊"; // Laut
        }

        // Informiere den Benutzer über die Änderung
        String message = String.format("%s Lautstärke geändert: **%d%%** → **%d%%**",
                volumeIcon, oldVolume, newVolume);

        event.reply(message).queue();
    }
}