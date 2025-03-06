package com.quartel.discordbot.core.listeners;

import com.quartel.discordbot.modules.music.commands.*;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener für Ereignisse, wenn der Bot einem neuen Server beitritt.
 * Registriert automatisch die Slash-Commands auf dem neuen Server.
 */
public class GuildJoinListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildJoinListener.class);

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        LOGGER.info("Bot ist Server beigetreten: {}", event.getGuild().getName());

        // Zuerst bestehende Befehle löschen, um Duplikate zu vermeiden
        event.getGuild().updateCommands().queue(
                success -> {
                    LOGGER.info("Bestehende Befehle für neuen Server {} gelöscht", event.getGuild().getName());

                    // Erstelle Liste der zu registrierenden Befehle
                    List<CommandData> commands = new ArrayList<>();
                    commands.add(PlayCommand.getCommandData());
                    commands.add(SkipCommand.getCommandData());
                    commands.add(StopCommand.getCommandData());
                    commands.add(QueueCommand.getCommandData());
                    commands.add(NowPlayingCommand.getCommandData());
                    commands.add(VolumeCommand.getCommandData());
                    commands.add(PauseResumeCommand.getPauseCommandData());
                    commands.add(PauseResumeCommand.getResumeCommandData());

                    // Jetzt neue Befehle registrieren
                    event.getGuild().updateCommands().addCommands(commands).queue(
                            registerSuccess -> LOGGER.info("Musik-Befehle erfolgreich für neuen Server {} registriert", event.getGuild().getName()),
                            registerError -> LOGGER.error("Fehler beim Registrieren der Musik-Befehle für neuen Server {}: {}",
                                    event.getGuild().getName(), registerError.getMessage())
                    );
                },
                error -> LOGGER.error("Fehler beim Löschen bestehender Befehle für neuen Server {}: {}",
                        event.getGuild().getName(), error.getMessage())
        );
    }
}