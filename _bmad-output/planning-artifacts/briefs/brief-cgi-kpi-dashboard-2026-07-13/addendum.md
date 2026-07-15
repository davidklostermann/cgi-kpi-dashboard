---
title: "Addendum: KPI-Inventar — cgi-kpi-dashboard"
status: draft
created: 2026-07-13
updated: 2026-07-15
parent: brief.md
---

# Addendum: KPI-Inventar

Dieses Addendum ergänzt den [Product Brief](brief.md) um die im Coaching besprochene **detaillierte KPI-Inventarliste**. Es dient als Input für PRD und technische Spezifikation.

**Legende:**

| Markierung | Bedeutung |
|------------|-----------|
| `[OFFEN]` | Formel, Schwellenwert, Datenquelle oder fachliche Definition noch nicht bestätigt |
| KI-Ausgabe | Kein deterministischer KPI — wird von Gemini erzeugt, klar als KI gekennzeichnet |

**Berechnungsprinzip:** Alle deterministischen Kennzahlen werden im Spring-Boot-Backend berechnet. Gemini erfindet keine KPI-Werte.

---

## 1. Projektportfolio

Portfolioübergreifende Aggregatkennzahlen.

| KPI-Kandidat | Beschreibung | Formel / Berechnung | Schwellenwerte | Datenquelle |
|--------------|--------------|---------------------|----------------|-------------|
| Anzahl aktiver Projekte | Projekte mit Status „aktiv" | `[OFFEN]` | — | `[OFFEN]` |
| Anzahl abgeschlossener Projekte | Projekte mit Status „abgeschlossen" | `[OFFEN]` | — | `[OFFEN]` |
| Anzahl Projekte je Kunde | Verteilung nach Auftraggeber/Kunde | `[OFFEN]` | — | `[OFFEN]` |
| Anzahl Projekte je Projektphase | Verteilung nach Phase | `[OFFEN]` | Projektphasen: `[OFFEN]` | `[OFFEN]` |
| Anzahl gefährdeter Projekte | Projekte mit erhöhtem Risiko oder kritischem Status | `[OFFEN]` | Definition „gefährdet": `[OFFEN]` | `[OFFEN]` |
| Anzahl verspäteter Projekte | Projekte mit Terminabweichung über Schwellenwert | `[OFFEN]` | Schwellenwert Terminabweichung: `[OFFEN]` | `[OFFEN]` |

---

## 2. Projektstatus

Projektbezogene Status- und Fortschrittskennzahlen (Einzelprojekt und aggregiert).

| KPI-Kandidat | Beschreibung | Formel / Berechnung | Schwellenwerte | Datenquelle |
|--------------|--------------|---------------------|----------------|-------------|
| Projektstatus (Grün / Gelb / Rot) | Ampelstatus je Projekt | `[OFFEN]` | Grün/Gelb/Rot: `[OFFEN]` | `[OFFEN]` |
| Projektfortschritt in Prozent | Fortschritt gegenüber Plan | `[OFFEN]` | — | `[OFFEN]` |
| Terminabweichung | Abweichung vom geplanten Endtermin | `[OFFEN]` | — | `[OFFEN]` |
| Meilenstein-Erfüllung | Erfüllungsgrad definierter Meilensteine | `[OFFEN]` | — | `[OFFEN]` |
| Anzahl offener Aufgaben | Noch nicht abgeschlossene Aufgaben | `[OFFEN]` | — | `[OFFEN]` |
| Anzahl überfälliger Aufgaben | Aufgaben über Fälligkeitsdatum | `[OFFEN]` | — | `[OFFEN]` |

**Hinweis MVP-Scope:** Aufgaben-Metriken sind **nicht** Teil des Dashboards (kein Aufgabenplanungs-System). Spalten bleiben im Inventar zu späterer Abstimmung `[NON-GOAL im MVP]`.

