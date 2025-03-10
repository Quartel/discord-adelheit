package com.quartel.discordbot.modules.ticket.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manager für die Erstellung von Ticket-Transkripten.
 */
public class TicketTranscriptManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketTranscriptManager.class);
    private static final int MESSAGE_HISTORY_LIMIT = 1000; // Maximale Anzahl von Nachrichten im Transkript
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    /**
     * Erstellt ein Transkript für einen Ticket-Kanal.
     *
     * @param channel Der Ticket-Kanal
     * @return CompletableFuture mit der Transkript-Datei
     */
    public static CompletableFuture<File> createTranscript(TextChannel channel) {
        CompletableFuture<File> future = new CompletableFuture<>();

        // Erstelle einen temporären Dateinamen für das Transkript
        String fileName = "ticket-" + channel.getName() + "-" +
                new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".txt";

        // Entferne ungültige Zeichen aus dem Dateinamen
        fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        File transcriptFile = new File(fileName);

        // Hole die letzten Nachrichten aus dem Kanal
        channel.getHistory().retrievePast(MESSAGE_HISTORY_LIMIT).queue(messages -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(transcriptFile))) {
                // Schreibe Header-Informationen
                writer.write("==========================================\n");
                writer.write("Ticket-Transkript: " + channel.getName() + "\n");
                writer.write("Erstellt am: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()) + "\n");
                writer.write("Server: " + channel.getGuild().getName() + "\n");
                writer.write("==========================================\n\n");

                // Schreibe Nachrichten in umgekehrter Reihenfolge (älteste zuerst)
                List<Message> messageList = new ArrayList<>(messages);
                Collections.reverse(messageList);

                for (Message msg : messageList) {
                    writer.write("[" + msg.getTimeCreated().format(TIME_FORMATTER) + "] " +
                            msg.getAuthor().getName() + " (" + msg.getAuthor().getId() + "): " +
                            msg.getContentDisplay() + "\n");

                    // Falls die Nachricht Anhänge hat, diese auch dokumentieren
                    if (!msg.getAttachments().isEmpty()) {
                        writer.write("Anhänge:\n");
                        for (Message.Attachment attachment : msg.getAttachments()) {
                            writer.write("- " + attachment.getFileName() + " (" +
                                    attachment.getSize() + " Bytes): " + attachment.getUrl() + "\n");
                        }
                    }

                    // Leerzeile nach jeder Nachricht für bessere Lesbarkeit
                    writer.write("\n");
                }

                writer.write("==========================================\n");
                writer.write("Ende des Transkripts\n");
                writer.write("==========================================\n");

                LOGGER.info("Transkript für Kanal {} erfolgreich erstellt: {}",
                        channel.getName(), transcriptFile.getAbsolutePath());

                future.complete(transcriptFile);

            } catch (IOException e) {
                LOGGER.error("Fehler beim Erstellen des Transkripts für Kanal {}: {}",
                        channel.getName(), e.getMessage(), e);
                future.completeExceptionally(e);
            }
        }, error -> {
            LOGGER.error("Fehler beim Abrufen der Nachrichtenhistorie für Kanal {}: {}",
                    channel.getName(), error.getMessage(), error);
            future.completeExceptionally(error);
        });

        return future;
    }
}