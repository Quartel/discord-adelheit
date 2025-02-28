package com.quartel.discordbot.core.listeners;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.quartel.discordbot.Bot;
import com.quartel.discordbot.modules.music.util.MusicLibraryManager;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener für Slash-Commands.
 * Leitet die Slash-Commands an die entsprechenden Handler weiter.
 */
public class SlashCommandListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommandListener.class);

    private final Bot bot;

    /**
     * Erstellt einen neuen SlashCommandListener mit Referenz auf die Bot-Instanz.
     *
     * @param bot Die Bot-Instanz
     */
    public SlashCommandListener(Bot bot) {
        this.bot = bot;
        LOGGER.debug("SlashCommandListener initialisiert");
    }

    /**
     * Wird aufgerufen, wenn ein Slash-Command ausgeführt wird.
     *
     * @param event Das SlashCommandInteractionEvent
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        LOGGER.debug("Slash-Command erhalten: {} von {}", commandName, event.getUser().getName());

        // Commands direkt an ihre Handler in den Modulen delegieren
        // Die Module registrieren ihre eigenen Listener für ihre Commands
    }

    /**
     * Wird aufgerufen, wenn Auto-Completion für einen Slash-Command angefordert wird.
     *
     * @param event Das CommandAutoCompleteInteractionEvent
     */
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        // Spezielle Auto-Completion-Logik für verschiedene Commands
        if (event.getName().equals("play")) {
            handlePlayCommandAutoComplete(event);
        }
    }

    /**
     * Behandelt Auto-Completion für den Play-Befehl.
     *
     * @param event Das CommandAutoCompleteInteractionEvent
     */
    private void handlePlayCommandAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        MusicLibraryManager musicLibraryManager = new MusicLibraryManager();
        JsonObject config = musicLibraryManager.loadMusicLibraryConfig();

        if (config == null || !config.has("playlists")) {
            event.getInteraction().replyChoices().queue();
            return;
        }

        String focusedOption = event.getFocusedOption().getName();
        String userInput = event.getFocusedOption().getValue().toLowerCase();

        JsonArray playlists = config.getAsJsonArray("playlists");
        List<Command.Choice> choices = new ArrayList<>();

        for (int i = 0; i < playlists.size(); i++) {
            JsonObject playlist = playlists.get(i).getAsJsonObject();
            String playlistName = playlist.get("name").getAsString();
            String description = playlist.get("description").getAsString();

            // Für Playlist- und Preview-Optionen
            if ((focusedOption.equals("playlist") || focusedOption.equals("preview"))
                    && playlistName.toLowerCase().contains(userInput)) {
                choices.add(new Command.Choice(
                        playlistName + " - " + description,
                        playlistName
                ));
            }

            // Begrenzen auf 25 Vorschläge (Discord-Limit)
            if (choices.size() >= 25) break;
        }

        event.getInteraction().replyChoices(choices).queue();
    }
}