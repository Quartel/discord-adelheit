package com.quartel.discordbot.core.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Verwaltet Berechtigungen für Bot-Befehle auf Discord-Servern.
 * Ermöglicht die Konfiguration von Berechtigungen basierend auf Rollen.
 */
public class PermissionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionManager.class);

    // Standardmäßige Berechtigungsstufen
    public enum PermissionLevel {
        EVERYONE,      // Jeder Benutzer
        DJ_ROLE,       // Benutzer mit DJ-Rolle
        ADMIN_ROLE,    // Administratoren
        SERVER_OWNER   // Nur Serverbesitzer
    }

    // Konfigurierbare Berechtigungen pro Server
    private final Map<Long, Map<String, PermissionLevel>> serverPermissions = new HashMap<>();

    // Singleton-Instanz
    private static PermissionManager instance;

    // Privater Konstruktor für Singleton
    private PermissionManager() {}

    /**
     * Gibt die Singleton-Instanz zurück.
     *
     * @return Die Instanz des PermissionManagers
     */
    public static synchronized PermissionManager getInstance() {
        if (instance == null) {
            instance = new PermissionManager();
        }
        return instance;
    }

    /**
     * Prüft, ob ein Mitglied eine bestimmte Berechtigung hat.
     *
     * @param member            Das Mitglied, dessen Berechtigung geprüft wird
     * @param requiredPermLevel Die erforderliche Berechtigungsstufe
     * @param commandName       Der Name des Befehls
     * @return true, wenn das Mitglied die Berechtigung hat
     */
    public boolean hasPermission(Member member, PermissionLevel requiredPermLevel, String commandName) {
        if (member == null) {
            LOGGER.warn("Berechtigungsprüfung für null-Mitglied");
            return false;
        }

        Guild guild = member.getGuild();
        long guildId = guild.getIdLong();

        // Serverbesitzer hat immer alle Berechtigungen
        if (guild.getOwnerId().equals(member.getId())) {
            LOGGER.debug("Serverbesitzer hat Zugriff");
            return true;
        }

        // Hole die spezifische Berechtigungsstufe für diesen Befehl
        PermissionLevel configuredLevel = getCommandPermissionLevel(guildId, commandName);

        LOGGER.debug("Konfigurierte Berechtigungsstufe für {}: {}", commandName, configuredLevel);

        switch (configuredLevel) {
            case EVERYONE:
                LOGGER.debug("Befehl {} für alle freigegeben", commandName);
                return true;
            case DJ_ROLE:
                boolean hasDJRole = hasDJRole(member);
                LOGGER.debug("DJ-Rolle Prüfung für {} bei Befehl {}: {}",
                        member.getUser().getName(), commandName, hasDJRole);
                return hasDJRole;
            case ADMIN_ROLE:
                boolean isAdmin = member.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR);
                LOGGER.debug("Admin-Prüfung für {} bei Befehl {}: {}",
                        member.getUser().getName(), commandName, isAdmin);
                return isAdmin;
            case SERVER_OWNER:
                LOGGER.debug("Nur Serverbesitzer hat Zugriff auf {}", commandName);
                return guild.getOwnerId().equals(member.getId());
            default:
                LOGGER.warn("Unbekannte Berechtigungsstufe für {}", commandName);
                return false;
        }
    }

    /**
     * Prüft, ob ein Mitglied eine DJ-Rolle hat.
     *
     * @param member Das zu prüfende Mitglied
     * @return true, wenn das Mitglied eine DJ-Rolle hat
     */
    private boolean hasDJRole(Member member) {
        // Definiere DJ-Rolle Muster
        Pattern djRolePattern = Pattern.compile(".*(?:dj|music|musik|audio).*", Pattern.CASE_INSENSITIVE);

        // Debugging: Zeige alle Rollen des Mitglieds
        LOGGER.debug("Rollen von {}: {}",
                member.getUser().getName(),
                member.getRoles().stream()
                        .map(Role::getName)
                        .toList()
        );

        return member.getRoles().stream()
                .anyMatch(role -> {
                    String roleName = role.getName().toLowerCase();
                    boolean matches = djRolePattern.matcher(roleName).matches();
                    if (matches) {
                        LOGGER.debug("DJ-Rolle gefunden: {}", role.getName());
                    }
                    return matches;
                });
    }

    /**
     * Setzt die Berechtigungsstufe für einen bestimmten Befehl auf einem Server.
     *
     * @param guildId      Die Server-ID
     * @param commandName  Der Name des Befehls
     * @param level        Die Berechtigungsstufe
     */
    public void setCommandPermissionLevel(long guildId, String commandName, PermissionLevel level) {
        serverPermissions.computeIfAbsent(guildId, k -> new HashMap<>())
                .put(commandName.toLowerCase(), level);

        LOGGER.info("Berechtigungsstufe für Befehl {} auf Server {} auf {} gesetzt",
                commandName, guildId, level);
    }

    /**
     * Ruft die Berechtigungsstufe für einen Befehl auf einem Server ab.
     *
     * @param guildId     Die Server-ID
     * @param commandName Der Name des Befehls
     * @return Die Berechtigungsstufe, oder EVERYONE als Standard
     */
    public PermissionLevel getCommandPermissionLevel(long guildId, String commandName) {
        return serverPermissions.getOrDefault(guildId, new HashMap<>())
                .getOrDefault(commandName.toLowerCase(), PermissionLevel.EVERYONE);
    }
}