| Prognostiziertes Enddatum | Voraussichtliches Projektende (deterministisch) | `[OFFEN]` | — | `[OFFEN]` |
| Zeitverbrauch / vergangene Projektdauer | Anteil verstrichene Projektlaufzeit | `[OFFEN]` | — | `[OFFEN]` |
| Letzte Datenaktualisierung | Zeitpunkt des Berichtsstands | Anzeige aus Datenfeld | Stale-Daten-Schwelle: `[OFFEN]` | `[OFFEN]` |

---

## 3. Budget und Aufwand

Plan-Ist-Vergleich für Budget und Personentage.

| KPI-Kandidat | Beschreibung | Formel / Berechnung | Schwellenwerte | Datenquelle |
|--------------|--------------|---------------------|----------------|-------------|
| Geplantes Projektbudget | Budget laut Plan | `[OFFEN]` | — | `[OFFEN]` |
| Bisherige Ist-Kosten | Tatsächlich angefallene Kosten | `[OFFEN]` | — | `[OFFEN]` |
| Restbudget | Verbleibendes Budget | `[OFFEN]` | — | `[OFFEN]` |
| Budgetverbrauch in Prozent | Anteil verbrauchtes Budget | `[OFFEN]` | Budgetrisiko-Schwelle: `[OFFEN]` | `[OFFEN]` |
| Geplante Personentage | Aufwand laut Plan | `[OFFEN]` | — | `[OFFEN]` |
| Verbrauchte Personentage | Tatsächlich gebuchter Aufwand | `[OFFEN]` | — | `[OFFEN]` |
| Verbleibende Personentage | Noch verfügbarer Aufwand | `[OFFEN]` | — | `[OFFEN]` |
| Plan-Ist-Abweichung (Budget/Aufwand) | Abweichung Plan vs. Ist | `[OFFEN]` | — | `[OFFEN]` |
| Deterministische End-Hochrechnung Budget | Prognostizierter Budget-Endwert | `[OFFEN]` | — | `[OFFEN]` |
| Deterministische End-Hochrechnung Aufwand | Prognostizierter Aufwand-Endwert | `[OFFEN]` | — | `[OFFEN]` |
| Restbudget / Restaufwand | Verbleibende Werte | `[OFFEN]` | — | `[OFFEN]` |

---

## 4. Risiken und Probleme

Risiko- und Problemkennzahlen je Projekt (und aggregiert im Portfolio).

| KPI-Kandidat | Beschreibung | Formel / Berechnung | Schwellenwerte | Datenquelle |
|--------------|--------------|---------------------|----------------|-------------|
| Anzahl offener Risiken | Noch nicht geschlossene Risiken | `[OFFEN]` | — | `[OFFEN]` |
| Anzahl kritischer Probleme | Probleme mit höchster Schwere | `[OFFEN]` | Definition „kritisch": `[OFFEN]` | `[OFFEN]` |
| Offene Risiken (Darstellung) | Liste/Detail offener Risiken im MVP | Mindestfelder laut PRD FR-6 | — | `[OFFEN]` |
| Kritische Probleme (Darstellung) | Liste/Detail kritischer Probleme im MVP | Mindestfelder laut PRD FR-6; getrennt von Risiken | — | `[OFFEN]` |
| Anzahl kritischer Risiken | Risiken oberhalb Schwelle | `[OFFEN]` | Definition: `[OFFEN]` | `[OFFEN]` |

---

## 5. Management Insights (deterministisch)

Backend-erkannte Auffälligkeiten — **keine KI-Ausgaben**. Regeln und Schwellenwerte: `[OFFEN]`.

| Insight-Typ (Kandidat) | Beschreibung | Regel / Berechnung | Schwellenwert |
|---|---|---|---|
| Budgetverbrauch > Fortschritt | Budget läuft schneller als Fortschritt | `[OFFEN]` | `[OFFEN]` |
| Fortschritt hinter Zeitverbrauch | Zeit verstrichen disproportioniert | `[OFFEN]` | `[OFFEN]` |
| Prognose Endtermin verschoben | Prognostiziertes Enddatum später als Plan | `[OFFEN]` | `[OFFEN]` |
| Meilenstein überfällig | Kritischer Meilenstein über Fälligkeit | `[OFFEN]` | `[OFFEN]` |
| Risiko-Cluster | Mehrere Risiken gleicher Themengruppe | `[OFFEN]` | `[OFFEN]` |
| Status verschlechtert | Ampel vs. vorheriger Berichtsstand | `[OFFEN]` | `[OFFEN]` |
| Stale Data | Keine Aktualisierung seit X Tagen | `[OFFEN]` | `[OFFEN]` |
| Widersprüchliche Signale | Ampel vs. Termin/Budget/Risiken | `[OFFEN]` | `[OFFEN]` |

