package com.quartel.discordbot;

import com.quartel.discordbot.config.Config;
import com.quartel.discordbot.core.CommandManager;
import com.quartel.discordbot.core.commands.PingCommand;
import com.quartel.discordbot.core.commands.PingSlashCommand;
import com.quartel.discordbot.core.listeners.MessageListener;
import com.quartel.discordbot.core.listeners.SlashCommandListener;
// import com.quartel.discordbot.modules.music.MusicModule;  // Auskommentiert für den Test

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

/**
 * Hauptklasse des Discord Bots
 * Verantwortlich für die Initialisierung und Konfiguration des Bots
 */
public class Bot {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);

    private static JDA jda;
    private static CommandManager commandManager;

    /**
     * Einstiegspunkt der Anwendung
     */
    public static void main(String[] args) {
        try {
            // Lade die Konfigurationsdatei
            Config.load();
            LOGGER.info("Konfiguration geladen.");

            // Initialisiere CommandManager
            commandManager = new CommandManager();

            // Registriere Testbefehle
            registerTestCommands();

            // Initialisiere den Bot
            initializeBot();

            // Registriere Slash-Befehle bei Discord
            commandManager.registerSlashCommandsWithDiscord(jda);

            // Initialisiere Module - auskommentiert für den Test
            // initializeModules();

            LOGGER.info("Bot erfolgreich gestartet!");
        } catch (Exception e) {
            LOGGER.error("Fehler beim Starten des Bots:", e);
            System.exit(1);
        }
    }

    /**
     * Initialisiert die JDA-Instanz mit den notwendigen Konfigurationen
     */
    private static void initializeBot() throws Exception {
        // Hole den Bot-Token aus der Konfiguration
        String token = Config.getString("bot.token");
        if (token == null || token.isEmpty() || token.equals("YOUR_TOKEN_HERE")) {
            LOGGER.error("Kein gültiger Bot-Token in der Konfiguration gefunden!");
            throw new IllegalArgumentException("Ungültiger Bot-Token");
        }

        // Aktivitätstext für den Bot
        String activityText = Config.getString("bot.activity", "Musik");

        // Definiere benötigte Gateway Intents (Berechtigungen für den Bot)
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,       // Für Nachrichten in Servern
                GatewayIntent.MESSAGE_CONTENT,      // Für Nachrichteninhalte
                GatewayIntent.GUILD_VOICE_STATES    // Für Voice-Funktionalität
        );

        // Erstelle und konfiguriere die JDA-Instanz
        jda = JDABuilder.createDefault(token)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing(activityText))
                .enableIntents(intents)
                .enableCache(CacheFlag.VOICE_STATE) // Cache für Voice-States aktivieren
                .addEventListeners(
                        new MessageListener(),       // Für Textnachrichten-Befehle
                        new SlashCommandListener()   // Für Slash-Befehle
                )
                .build();

        // Warte, bis der Bot vollständig verbunden ist
        jda.awaitReady();

        // Füge einen Shutdown Hook hinzu, um den Bot sauber zu beenden
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Bot wird heruntergefahren...");
            if (jda != null) {
                jda.shutdown();
            }
        }));
    }

    /**
     * Registriert einige einfache Testbefehle für den Bot
     */
    private static void registerTestCommands() {
        LOGGER.info("Testbefehle werden registriert...");

        // Registriere Textnachrichten-Befehl
        commandManager.registerCommand(new PingCommand());

        // Registriere Slash-Befehl
        commandManager.registerSlashCommand(new PingSlashCommand());

        LOGGER.info("Testbefehle erfolgreich registriert");
    }

    /**
     * Initialisiert alle aktivierten Module basierend auf der Konfiguration
     * (Auskommentiert für den Test)
     */
    /*
    private static void initializeModules() {
        String enabledModules = Config.getString("modules.enabled", "music");

        // Überprüfe, welche Module aktiviert sind
        for (String module : enabledModules.split(",")) {
            module = module.trim().toLowerCase();

            switch (module) {
                case "music":
                    // Initialisiere das Musik-Modul
                    new MusicModule().initialize(jda, commandManager);
                    LOGGER.info("Musik-Modul initialisiert");
                    break;

                // Hier können weitere Module hinzugefügt werden

                default:
                    LOGGER.warn("Unbekanntes Modul in der Konfiguration: {}", module);
            }
        }
    }
    */

    /**
     * Gibt die aktuelle JDA-Instanz zurück
     */
    public static JDA getJda() {
        return jda;
    }

    /**
     * Gibt den CommandManager zurück
     */
    public static CommandManager getCommandManager() {
        return commandManager;
    }
}