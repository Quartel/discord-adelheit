# Wartungs- und Debugging-Funktionen

Diese Dokumentation beschreibt spezielle Funktionen, die für die Wartung, Fehlerbehebung und Optimierung des Discord-Bots entwickelt wurden. Diese Funktionen sind in der Regel im normalen Betrieb deaktiviert, können aber bei Bedarf aktiviert werden.

## Inhaltsverzeichnis

1. [Befehlsbereinigung (Command Cleanup)](#befehlsbereinigung-command-cleanup)
    - [Hintergrund](#hintergrund)
    - [Funktionsweise](#funktionsweise)
    - [Verwendung](#verwendung)
    - [Fehlerszenarien](#fehlerszenarien)
2. [Konfigurationsmanagement](#Konfigurationsmanagement)
    - [Funktionsweise](#funktionsweise-des-konfigurationssystems)
    - [Hinzufügen neuer Konfigurationsoptionen](#hinzufügen-neuer-konfigurationsoptionen)
    - [So funktioniert die Konfigurationsmigration](#so-funktioniert-die-konfigurationsmigration)
    - [Best Practices](#best-practices)

---

## Befehlsbereinigung (Command Cleanup)

### Hintergrund

Discord-Bots können Befehle auf zwei Arten registrieren:
- **Global**: Die Befehle werden für den gesamten Bot registriert und erscheinen auf allen Servern
- **Guild-spezifisch**: Die Befehle werden nur für bestimmte Server registriert

Wenn ein Bot versehentlich Befehle sowohl global als auch guild-spezifisch registriert, können Befehle doppelt angezeigt werden. Dies führt zu Verwirrung bei den Nutzern und kann die Nutzererfahrung beeinträchtigen.

### Funktionsweise

Die Befehlsbereinigung ist eine spezielle Funktion, die alle registrierten Befehle (global und guild-spezifisch) löscht, bevor neue Befehle registriert werden. Dies schafft einen "sauberen Zustand" und verhindert doppelte Befehle.

Der Prozess läuft in zwei Phasen ab:
1. **Löschung aller bestehenden Befehle**:
    - Globale Befehle werden entfernt
    - Befehle auf jedem Server werden einzeln entfernt
2. **Registrierung neuer Befehle**:
    - Nach der Bereinigung werden neue Befehle nur auf Server-Ebene registriert

Die Funktion wird durch die Klasse `CommandCleaner` implementiert und kann über ein Flag in der Bot-Klasse aktiviert werden.

### Verwendung

Die Befehlsbereinigung wird über das Flag `CLEAN_COMMANDS_ON_START` in der Klasse `Bot.java` gesteuert:

```java
private static final boolean CLEAN_COMMANDS_ON_START = true; // Bereinigung aktivieren
```

**Anleitung zur Verwendung**:

1. **Aktivieren**: Setze `CLEAN_COMMANDS_ON_START` auf `true`
2. **Ausführen**: Starte den Bot einmalig, um die Bereinigung durchzuführen
3. **Deaktivieren**: Nach erfolgreicher Bereinigung setze `CLEAN_COMMANDS_ON_START` zurück auf `false`
4. **Normalbetrieb**: Starte den Bot neu für normalen Betrieb

**Wann sollte die Bereinigung aktiviert werden?**
- Wenn Befehle doppelt in Discord angezeigt werden
- Nach größeren Änderungen an der Befehlsstruktur
- Beim Wechsel zwischen Entwicklungs- und Produktionsumgebungen
- Nach einem Update der JDA-Bibliothek
- Wenn der Bot vielen neuen Servern in kurzer Zeit beitritt

**Wann sollte die Bereinigung deaktiviert sein?**
- Im regulären Betrieb des Bots
- Für schnelleren Startvorgang
- Um unnötige Discord API-Aufrufe zu vermeiden
- Wenn keine Probleme mit Befehlen bestehen

### Fehlerszenarien

Falls nach der Befehlsbereinigung Probleme auftreten:

1. **Keine Befehle sichtbar**:
    - Es kann bis zu 1 Stunde dauern, bis Änderungen von Discord vollständig übernommen werden
    - Starte den Bot neu, um sicherzustellen, dass neue Befehle korrekt registriert wurden

2. **Fehler beim Löschen von Befehlen**:
    - Überprüfe die Logs auf spezifische Fehlermeldungen
    - Stelle sicher, dass der Bot über die notwendigen Berechtigungen verfügt
    - Bei Rate-Limiting-Problemen: Warte einige Zeit und versuche es erneut

3. **Befehle immer noch doppelt**:
    - Überprüfe, ob andere Teile des Codes Befehle global registrieren
    - Stelle sicher, dass der Bot korrekt neu gestartet wurde
    - Suche in allen Modulen nach `updateCommands()` oder ähnlichen Aufrufen

---

## Konfigurationsmanagement

### Funktionsweise des Konfigurationssystems

Der Adelheit Bot verwendet ein zweistufiges Konfigurationssystem:

1. **Zentrale Standardwerte** werden in der Klasse `DefaultConfigManager` definiert
2. **Benutzerspezifische Werte** werden in der Datei `config/config.properties` gespeichert

Das System bietet folgende Vorteile:

- Automatische Migration neuer Konfigurationsoptionen in bestehende Konfigurationsdateien
- Zentrale Definition aller verfügbaren Optionen und ihrer Standardwerte
- Typsichere Zugriffsmethoden für Konfigurationswerte

### Hinzufügen neuer Konfigurationsoptionen

Wenn du neue Features entwickelst, die Konfigurationsoptionen benötigen, befolge diese Schritte:

#### 1. Standardwert in DefaultConfigManager registrieren

Öffne die Datei `src/main/java/com/quartel/discordbot/config/DefaultConfigManager.java`.
Füge in der `static`-Initialisierung eine neue Zeile hinzu:

```java
// Im static-Block:
registerDefault("modul.feature.option", "Standardwert");
```

Verwende dabei folgende Namenskonventionen:
- `modul` - Name des Moduls (z.B. "music", "admin")
- `feature` - Name des Features (z.B. "playlist", "permissions")
- `option` - Name der spezifischen Option (z.B. "enabled", "timeout")

#### 2. Getter-Methode in Config.java hinzufügen (empfohlen)

Erweitere die Klasse `Config.java` um eine typisierte Zugriffsmethode:

```java
/**
 * Beschreibung der Option
 *
 * @return Beschreibung des Rückgabewerts
 */
public static String getFeatureOption() {
    return getProperty("modul.feature.option");
}

// Für numerische Werte:
public static int getFeatureOptionZahl() {
    return Integer.parseInt(getProperty("modul.feature.option_zahl", "123"));
}

// Für boolesche Werte:
public static boolean isFeatureEnabled() {
    return Boolean.parseBoolean(getProperty("modul.feature.enabled", "false"));
}
```

#### 3. Dokumentation in Example-Datei aktualisieren

Obwohl nicht technisch erforderlich, ist es gute Praxis, die Beispielkonfigurationsdatei zu aktualisieren:

```properties
# In src/main/resources/config.properties.example:

# Beschreibung des Features
# Mögliche Werte: Wert1, Wert2, ...
modul.feature.option=Standardwert
```

### So funktioniert die Konfigurationsmigration

Wenn ein Benutzer den Bot startet:

1. Die `Config.java` lädt die bestehende Konfigurationsdatei
2. Die Methode `migrateConfig()` wird aufgerufen
3. Jede in `DefaultConfigManager` registrierte Option wird überprüft
4. Fehlende Optionen werden mit ihren Standardwerten zur Konfigurationsdatei hinzugefügt
5. Die aktualisierte Konfigurationsdatei wird gespeichert

Der Benutzer bemerkt diesen Prozess nur durch eine Logmeldung und muss nichts manuell tun.

### Best Practices

#### Konfigurationswerte gruppieren

Verwende konsistente Präfixe, um zusammengehörige Einstellungen zu gruppieren:

```properties
# Gut:
music.volume.default=50
music.volume.max=100
music.timeout=60

# Vermeiden:
default_volume=50
max_vol=100
music_timeout=60
```

#### Standardwerte sinnvoll wählen

Wähle immer sichere und funktionale Standardwerte, damit der Bot auch ohne manuelle Konfiguration läuft.

#### Sensible Daten behandeln

- Für sensible Daten wie API-Schlüssel keine echten Werte als Standard setzen
- Verwende Platzhalter wie "YOUR_API_KEY_HERE"
- Validiere sensible Werte immer vor der Verwendung

#### Konfigurationsänderungen dokumentieren

Wenn du neue Konfigurationsoptionen hinzufügst, dokumentiere diese in:

1. CHANGELOG.md - Unter der entsprechenden Versionssektion
2. README.md - Falls relevant für den Endbenutzer

### Beispiel: Neue Feature-Konfiguration

Angenommen, du möchtest ein Feature für automatische Playlist-Wiederholung implementieren:

#### 1. In DefaultConfigManager.java:

```java
registerDefault("music.playlist.repeat.enabled", "false");
registerDefault("music.playlist.repeat.count", "3");
```

#### 2. In Config.java:

```java
/**
 * Prüft, ob die automatische Playlist-Wiederholung aktiviert ist.
 *
 * @return true, wenn die Wiederholung aktiviert ist
 */
public static boolean isPlaylistRepeatEnabled() {
    return Boolean.parseBoolean(getProperty("music.playlist.repeat.enabled", "false"));
}

/**
 * Gibt an, wie oft eine Playlist wiederholt werden soll.
 *
 * @return Anzahl der Wiederholungen (mindestens 1)
 */
public static int getPlaylistRepeatCount() {
    int count = Integer.parseInt(getProperty("music.playlist.repeat.count", "3"));
    return Math.max(1, count);  // Stelle sicher, dass der Wert mindestens 1 ist
}
```

#### 3. In config.properties.example:

```properties
# Playlist-Wiederholung
# Wenn aktiviert, wird eine Playlist mehrfach abgespielt
music.playlist.repeat.enabled=false
# Anzahl der Wiederholungen (wenn aktiviert)
music.playlist.repeat.count=3
```

Diese Dokumentation sollte den Entwicklern einen klaren Weg bieten, neue Konfigurationsoptionen zu implementieren und das Konfigurationssystem zu verstehen.

---

## Änderungsprotokoll der Wartungsfunktionen

| Datum      | Version | Funktion                 | Beschreibung                                     |
|------------|---------|--------------------------|--------------------------------------------------|
| 06.03.2025 | 0.1.0   | Befehlsbereinigung       | Erstimplementierung zur Lösung doppelter Befehle |
| 07.03.2024 | 0.1.0   | Konfigurationsmanagement | Erstimplementierung Konfigurationsmanagement     |

---