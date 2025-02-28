# Changelog

## [0.1.0-alpha] - 2025-02-28

### Added
- Grundlegende Musikwiedergabe-Funktionalität
- Lokale und Remote-Playlist-Unterstützung
- Modulares Bot-Design mit Musik-Modul
- Slash-Commands für Musiksteuerung:
    - `/play` - Musik abspielen
    - `/skip` - Track überspringen
    - `/stop` - Wiedergabe stoppen
    - `/queue` - Warteschlange anzeigen
    - `/nowplaying` - Aktuellen Track anzeigen
    - `/volume` - Lautstärke anpassen
    - `/pause` - Wiedergabe pausieren
    - `/resume` - Wiedergabe fortsetzen

### Configuration
- Zentralisiertes Konfigurationsmanagement
- Unterstützung für Konfigurationsdateien
- Dynamische Modulaktivierung

### Technical
- Maven-basiertes Build-System
- Logging-Unterstützung mit Logback
- Verwendung von JDA für Discord-Integration
- LavaPlayer für Audio-Streaming

### Todo for Next Release
- Verbesserte Fehlerbehandlung
- Erweiterte Konfigurationsoptionen
- Unit-Tests hinzufügen
- Permissions-System implementieren