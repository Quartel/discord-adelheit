package com.quartel.discordbot.core;

import java.util.Collections;
import java.util.List;

/**
 * Basisklasse für Befehle, die das Command-Interface teilweise implementiert.
 * Konkrete Befehle können von dieser Klasse erben, um Redundanz zu reduzieren.
 */
public abstract class BaseCommand implements Command {

    private final String name;
    private final String description;
    private final String syntax;
    private final String category;
    private final List<String> aliases;

    /**
     * Konstruktor für einen Befehl mit allen Details.
     *
     * @param name Der Name des Befehls
     * @param description Die Beschreibung des Befehls
     * @param syntax Die Syntax des Befehls
     * @param category Die Kategorie des Befehls
     * @param aliases Aliase für den Befehl (optional)
     */
    public BaseCommand(String name, String description, String syntax, String category, List<String> aliases) {
        this.name = name;
        this.description = description;
        this.syntax = syntax;
        this.category = category;
        this.aliases = aliases != null ? aliases : Collections.emptyList();
    }

    /**
     * Konstruktor für einen Befehl ohne Aliase.
     *
     * @param name Der Name des Befehls
     * @param description Die Beschreibung des Befehls
     * @param syntax Die Syntax des Befehls
     * @param category Die Kategorie des Befehls
     */
    public BaseCommand(String name, String description, String syntax, String category) {
        this(name, description, syntax, category, Collections.emptyList());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getSyntax() {
        return syntax;
    }

    @Override
    public String getCategory() {
        return category;
    }
}