---
title: "Product Brief: cgi-kpi-dashboard"
status: ready
created: 2026-07-13
updated: 2026-07-13
---

# Product Brief: cgi-kpi-dashboard

## Executive Summary

**cgi-kpi-dashboard** ist eine interne Greenfield-Webanwendung zur Steuerung und Analyse paralleler KI-Implementierungsprojekte bei externen Kunden. Das Unternehmen integriert KI-Lösungen in bestehende Geschäftsprozesse — dabei entstehen mehrere gleichzeitige Projekte mit unterschiedlichen Kunden, Phasen, Budgets, Aufwänden, Risiken und Fortschrittsständen.

Das Produkt ist ein **Management-Dashboard mit integrierter KI-Analyseschicht**. Gegenüber einem reinen KPI-Dashboard liefert es zusätzliche Interpretation, Prognose und Zusammenfassung deterministisch berechneter Projektdaten. Kennzahlen werden im Spring-Boot-Backend berechnet; die Gemini API ergänzt um KI-Einschätzungen — ohne KPI-Berechnung oder menschliche Entscheidungen zu ersetzen.

Der erste Release ist ein interner Pilot (Mock-Daten, ~20 Projekte, 2–5 Nutzer), der eine zentrale Produkthypothese prüft. Das Produkt ist kein Pflichtsystem, sondern ein Effizienz- und Entscheidungsunterstützungs-Tool. Der fachliche Auftraggeber ist **[OFFEN]**; voraussichtliche Verantwortung bei der Führungsebene für KI-Implementierungsprojekte.

## The Problem

**Die zentrale Produkthypothese lautet, dass Informationen zur Steuerung paralleler KI-Implementierungsprojekte nicht ausreichend zentral aufbereitet, projektübergreifend vergleichbar und frühzeitig interpretierbar sind. Der Pilot soll prüfen, ob ein gemeinsames KPI-Dashboard mit KI-gestützter Analyse die Entscheidungsgrundlage für Führungskräfte und Projektleiter verbessert.**

**[OFFENE ANNAHMEN — noch nicht validiert]** über die heutige Situation:

- Projektinformationen sind möglicherweise über verschiedene Systeme, Dateien und manuelle Auswertungen verteilt.
- Status, Fortschritt, Risiken, Budget und Aufwand lassen sich nur schwer vergleichen.
- Kritische Entwicklungen werden möglicherweise zu spät erkannt.
- Managementberichte müssen teilweise manuell erstellt werden.

**Konsequenz, falls die Hypothese zutrifft und ungelöst bleibt:** Entscheidungen basieren weiter auf manuellen Auswertungen und persönlichen Einschätzungen. Kein Betriebsausfall — aber eingeschränkte Qualität und Geschwindigkeit portfolioübergreifender Steuerung.

**Zehn Leitfragen:**

1. Welche Projekte sind aktuell aktiv?
2. Wie ist der Status der einzelnen Projekte?
3. In welcher Projektphase befinden sie sich?
4. Wie weit sind die Projekte fortgeschritten?
5. Welche Projekte sind gefährdet oder verspätet?
6. Wie entwickeln sich Budget und Aufwand im Vergleich zur Planung?
7. Welche offenen Risiken und kritischen Probleme bestehen?
8. Welche Projekte könnten sich voraussichtlich verzögern?
9. Bei welchen Projekten besteht ein mögliches Budgetrisiko?
10. Welche Auffälligkeiten lassen sich aus den Projektdaten erkennen?

## The Solution

Interne Webanwendung (Monorepo: Spring-Boot-Backend + Angular-Frontend) für ein simuliertes Projektportfolio — übersichtlich, filterbar, mit KI-Analyseschicht.

### Architekturprinzip: KPIs vs. KI

| Schicht | Verantwortung |
|---------|---------------|
| **Spring-Boot-Backend** | Deterministische KPI-Berechnung; REST-API; PostgreSQL |
| **Gemini API (serverseitig)** | Analyse freigegebener Projektdaten: Prognosen, Risikoeinschätzungen, Zusammenfassungen, Gegenmaßnahmen-Hinweise |
| **Angular-Frontend** | Darstellung, Filterung, Visualisierung (CGI EDS) — kein API-Key, keine KPI-Berechnung, kein direkter Gemini-Aufruf |

Gemini-Ausgaben sind als **KI-Prognose** oder **KI-Einschätzung** gekennzeichnet. Keine Auto-Entscheidungen, keine erfundenen Daten. Bei API-Ausfall: KPIs nutzbar, Prognosebereich zeigt Fehlermeldung.

### Nutzersichten (MVP-Priorität: Führungskraft)

| Nutzer | Sicht |
|--------|-------|
| **Führungskraft (primär)** | Portfolio-Überblick: Status, Risiken, Prognosen, Vergleichbarkeit |
| **Projektleiter (sekundär)** | Einzelprojekt-Detail: Abweichungen, Risiken, Budget/Aufwand Plan-Ist |

### Erster Einsatz

Greenfield, ausschließlich Mock-Daten (~20 Projekte: im Plan, verspätet, budgetkritisch, Risiken, widersprüchliche Signale, abgeschlossen). Keine echten Daten an Gemini. Interner Pilot: 2–5 Nutzer.

