# Requirements

Die wichtigsten Anforderungen für den aktuellen Pilotstand.

## Funktionale Anforderungen

1. **Portfolioübersicht** mit Projektstatus und zentralen Kennzahlen
2. **Projektliste mit Filtern**, z. B. nach Kunde, Projektleitung, Status, Phase und Vorgehensmodell
3. **Projekt-Detailansicht** mit Stammdaten, KPIs, Trends, Risiken und Problemen
4. **Vergleich verschiedener Berichtsstände** zur Darstellung von Veränderungen im Zeitverlauf
5. **Team- und Kapazitätsinformationen** auf Projektebene
6. **Agile Delivery** für agile und hybride Projekte mit Sprints und relevanten Delivery-Kennzahlen
7. **ISO-21502-Steuerungsfelder** als zusätzliche Informationen zur Projektsteuerung
8. **KI-Unterstützung**
   - Analyse von Auffälligkeiten und Mustern im Portfolio
   - Zusammenfassung und Fragen zu einzelnen Projekten
   - eindeutige Kennzeichnung von KI-generierten Einschätzungen
9. **Authentifizierung und Rollenmodell** für Admin- und Workspace-Nutzer

## Technische Anforderungen

- Die KPI-Berechnung funktioniert unabhängig von der KI-Komponente.
- Zugangsdaten und API-Keys werden ausschließlich serverseitig bzw. über Environment Variables verwaltet.
- API-Fehler werden strukturiert mit `code` und `message` zurückgegeben.
- Die Oberfläche ist zunächst für Desktop-Nutzung ausgelegt.
- Für Pilot und öffentliche Demo werden ausschließlich Mock- bzw. Demo-Daten verwendet.

## Abgrenzung

Das Dashboard soll bestehende Projektmanagement-Systeme nicht ersetzen, sondern eine kompakte Steuerungs- und Auswertungsebene darüber bereitstellen.

Nicht Bestandteil des Piloten sind:

- vollständige Projektplanung und Aufgabenverwaltung
- automatische Entscheidungen oder Maßnahmen durch die KI
- produktive Kundendaten in der öffentlichen Demo
