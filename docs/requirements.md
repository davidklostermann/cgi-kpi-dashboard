# Requirements (kompakt)

Zusammengefasst aus den finalen Produktanforderungen des Pilots.

## Funktionale Schwerpunkte

1. **Portfolioübersicht** mit Statusverteilung und Kennzahlenkarten
2. **Filterbare Projektliste** (u. a. Kunde, Leitung, Status, Phase, Vorgehensmodell)
3. **Projekt-Detail** mit Stammdaten, KPIs, Trends, Risiken/Problemen
4. **Berichtsstandsvergleich** über Zeit
5. **Kapazität / Team** Informationen
6. **Agile Delivery** für agile/hybride Projekte (Sprints, Delivery-Kennzahlen)
7. **ISO-Steuerungsfelder** als ergänzende Managementdaten
8. **KI-Schicht**
   - Portfolio-Musteranalyse
   - Projekt-Assistent (Zusammenfassung / Fragen)
   - klare Kennzeichnung als KI-Einschätzung
9. **Authentifizierung** mit Rollen (Admin/Workspace) und Session-Sicherheit

## Nichtfunktionale Leitplanken

- KPIs bleiben bei KI-Ausfall nutzbar
- Secrets nur serverseitig / über Environment Variables
- API-Fehler als strukturierte Responses (`code`, `message`)
- Desktop-first UI
- Mock-/Demo-Daten für den Pilot und die öffentliche Demo

## Explizite Nicht-Ziele

- Kein Ersatz für vollständige PM-Tools
- Keine automatischen Entscheidungen durch KI
- Keine produktiven Kundendaten in der öffentlichen Demo
