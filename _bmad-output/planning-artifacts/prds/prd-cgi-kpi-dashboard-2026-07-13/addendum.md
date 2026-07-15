---
title: "PRD Addendum: cgi-kpi-dashboard"
status: final
created: 2026-07-13
updated: 2026-07-14
parent: prd.md
---

# PRD Addendum

Technische und fachliche Vertiefung, die nicht in die PRD-Hauptnarrative gehört. Verbindliche Produktanforderungen stehen in [prd.md](prd.md).

## Technische Grundrichtung (aus Brief, für Architecture)

| Bereich | Technologie |
|---------|-------------|
| Backend | Java 21, Spring Boot 3.5.x, Maven, REST, Spring Data JPA, PostgreSQL, Flyway, Jakarta Bean Validation, Actuator |
| Tests | JUnit, Spring Boot Test |
| Frontend | Angular 20, TypeScript, SCSS, Angular Material, Angular CDK, CGI Experience Design System 19.0.0, HttpClient, RxJS, Angular Signals |
| Struktur | Monorepo; KPI-Berechnung im Backend |
| KI | Gemini API ausschließlich serverseitig; API-Key nicht im Frontend/Git |

## KPI-Inventar

Detaillierte Metrik-Liste mit `[OFFEN]`-Formeln: [Brief-Addendum](../briefs/brief-cgi-kpi-dashboard-2026-07-13/addendum.md).

PRD-FR-9 verlangt Abdeckung aller fünf KPI-Bereiche; fachliche Definitionen werden im Pilot oder vor Architecture-Sign-off geklärt.

## Mock-Portfolio (FR-19)

Ca. 20 Projekte mit mindestens einem Vertreter pro Szenario:

| Szenario | Zweck im Pilot |
|----------|----------------|
| Im Plan | Baseline, positive Referenz |
| Terminverzug | Terminabweichung, Ampel Gelb/Rot |
| Erhöhter Budgetverbrauch | Budget-KPI, Budget-Prognose |
| Offene Risiken | Risiko-Darstellung, Q&A |
| Widersprüchliche Signale | KPI vs. KI-Interpretation testen |
| Abgeschlossen | Portfolio-Aggregation, Filter |

Konkretes Datenmodell (Entitäten, Felder): `[OFFEN]` — Architecture/Story-Ebene.

## Demo auf Arbeitsrechner

Betriebsanforderung: `[OFFEN]` (lokaler Start, Docker, o. Ä.).

## Gemini-Integration (Hinweise für Architecture)

- Separate Endpunkte oder Use Cases: Portfolio-Trendanalyse, Management-Zusammenfassung, Prognose, Projekt-Q&A
- Prompting und Eingabe-Schema für freigegebene Daten: `[OFFEN]`
- Speicherung von KI-Ausgaben: `[OFFEN]`
- Timeout/Fehlerbehandlung → FR-15
