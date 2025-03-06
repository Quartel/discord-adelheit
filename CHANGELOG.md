# Changelog

## [0.2.0-alpha] - 2025-03-06

### Added
- Berechtigungssystem für Befehle und Benutzerrollen
- Verbessertes Error-Handling für instabile Netzwerkverbindungen
- Auto-Completion für lokale Playlists
- Befehl zum Anzeigen von Playlist-Vorschauen
- Verbesserte Dokumentation der Kommandos
- Optimierte Musikwiedergabe mit reduziertem Puffern
- Bessere visuelle Darstellung des Abspielfortschritts

### Changed
- Optimiertes Logging für verbesserte Fehleranalyse
- Verbesserte Reaktionszeiten bei Slash-Commands
- Verbesserte Benutzerfreundlichkeit bei Fehlermeldungen
- Youtube Unterstützung entfernt

### Fixed
- Problem mit dem automatischen Trennen bei längeren Tracks
- Verbesserte Stabilität bei Verbindungsabbrüchen

## [0.1.0-alpha] - 2025-02-28

### Added
- Grundlegende Musikwiedergabe-Funktionalität
- Lokale und Remote-Playlist-Unterstützung
- Modulares Bot-Design mit Musik Modul
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
