package com.quartel.discordbot.core;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * Basisklasse für Slash-Befehle.
 * Implementiert gemeinsame Funktionalität für alle Slash-Befehle.
 */
public abstract class BaseSlashCommand implements SlashCommand {

    private final String name;
    private final String description;
    private final String category;

    /**
     * Konstruktor für einen Slash-Befehl.
     *
     * @param name Der Name des Befehls
     * @param description Die Beschreibung des Befehls
     * @param category Die Kategorie des Befehls
     */
    public BaseSlashCommand(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getCategory() {
        return category;
    }

    /**
     * Gibt die grundlegende CommandData für diesen Slash-Befehl zurück.
     * Diese Methode kann von Unterklassen überschrieben werden, um zusätzliche Optionen hinzuzufügen.
     *
     * @return Die CommandData für diesen Befehl
     */
    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description);
    }
}