---

## 6. Berichtsstandsvergleich

| KPI-Kandidat | Beschreibung | Formel / Berechnung | MVP-Datenmodell |
|---|---|---|---|
| Delta Fortschritt | Änderung seit vorherigem Berichtsstand | `[OFFEN]` | `[ASSUMPTION]` Snapshot-Tabelle |
| Delta Budgetverbrauch | Änderung Budget % seit Vorperiode | `[OFFEN]` | `[ASSUMPTION]` Snapshot-Tabelle |
| Delta Terminprognose | Verschiebung prognostiziertes Ende | `[OFFEN]` | `[ASSUMPTION]` Snapshot-Tabelle |
| Delta Ampelstatus | Statuswechsel seit Vorperiode | `[OFFEN]` | `[ASSUMPTION]` Snapshot-Tabelle |
| Delta Risiken | Anzahl/Schwere offener Risiken | `[OFFEN]` | `[ASSUMPTION]` Snapshot-Tabelle |

---

## 7. Gemini-Prognosen und Management-Zusammenfassungen

**Keine deterministischen KPIs.** Gemini analysiert ausgewählte, bereits berechnete und freigegebene Projektdaten. Alle Ausgaben sind als **KI-Prognose** oder **KI-Einschätzung** gekennzeichnet. Keine automatischen Projektentscheidungen. Keine erfundenen Daten.

| Ausgabe-Typ | Beschreibung | Eingabedaten | Speicherung | Datenschutz |
|-------------|--------------|--------------|-------------|-------------|
| Prognose zu möglichen Projektverzögerungen | KI-Einschätzung zu Terminrisiken | Berechnete Projektdaten: `[OFFEN]` | `[OFFEN]` | `[OFFEN]` |
| Einschätzung zu Budgetrisiken | KI-Einschätzung zu Budgetentwicklung | Berechnete Projektdaten: `[OFFEN]` | `[OFFEN]` | `[OFFEN]` |
| Einschätzung zum allgemeinen Projektrisiko | KI-Einschätzung zum Gesamtrisiko | Berechnete Projektdaten: `[OFFEN]` | `[OFFEN]` | `[OFFEN]` |
| Erkennung auffälliger Entwicklungen | Hinweise auf ungewöhnliche Muster | Berechnete Projektdaten: `[OFFEN]` | `[OFFEN]` | `[OFFEN]` |
| Zusammenfassung wichtiger Warnsignale | Kompakte Warnübersicht | Berechnete Projektdaten: `[OFFEN]` | `[OFFEN]` | `[OFFEN]` |
| Management-Zusammenfassung | Verständliche Gesamtzusammenfassung für Führungskräfte | Berechnete Projektdaten: `[OFFEN]` | `[OFFEN]` | `[OFFEN]` |
| Hinweise auf mögliche Gegenmaßnahmen | Vorschläge ohne verbindliche Wirkung | Berechnete Projektdaten: `[OFFEN]` | `[OFFEN]` | `[OFFEN]` |

**MVP-Hinweis (Pilot):** Es werden ausschließlich Mock-Daten verwendet. Keine echten Kunden- oder Projektdaten an Gemini.

**Verhalten bei API-Ausfall:** KPI-Funktionen bleiben verfügbar; im Prognosebereich erscheint eine verständliche Fehlermeldung.

---

## Offene Querschnittsthemen (für PRD)

- Priorisierung: Welche KPIs sind für Management und Projektleitung am wichtigsten? `[OFFEN]`
- Fachlicher Sign-off der KPI-Definitionen: `[OFFEN]`
- Qualitätsbewertung der Gemini-Prognosen: `[OFFEN]`