## What Makes This Different

Primär ein **Management- und Projektportfolio-Dashboard** — kein KI-Experiment, kein PM-Tool-Ersatz. Drei Differenzierungsmerkmale:

1. **Deterministische Kennzahlen** — KPIs im Backend, nicht von KI abgeleitet.
2. **Projektübergreifende Vergleichbarkeit** — einheitlicher Portfolio-Überblick mit Filtern.
3. **KI-Analyseschicht** — Gemini interpretiert freigegebene Daten (Prognosen, Warnsignale, Management-Zusammenfassungen).

Ohne Gemini wäre die Anwendung nutzbar, aber die Produkthypothese (Zusatznutzen durch KI) nicht prüfbar. Gemini ist MVP-Bestandteil, bleibt ergänzend.

Ob bestehende Werkzeuge (PM-Tools, Tabellen, Reporting) dieselben Fragen bereits beantworten: **[OFFEN]**.

## Who This Serves

**MVP-Fokus:** Führungskräfte (primär), Projektleiter (sekundär). Bei Konflikt hat die Führungskraft-Sicht Vorrang.

**Weitere voraussichtliche Nutzer:** Business Analysts, Delivery-Verantwortliche, Account-Verantwortliche.

**Spätere Nutzer (nicht MVP-Fokus):** Entwickler, Data Scientists, KI-Architekten, Controlling, Qualitätsmanagement.

## Success Criteria

Der Pilot prüft die Produkthypothese — nicht vollständige Produktreife. Konkrete Zielwerte: **[OFFEN]**.

- Führungskräfte identifizieren kritische Projekte schneller.
- Status, Budget, Aufwand und Risiken werden als verständlich bewertet.
- Gemini-Ausgaben sind nachvollziehbar und beziehen sich auf vorhandene Daten.
- KI-Zusammenfassungen liefern wahrgenommenen Zusatznutzen gegenüber reiner KPI-Anzeige.
- Dashboard bleibt bei Gemini-Ausfall nutzbar.
- Pilot liefert konkrete Anforderungen für reale Nutzung.

**Messung der Pilot-Evaluation:** `[OFFEN]`

## Scope

### MVP — enthalten

Projektübersicht, Status (Grün/Gelb/Rot), Fortschritt, Phase, Budget/Aufwand Plan-Ist, Terminabweichung, Risiken, kritische Probleme, Gemini-Prognosen, Management-Zusammenfassung, Filter (Kunde, Projekt, Zeitraum, Status), KPI-Karten, Projekttabelle, Diagramme, Risiko-/Prognosebereich.

### MVP — ausgeschlossen

Ressourcenplanung, Aufgaben-/Projektplanung im Dashboard, Auto-Änderung von Projektdaten durch Gemini, Auto-Entscheidungen, vollständige Rollen-/Rechteverwaltung, Kundenportal, Live-Monitoring produktiver KI-Systeme, Anbindung aller Kundensysteme, mobile App, komplexe Exportfunktionen.

### KPI-Inventar

Fünf Bereiche mit detaillierter Metrik-Liste: Projektportfolio, Projektstatus, Budget und Aufwand, Risiken und Probleme, Gemini-Prognosen. Formeln, Schwellenwerte und Datenquellen: **[OFFEN]**. Details: [addendum.md](addendum.md).

### Technische Grundrichtung

| Bereich | Technologie |
|---------|-------------|
| Backend | Java 21, Spring Boot 3.5.x, Maven, REST, Spring Data JPA, PostgreSQL, Flyway, Jakarta Bean Validation, Actuator |
| Tests | JUnit, Spring Boot Test |
| Frontend | Angular 20, TypeScript, SCSS, Angular Material, CGI EDS 19.0.0 |
| Struktur | Monorepo; KPI-Berechnung im Backend; Gemini-Key nur serverseitig |

## Vision

**Kurzfristig:** Demonstrator mit Mock-Daten, Validierung der Produkthypothese.

**Mittelfristig:** Reale Projektdaten, bestätigte KPI-Definitionen, Datenschutz-/Gemini-Richtlinien, erweiterte Nutzergruppen.

**Langfristig (2–3 Jahre):** Etabliertes internes Steuerungsinstrument für das KI-Projektportfolio. Umfang und Integrationstiefe: **[OFFEN]**, abhängig vom Pilot.

## Offene Punkte

**Fachlich & Daten:** Verfügbare Projektdaten, Quellsysteme, Berechnung von Status/Fortschritt, Projektphasen, Ampel-Schwellenwerte, KPI-Priorisierung, Sign-off der Definitionen.

**Organisatorisch:** Fachlicher Auftraggeber `[OFFEN]`; keine bestätigten Reporting-Templates oder Auftraggebervorgaben.

**Gemini & Datenschutz:** Übertragbare Daten, Anonymisierung, Speicherung von Ausgaben, Prognose-Qualität, interne Sicherheitsvorgaben.

**Technisch & Betrieb:** Demo auf Arbeitsrechner, Pilot-Evaluation `[OFFEN]`.

**Produkthypothese:** Ob die angenommenen Reporting-Probleme bestehen, klärt der Pilot.
