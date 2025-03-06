package com.quartel.discordbot.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hilfsprogramm zum Bereinigen aller Befehle - sowohl global als auch guild-spezifisch.
 * Kann zum Zurücksetzen des Bots verwendet werden, wenn Probleme mit doppelten Befehlen auftreten.
 */
public class CommandCleaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandCleaner.class);

    /**
     * Löscht alle Befehle (global und guild-spezifisch) und gibt die Kontrolle zurück, sobald die Löschung abgeschlossen ist.
     *
     * @param jda Die JDA-Instanz
     * @param callback Ein Callback, der ausgeführt wird, wenn der Löschvorgang abgeschlossen ist
     */
    public static void cleanAllCommands(JDA jda, Runnable callback) {
        LOGGER.info("Starte vollständige Bereinigung aller Befehle...");

        // Zuerst alle globalen Befehle löschen
        LOGGER.info("Lösche alle globalen Befehle...");
        jda.updateCommands().queue(
                globalSuccess -> {
                    LOGGER.info("Globale Befehle erfolgreich gelöscht");

                    // Zähler für die Anzahl der Guilds
                    final int[] remainingGuilds = {jda.getGuilds().size()};

                    if (remainingGuilds[0] == 0) {
                        LOGGER.info("Keine Guilds gefunden. Bereinigung abgeschlossen.");
                        callback.run();
                        return;
                    }

                    // Dann alle guild-spezifischen Befehle löschen
                    for (Guild guild : jda.getGuilds()) {
                        LOGGER.info("Lösche Befehle für Guild: {}", guild.getName());
                        guild.updateCommands().queue(
                                guildSuccess -> {
                                    LOGGER.info("Befehle für Guild {} erfolgreich gelöscht", guild.getName());

                                    // Reduziere den Zähler
                                    remainingGuilds[0]--;

                                    // Wenn alle Guilds bearbeitet wurden, führe den Callback aus
                                    if (remainingGuilds[0] == 0) {
                                        LOGGER.info("Alle Befehle vollständig bereinigt.");
                                        callback.run();
                                    }
                                },
                                guildError -> {
                                    LOGGER.error("Fehler beim Löschen der Befehle für Guild {}: {}",
                                            guild.getName(), guildError.getMessage());

                                    // Reduziere den Zähler trotz Fehler
                                    remainingGuilds[0]--;

                                    // Wenn alle Guilds bearbeitet wurden, führe den Callback aus
                                    if (remainingGuilds[0] == 0) {
                                        LOGGER.info("Bereinigung abgeschlossen (mit einigen Fehlern).");
                                        callback.run();
                                    }
                                }
                        );
                    }
                },
                globalError -> {
                    LOGGER.error("Fehler beim Löschen globaler Befehle: {}", globalError.getMessage());
                    callback.run(); // Führe den Callback trotz Fehler aus
                }
        );
    }
}