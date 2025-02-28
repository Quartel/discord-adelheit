package com.quartel.discordbot;

import com.quartel.discordbot.config.Config;
import com.quartel.discordbot.core.CommandManager;
import com.quartel.discordbot.core.listeners.SlashCommandListener;
import com.quartel.discordbot.modules.Module;
import com.quartel.discordbot.modules.music.MusicModule;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Die Hauptklasse des Discord-Bots.
 */
public class Bot extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    private static Bot instance;

    private JDA jda;
    private CommandManager commandManager;
    private final Map<String, Module> modules = new HashMap<>();
    private boolean running = false;

    /**
     * Privater Konstruktor für Singleton-Muster.
     */
    private Bot() {
        LOGGER.info("Bot-Instanz erstellt");
    }

    /**
     * Gibt die einzige Instanz des Bots zurück oder erstellt sie, falls sie nicht existiert.
     *
     * @return Die Bot-Instanz
     */
    public static synchronized Bot getInstance() {
        if (instance == null) {
            instance = new Bot();
        }
        return instance;
    }

    /**
     * Startet den Bot.
     */
    public void start() {
        if (running) {
            LOGGER.warn("Bot läuft bereits");
            return;
        }

        LOGGER.info("Starte Bot...");

        try {
            // Konfiguration laden
            String token = Config.getToken();
            if (token == null || token.isEmpty() || "YOUR_TOKEN_HERE".equals(token)) {
                LOGGER.error("Kein gültiger Bot-Token in der Konfiguration gefunden");
                return;
            }

            // JDA konfigurieren und erstellen
            JDABuilder builder = JDABuilder.createDefault(token)
                    // Aktiviere notwendige Intents
                    .enableIntents(
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    // Cache-Einstellungen
                    .setMemberCachePolicy(MemberCachePolicy.VOICE)
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .enableCache(CacheFlag.VOICE_STATE)
                    // Bot-Status
                    .setStatus(OnlineStatus.ONLINE)
                    .setActivity(Activity.playing(Config.getActivity()));

            // Bot erstellen und auf Bereitschaft warten
            jda = builder.build().awaitReady();
            LOGGER.info("JDA erfolgreich initialisiert");

            // Command-Manager erstellen
            commandManager = new CommandManager(jda);
            commandManager.registerDefaultListeners();

            // SlashCommandListener hinzufügen
            jda.addEventListener(new SlashCommandListener(this));

            // Module registrieren und laden
            registerModules();
            loadEnabledModules();

            running = true;
            LOGGER.info("Bot erfolgreich gestartet!");

        } catch (Exception e) {
            LOGGER.error("Fehler beim Starten des Bots", e);
        }
    }

    /**
     * Stoppt den Bot.
     */
    public void stop() {
        if (!running) {
            LOGGER.warn("Bot ist nicht gestartet");
            return;
        }

        LOGGER.info("Stoppe Bot...");

        try {
            // Module deaktivieren
            disableAllModules();

            // Listener entfernen
            if (commandManager != null) {
                commandManager.removeAllListeners();
            }

            // JDA herunterfahren
            if (jda != null) {
                jda.shutdown();
                jda = null;
            }

            running = false;
            LOGGER.info("Bot erfolgreich gestoppt");

        } catch (Exception e) {
            LOGGER.error("Fehler beim Stoppen des Bots", e);
        }
    }

    /**
     * Registriert alle verfügbaren Module.
     */
    private void registerModules() {
        // Musik-Modul registrieren
        registerModule(new MusicModule());

        // Hier könnten weitere Module registriert werden

        LOGGER.info("{} Module registriert", modules.size());
    }

    /**
     * Registriert ein Modul.
     *
     * @param module Das zu registrierende Modul
     */
    public void registerModule(Module module) {
        modules.put(module.getName().toLowerCase(), module);
        module.load();
        LOGGER.info("Modul {} registriert", module.getName());
    }

    /**
     * Lädt alle in der Konfiguration aktivierten Module.
     */
    private void loadEnabledModules() {
        String[] enabledModules = Config.getEnabledModules();

        if (enabledModules.length == 0) {
            LOGGER.warn("Keine Module in der Konfiguration aktiviert");
            return;
        }

        for (String moduleName : enabledModules) {
            enableModule(moduleName.trim().toLowerCase());
        }
    }

    /**
     * Aktiviert ein Modul anhand seines Namens.
     *
     * @param moduleName Der Name des zu aktivierenden Moduls
     * @return true, wenn das Modul erfolgreich aktiviert wurde, sonst false
     */
    public boolean enableModule(String moduleName) {
        Module module = modules.get(moduleName.toLowerCase());

        if (module == null) {
            LOGGER.warn("Modul {} nicht gefunden", moduleName);
            return false;
        }

        if (module.isEnabled()) {
            LOGGER.warn("Modul {} ist bereits aktiviert", moduleName);
            return true;
        }

        module.enable(jda);
        LOGGER.info("Modul {} aktiviert", moduleName);
        return true;
    }

    /**
     * Deaktiviert ein Modul anhand seines Namens.
     *
     * @param moduleName Der Name des zu deaktivierenden Moduls
     * @return true, wenn das Modul erfolgreich deaktiviert wurde, sonst false
     */
    public boolean disableModule(String moduleName) {
        Module module = modules.get(moduleName.toLowerCase());

        if (module == null) {
            LOGGER.warn("Modul {} nicht gefunden", moduleName);
            return false;
        }

        if (!module.isEnabled()) {
            LOGGER.warn("Modul {} ist bereits deaktiviert", moduleName);
            return true;
        }

        module.disable(jda);
        LOGGER.info("Modul {} deaktiviert", moduleName);
        return true;
    }

    /**
     * Deaktiviert alle Module.
     */
    private void disableAllModules() {
        for (Module module : modules.values()) {
            if (module.isEnabled()) {
                module.disable(jda);
                LOGGER.info("Modul {} deaktiviert", module.getName());
            }
        }
    }

    /**
     * Gibt die JDA-Instanz zurück.
     *
     * @return Die JDA-Instanz
     */
    public JDA getJda() {
        return jda;
    }

    /**
     * Gibt den CommandManager zurück.
     *
     * @return Der CommandManager
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Gibt eine Liste aller verfügbaren Module zurück.
     *
     * @return Eine Liste der Module
     */
    public List<Module> getModules() {
        return new ArrayList<>(modules.values());
    }

    /**
     * Prüft, ob der Bot läuft.
     *
     * @return true, wenn der Bot läuft, sonst false
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Die Main-Methode zum Starten des Bots.
     *
     * @param args Befehlszeilenargumente (nicht verwendet)
     */
    public static void main(String[] args) {
        // Bot starten
        Bot bot = Bot.getInstance();
        bot.start();

        // Shutdown-Hook registrieren, um den Bot sauber zu beenden
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown-Signal empfangen, beende Bot...");
            bot.stop();
        }));

        LOGGER.info("Bot-Hauptprogramm gestartet. Drücke Ctrl+C zum Beenden.");
    }
}