package com.quartel.discordbot.modules.ticket.util;

import com.quartel.discordbot.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Verwaltet die Erstellung und Verwaltung von Tickets.
 */
public class TicketManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketManager.class);
    private static final Random RANDOM = new Random();

    // Map zum Speichern aktiver Tickets (Kanal-ID -> Ersteller-ID)
    private static final Map<Long, Long> activeTickets = new HashMap<>();

    /**
     * Erstellt ein Modal zur Eingabe von Ticket-Informationen.
     *
     * @param event Das ButtonInteractionEvent
     */
    public static void createTicket(ButtonInteractionEvent event) {
        // Erstelle ein Modal (Popup-Formular) für weitere Ticket-Informationen
        TextInput descriptionInput = TextInput.create("ticket_description", "Beschreibe dein Anliegen", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Bitte beschreibe dein Problem oder deine Frage ausführlich...")
                .setMinLength(10)
                .setMaxLength(1000)
                .setRequired(true)
                .build();

        Modal modal = Modal.create("ticket_modal", "Ticket erstellen")
                .addActionRow(descriptionInput)
                .build();

        // Zeige das Modal dem Benutzer an
        event.replyModal(modal).queue();
    }

    /**
     * Erstellt einen Ticket-Kanal.
     *
     * @param guild Die Guild, in der das Ticket erstellt werden soll
     * @param user Der Benutzer, der das Ticket erstellt
     * @param description Die Beschreibung des Tickets
     */
    public static void createTicketChannel(Guild guild, User user, String description) {
        if (guild == null || user == null) {
            LOGGER.error("Guild oder User ist null - Ticket kann nicht erstellt werden");
            return;
        }

        // Prüfe, ob der Benutzer bereits ein Ticket hat
        if (hasActiveTicket(user.getIdLong())) {
            LOGGER.info("User {} hat bereits ein aktives Ticket", user.getName());
            // Idealerweise würden wir hier dem Benutzer eine Benachrichtigung senden,
            // aber dazu benötigen wir eine Hook für Antworten, die wir nicht mehr haben
            return;
        }

        // Generiere eine zufällige Ticket-Nummer
        int ticketNumber = RANDOM.nextInt(10000);

        // Erstelle den Ticket-Kanal-Namen
        String channelName = "ticket-" + user.getName().toLowerCase() + "-" + ticketNumber;
        // Entferne ungültige Zeichen aus dem Kanalnamen (Discord erlaubt nur Buchstaben, Zahlen, Bindestriche und Unterstriche)
        channelName = channelName.replaceAll("[^a-zA-Z0-9-_]", "");

        // Hole die Ticket-Kategorie aus der Konfiguration
        String categoryIdStr = Config.getProperty("ticket.category_id");
        if (categoryIdStr == null || categoryIdStr.isEmpty()) {
            LOGGER.error("Ticket-Kategorie-ID ist nicht konfiguriert!");
            return;
        }

        try {
            long categoryId = Long.parseLong(categoryIdStr);
            Category category = guild.getCategoryById(categoryId);

            if (category == null) {
                LOGGER.error("Kategorie mit ID {} wurde nicht gefunden", categoryId);
                return;
            }

            // Hole die Staff-Rolle aus der Konfiguration
            String staffRoleIdStr = Config.getProperty("ticket.staff_role_id");
            Role staffRole = null;

            if (staffRoleIdStr != null && !staffRoleIdStr.isEmpty()) {
                try {
                    long staffRoleId = Long.parseLong(staffRoleIdStr);
                    staffRole = guild.getRoleById(staffRoleId);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Ungültige Staff-Rollen-ID in der Konfiguration", e);
                }
            }

            // Standardmäßige Berechtigungen vorbereiten
            // Jeder kann den Kanal nicht sehen, außer der Bot selbst
            EnumSet<Permission> denyPermissions = EnumSet.of(
                    Permission.VIEW_CHANNEL,
                    Permission.MESSAGE_SEND
            );

            // Erstelle finale Kopien für Lambda-Ausdrücke
            final User finalUser = user;
            final String finalDescription = description;
            final Role finalStaffRole = staffRole;
            final String finalChannelName = channelName;
            final int finalTicketNumber = ticketNumber;

            // Erstelle einen neuen Textkanal für das Ticket
            category.createTextChannel(channelName)
                    .addMemberPermissionOverride(finalUser.getIdLong(),
                            EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                    .addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, denyPermissions)
                    .queue(channel -> {
                        // Speichere das Ticket als aktiv
                        activeTickets.put(channel.getIdLong(), finalUser.getIdLong());

                        // Füge Berechtigungen für die Staff-Rolle hinzu, falls vorhanden
                        if (finalStaffRole != null) {
                            channel.upsertPermissionOverride(finalStaffRole)
                                    .grant(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                                    .queue();
                        }

                        // Erstelle ein Embed für das neue Ticket
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTitle("Neues Ticket")
                                .setColor(Color.GREEN)
                                .setDescription("Ticket von " + finalUser.getAsMention() + " wurde erstellt.")
                                .addField("Beschreibung", finalDescription, false)
                                .setFooter("Ticket-ID: " + finalTicketNumber, finalUser.getEffectiveAvatarUrl());

                        // Füge Buttons für das Ticket-Management hinzu
                        channel.sendMessageEmbeds(embedBuilder.build())
                                .addActionRow(
                                        Button.danger("close_ticket", "Ticket schließen")
                                )
                                .queue(success -> {
                                    LOGGER.info("Ticket-Kanal {} erfolgreich erstellt für User {}",
                                            channel.getName(), finalUser.getName());

                                    // Benachrichtige das Team, falls konfiguriert
                                    boolean mentionStaff = Boolean.parseBoolean(
                                            Config.getProperty("ticket.mention_staff", "true"));

                                    if (mentionStaff && finalStaffRole != null) {
                                        channel.sendMessage(finalStaffRole.getAsMention() +
                                                " Ein neues Ticket wurde erstellt!").queue();
                                    }
                                });
                    }, error -> {
                        LOGGER.error("Fehler beim Erstellen des Ticket-Kanals: {}", error.getMessage(), error);
                    });

        } catch (NumberFormatException e) {
            LOGGER.error("Ungültige Kategorie-ID in der Konfiguration", e);
        }
    }

    /**
     * Schließt ein Ticket und erstellt ein Transkript.
     *
     * @param guild Die Guild
     * @param channel Der Ticket-Kanal
     * @param closer Der Benutzer, der das Ticket schließt
     * @return CompletableFuture, das abgeschlossen wird, wenn das Ticket geschlossen wurde
     */
    public static CompletableFuture<Void> closeTicket(Guild guild, TextChannel channel, User closer) {
        if (!isTicketChannel(channel.getIdLong())) {
            LOGGER.warn("Versuch, einen Nicht-Ticket-Kanal {} zu schließen", channel.getName());
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        // Erstelle finale Kopien für Lambda-Ausdrücke
        final Guild finalGuild = guild;
        final TextChannel finalChannel = channel;
        final User finalCloser = closer;

        // Erstelle zunächst ein Transkript (dies werden wir im nächsten Schritt implementieren)
        TicketTranscriptManager.createTranscript(finalChannel)
                .thenAccept(transcript -> {
                    // Entferne das Ticket aus der aktiven Ticket-Liste
                    long creatorId = activeTickets.remove(finalChannel.getIdLong());

                    // Suche den Ersteller des Tickets
                    User creator = finalGuild.getJDA().getUserById(creatorId);
                    String creatorMention = creator != null ? creator.getAsMention() : "Unbekannter Benutzer";

                    // Finale Kopien für weitere Lambda-Ausdrücke
                    final User finalCreator = creator;
                    final String finalCreatorMention = creatorMention;

                    // Hole den Log-Kanal aus der Konfiguration
                    String logChannelIdStr = Config.getProperty("ticket.log_channel_id");

                    if (logChannelIdStr != null && !logChannelIdStr.isEmpty()) {
                        try {
                            long logChannelId = Long.parseLong(logChannelIdStr);
                            TextChannel logChannel = finalGuild.getTextChannelById(logChannelId);

                            if (logChannel != null) {
                                // Finale Kopie des logChannel
                                final TextChannel finalLogChannel = logChannel;

                                // Erstelle ein Embed für das Log
                                EmbedBuilder logEmbed = new EmbedBuilder()
                                        .setTitle("Ticket geschlossen")
                                        .setColor(Color.RED)
                                        .setDescription("Ein Ticket wurde geschlossen.")
                                        .addField("Ticket", "#" + finalChannel.getName(), true)
                                        .addField("Ersteller", finalCreatorMention, true)
                                        .addField("Geschlossen von", finalCloser.getAsMention(), true)
                                        .setFooter("Ticket-Transkript", finalGuild.getIconUrl());

                                // Sende das Transkript
                                finalLogChannel.sendMessageEmbeds(logEmbed.build())
                                        .addFiles(FileUpload.fromData(transcript))
                                        .queue(success -> {
                                            LOGGER.info("Ticket-Transkript für Kanal {} erfolgreich erstellt",
                                                    finalChannel.getName());

                                            // Lösche den Ticket-Kanal nach 5 Sekunden
                                            finalChannel.sendMessage("Dieses Ticket wird in 5 Sekunden gelöscht...").queue();

                                            try {
                                                Thread.sleep(5000);
                                                finalChannel.delete().queue(
                                                        deleteSuccess -> {
                                                            LOGGER.info("Ticket-Kanal {} erfolgreich gelöscht",
                                                                    finalChannel.getName());
                                                            future.complete(null);
                                                        },
                                                        deleteError -> {
                                                            LOGGER.error("Fehler beim Löschen des Ticket-Kanals: {}",
                                                                    deleteError.getMessage(), deleteError);
                                                            future.completeExceptionally(deleteError);
                                                        }
                                                );
                                            } catch (InterruptedException e) {
                                                LOGGER.error("Thread-Unterbrechung beim Löschen des Ticket-Kanals", e);
                                                future.completeExceptionally(e);
                                            }
                                        }, error -> {
                                            LOGGER.error("Fehler beim Senden des Ticket-Transkripts: {}",
                                                    error.getMessage(), error);
                                            future.completeExceptionally(error);
                                        });
                            } else {
                                LOGGER.warn("Log-Kanal mit ID {} nicht gefunden", logChannelId);
                                future.completeExceptionally(new IllegalStateException("Log-Kanal nicht gefunden"));
                            }
                        } catch (NumberFormatException e) {
                            LOGGER.error("Ungültige Log-Kanal-ID in der Konfiguration", e);
                            future.completeExceptionally(e);
                        }
                    } else {
                        LOGGER.warn("Keine Log-Kanal-ID konfiguriert");
                        // Lösche den Kanal trotzdem
                        finalChannel.delete().queue(
                                success -> {
                                    LOGGER.info("Ticket-Kanal {} erfolgreich gelöscht (kein Transkript)",
                                            finalChannel.getName());
                                    future.complete(null);
                                },
                                error -> {
                                    LOGGER.error("Fehler beim Löschen des Ticket-Kanals: {}",
                                            error.getMessage(), error);
                                    future.completeExceptionally(error);
                                }
                        );
                    }
                })
                .exceptionally(e -> {
                    LOGGER.error("Fehler beim Erstellen des Ticket-Transkripts", e);
                    future.completeExceptionally(e);
                    return null;
                });

        return future;
    }

    /**
     * Prüft, ob ein Benutzer bereits ein aktives Ticket hat.
     *
     * @param userId Die ID des Benutzers
     * @return true, wenn der Benutzer ein aktives Ticket hat
     */
    public static boolean hasActiveTicket(long userId) {
        return activeTickets.containsValue(userId);
    }

    /**
     * Prüft, ob ein Kanal ein Ticket-Kanal ist.
     *
     * @param channelId Die ID des Kanals
     * @return true, wenn der Kanal ein Ticket-Kanal ist
     */
    public static boolean isTicketChannel(long channelId) {
        return activeTickets.containsKey(channelId);
    }
}