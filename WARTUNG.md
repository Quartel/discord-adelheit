# Wartungs- und Debugging-Funktionen

Diese Dokumentation beschreibt spezielle Funktionen, die für die Wartung, Fehlerbehebung und Optimierung des Discord-Bots entwickelt wurden. Diese Funktionen sind in der Regel im normalen Betrieb deaktiviert, können aber bei Bedarf aktiviert werden.

## Inhaltsverzeichnis

1. [Befehlsbereinigung (Command Cleanup)](#befehlsbereinigung-command-cleanup)
    - [Hintergrund](#hintergrund)
    - [Funktionsweise](#funktionsweise)
    - [Verwendung](#verwendung)
    - [Fehlerszenarien](#fehlerszenarien)
2. [Zukünftige Wartungsfunktionen](#zukünftige-wartungsfunktionen)

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

## Zukünftige Wartungsfunktionen

Dieser Abschnitt dient als Platzhalter für zukünftige Wartungsfunktionen, die dem Bot hinzugefügt werden. Jede neue Funktion sollte hier dokumentiert werden, einschließlich:

- Zweck und Hintergrund der Funktion
- Konfigurationsmöglichkeiten
- Aktivierung und Deaktivierung
- Empfohlene Verwendungsszenarien
- Mögliche Probleme und deren Lösungen

---

## Änderungsprotokoll der Wartungsfunktionen

| Datum      | Version | Funktion             | Beschreibung                                   |
|------------|---------|----------------------|------------------------------------------------|
| 06.03.2025 | 0.1.0   | Befehlsbereinigung   | Erstimplementierung zur Lösung doppelter Befehle |

---