---
title: "PRD: cgi-kpi-dashboard"
status: final
created: 2026-07-13
updated: 2026-07-15
sources:
  - ../briefs/brief-cgi-kpi-dashboard-2026-07-13/brief.md
  - ../briefs/brief-cgi-kpi-dashboard-2026-07-13/addendum.md
---

# PRD: cgi-kpi-dashboard

## 0. Document Purpose

Dieses PRD definiert die Produktanforderungen für **cgi-kpi-dashboard** — ein internes Management-Dashboard mit KI-Analyseschicht zur Steuerung paralleler KI-Implementierungsprojekte. Zielgruppe: Product Owner, Entwicklung, UX und Stakeholder des internen Pilots.

**Quellen:** [Product Brief](../briefs/brief-cgi-kpi-dashboard-2026-07-13/brief.md), [KPI-Inventar-Addendum](../briefs/brief-cgi-kpi-dashboard-2026-07-13/addendum.md).

**Struktur:** Glossary-anchored Begriffe, Features mit global nummerierten FRs, `[ASSUMPTION]`/`[OFFEN]` inline und im Assumptions Index. Technische Implementierungsdetails → PRD-`addendum.md`.

## 1. Vision

**cgi-kpi-dashboard** ist ein internes Web-Dashboard, das Führungskräften und Projektleitern einen zentralen Überblick über ein Portfolio paralleler KI-Implementierungsprojekte bei externen Kunden gibt. Deterministisch berechnete Kennzahlen (Backend) und klar gekennzeichnete KI-Einschätzungen (Gemini) arbeiten zusammen: KPIs liefern verlässliche Fakten, die KI-Schicht hilft bei Interpretation, Priorisierung und Zusammenfassung — ohne KPIs zu ersetzen oder Entscheidungen zu automatisieren.

Der zentrale Nutzenmoment: *„Ich sehe sofort, welche Projekte kritisch oder auffällig sind, verstehe warum — und kann gezielt vertiefen."* Innerhalb von 30 Sekunden erkennt eine Führungskraft Portfolio-Gesundheit, kritische Projekte und zentrale KPIs (aktive Projekte, Ø-Fortschritt, Budgetabweichung, Termintreue, kritische Risiken, Ampelverteilung) — visuell getrennt von der **Portfolio-Trendanalyse** als KI-Einschätzung. Die **Projekt-Detailansicht** ermöglicht dieselbe Geschwindigkeit für die Einzelprojekt-Analyse: Stammdaten, Management-KPIs, deterministische **Management Insights**, Phasen/Meilensteine, Budget/Aufwand sowie getrennte Risiken und Probleme — ergänzt durch KI-Interpretation, nicht ersetzt.

Der erste Release ist ein **interner Pilot** mit Mock-Daten (~20 Projekte, 2–5 Nutzer), der eine Produkthypothese prüft: Ob ein gemeinsames KPI-Dashboard mit KI-gestützter Analyse die Entscheidungsgrundlage verbessert. Der Pilot ist gelungen, wenn Nutzer das Portfolio schneller verstehen, kritische Entwicklungen früher erkennen und Gemini-Ausgaben als nachvollziehbare Entscheidungsunterstützung bewerten. Er ist gescheitert, wenn kein besserer Überblick entsteht oder KI-Ausgaben als unverständlich, unzuverlässig oder nutzlos wahrgenommen werden. Konkrete Zielwerte und Messmethoden: `[OFFEN]`.

**KI-Umfang im MVP:** Portfolio-Trendanalyse (kompakter portfolio-weiter KI-Text inkl. Top-3-Handlungsbedarf), Management-Zusammenfassung pro Projekt, Prognose zu Verzögerungs- und Budgetrisiken sowie **projektbezogenes KI-Q&A** (vollständig Gemini-basiert). Portfolio-Trendanalyse ergänzt Projekt-Zusammenfassungen und Q&A, ersetzt sie nicht. Gemini verwendet bei Projekt-Q&A nur freigegebene Daten des ausgewählten Projekts; Portfolio-Trendanalyse nutzt freigegebene Portfolio-Daten. Top-3-Auswahl-Logik: `[OFFEN]`. Ausgeschlossen: Portfolio-Chatbot (Freitext-Q&A über alle Projekte), automatische Entscheidungen oder Datenänderungen, vollständig generierte Maßnahmenpläne, trainierte/selbstlernende Modelle.

## 2. Target User

### 2.1 Jobs To Be Done

**Führungskraft (primär)**

- Portfolio-Gesundheit auf einen Blick erfassen, ohne mehrere Quellen zu konsolidieren
- Kritische Projekte und Trends früh erkennen und Prioritäten setzen
- Vor Eskalationen oder Steering-Meetings schnell entscheidungsreif werden
- KI-Einschätzungen als Interpretationshilfe nutzen — nicht als Ersatz für eigene Bewertung

**Projektleiter (sekundär)**

- Status, Budget, Aufwand und Risiken des eigenen Projekts im Detail nachvollziehen
- Abweichungen gegenüber Plan verstehen und begründen können
- Gezielte Fragen an die KI stellen, um Ursachen und Handlungsfelder einzuordnen
- Portfolio-Kontext sehen, wenn nötig — Fokus bleibt auf dem eigenen Projekt

### 2.2 Non-Users (v1)

Nicht Zielgruppe des MVP-Pilots:

- Externe Kunden (kein Kundenportal)
- Entwickler, Data Scientists, KI-Architekten (kein technisches Monitoring produktiver KI-Systeme)
- Controlling und Qualitätsmanagement (kein vollständiges Reporting-/Audit-System)
- Nutzer, die Aufgaben- oder Ressourcenplanung im Dashboard erwarten

### 2.3 Key User Journeys

**UJ-1. Sabine prüft montags das Portfolio vor dem Steering.**

- **Persona + Kontext:** Sabine, Bereichsleitung, verantwortet mehrere parallele KI-Implementierungsprojekte. Montagmorgen, 15 Minuten vor dem Steering-Meeting.
- **Entry state:** Authentifizierung im MVP: `[OFFEN]` — Pilot vermutlich ohne vollständige Rollenverwaltung; Sabine öffnet die Portfolio-Startseite.
- **Path:** Sie sieht KPI-Karten (aktive Projekte, Ø-Fortschritt, Budgetabweichung, Termintreue, kritische Risiken, Ampelverteilung) und Trenddiagramme. Die **Management-Projekttabelle** zeigt vergleichbare Kennzahlen je Projekt (u. a. Projektleitung, Termin-/Budgetabweichung, Risiko-Zähler, letzte Aktualisierung) und ist sortier- und filterbar. Die **Portfolio-Trendanalyse** (ein kompakter KI-Text) fasst wichtige Entwicklungen zusammen. Sie filtert optional nach Ampelstatus, Kunde, Projektleitung oder Phase.
- **Climax:** Innerhalb von 30 Sekunden weiß sie, welche zwei Projekte sie im Meeting vertiefen muss — ohne Excel oder Einzelmails.
- **Resolution:** Sie notiert sich die Projektnamen, klickt optional in ein Projekt für die Management-Zusammenfassung, geht ins Meeting.
- **Edge case:** Gemini API nicht erreichbar — KPIs und Filter funktionieren; Portfolio-Trendanalyse zeigt verständliche Fehlermeldung.

**UJ-2. Markus vertieft ein kritisches Projekt und nutzt KI-Q&A.**

- **Persona + Kontext:** Markus, Projektleiter eines als „Gelb" eingestuften KI-Rollout-Projekts. Will vor dem Weekly mit dem Team die Lage einordnen.
- **Entry state:** Er kommt von der Portfolio-Übersicht oder direkt per Projektauswahl in die Projekt-Detailansicht.
- **Path:** Er sieht Projektstammdaten, Management-KPIs (Plan/Ist/Prognose getrennt), **Management Insights** (deterministische Auffälligkeiten mit Begründung), Phasen/Meilensteine, Budget/Aufwand, getrennte Listen für Risiken und Probleme sowie optional Entwicklung gegenüber dem vorherigen Berichtsstand. Er liest die Gemini-Management-Zusammenfassung, Erläuterungen auffälliger Entwicklungen und die Verzögerungs-/Budget-Prognose (KI-gekennzeichnet). Er nutzt Quick-Reply-Chips oder Freitext-Q&A.
- **Climax:** Die KI-Antworten beziehen sich erkennbar auf die angezeigten Projektdaten; Markus kann sie im Team-Meeting als Gesprächsgrundlage nutzen — nicht als verbindliche Anweisung.
- **Resolution:** Er bereitet das Weekly vor; keine Projektdaten wurden durch Gemini verändert.
- **Edge case:** Frage außerhalb der freigegebenen Daten — Gemini antwortet, dass keine ausreichende Datengrundlage vorliegt (keine erfundenen Werte).

## 3. Glossary

- **Portfolio** — Gesamtheit der im Dashboard geführten KI-Implementierungsprojekte. Ein Portfolio enthält mehrere Projekte.
- **Projekt** — Ein KI-Implementierungsprojekt bei einem externen Kunden mit Plan-/Ist-Daten zu Status, Phase, Fortschritt, Budget, Aufwand, Terminen, Risiken und Problemen. Enthält Stammdaten (u. a. Projektleitung, Kunde/Geschäftsbereich).
- **Projektleitung** — Verantwortliche Person oder Rolle für die operative Projektsteuerung. Darstellung im MVP: `[OFFEN]` (Name vs. Rolle vs. Platzhalter in Mock-Daten).
- **Geschäftsbereich** — Optionale fachliche Zuordnung neben Kunde; im MVP synonym zu Kunde nutzbar, sofern nicht getrennt modelliert `[ASSUMPTION]`.
- **Berichtsstand** — Zeitpunkt bzw. Periode der zugrunde liegenden Projektdaten (aktueller vs. vorheriger Stand). MVP-Historisierung: `[ASSUMPTION]` — siehe FR-21.
- **Management Insight** — Deterministisch im Backend festgestellte Auffälligkeit mit klarer Aussage, zugrunde liegenden Kennzahlen und nachvollziehbarer Begründung — keine KI-Einschätzung.
- **Prognostiziertes Enddatum** — Deterministisch berechnetes voraussichtliches Projektende, sofern aus vorhandenen Daten ableitbar. Berechnungslogik: `[OFFEN]`.
- **Deterministische Hochrechnung** — Backend-KPI für Restwert oder Endwert (Budget/Aufwand/Termin) auf Basis fester Regeln — nicht von Gemini erzeugt.
- **KI-Implementierungsprojekt** — Bezeichnet dasselbe wie **Projekt**: Einführung einer KI-Lösung in einen bestehenden Geschäftsprozess.
- **Pilot** — Erster interner Release mit Mock-Daten und 2–5 Nutzern zur Prüfung der Produkthypothese.
- **Mock-Daten** — Simulierte Projektdaten (~20 Projekte) ohne echte Kunden- oder Projektdaten; einzige Datenquelle im MVP.
- **KPI** — Deterministisch im Backend berechnete Kennzahl. KPIs sind verbindliche Faktenbasis der Darstellung, nicht von Gemini abgeleitet.
- **Ampelstatus** — Projektstatus als Grün, Gelb oder Rot. Berechnungslogik und Schwellenwerte: `[OFFEN]`.
- **Projektphase** — Aktuelle Phase eines Projekts im Implementierungszyklus. Phasenmodell: `[OFFEN]`.
- **Projektfortschritt** — Fortschritt eines Projekts in Prozent gegenüber Plan. Berechnung: `[OFFEN]`.
- **Terminabweichung** — Abweichung des Projekts vom geplanten Endtermin. Berechnung: `[OFFEN]`.
- **Budget Plan/Ist** — Geplantes vs. tatsächliches Projektbudget inkl. Restbudget und Verbrauch in Prozent. Berechnung: `[OFFEN]`.
- **Aufwand Plan/Ist** — Geplante vs. verbrauchte Personentage inkl. verbleibendem Aufwand und Plan-Ist-Abweichung. Berechnung: `[OFFEN]`.
- **Risiko** — Erfasstes, noch nicht eingetretenes Projektrisiko mit Status offen/geschlossen. Pflichtfelder MVP: siehe FR-6. Wahrscheinlichkeit, Auswirkung, Schweregrad: `[OFFEN]` (Skalen/Formeln).
- **Problem** — Bereits eingetretenes oder unmittelbar wirksames Projektproblem — getrennt von **Risiko**. Pflichtfelder MVP: siehe FR-6. Definition „kritisch": `[OFFEN]`.
- **Kritisches Problem** — Erfasstes Problem mit höchster Relevanz für die Projektsteuerung. Definition „kritisch": `[OFFEN]`.
- **Freigegebene Projektdaten** — Berechnete KPIs und zugehörige Projektfelder, die für die Gemini-Analyseschicht freigegeben sind. Umfang im MVP (Mock): `[OFFEN]`; im Pilot keine echten Kundendaten.
- **Gemini-Analyseschicht** — Serverseitige KI-Funktionen über die Gemini API; ergänzt KPIs um Interpretation und Prognose.
- **KI-Einschätzung** — Von Gemini erzeugte textuelle Bewertung oder Antwort, visuell als KI gekennzeichnet; keine verbindliche Entscheidung.
- **KI-Prognose** — Von Gemini erzeugte Einschätzung zu zukünftiger Entwicklung (z. B. Verzögerungs- oder Budgetrisiko), als KI gekennzeichnet.
- **Management-Zusammenfassung** — KI-Einschätzung mit verständlicher Gesamtzusammenfassung eines einzelnen Projekts für Führungskräfte. Ergänzt die Portfolio-Trendanalyse, ersetzt sie nicht.
- **Portfolio-Trendanalyse** — Kompakter, portfolio-weiter KI-Text von Gemini: fasst wichtigste Entwicklungen zusammen, nennt auffällige Budget-, Termin- und Risikotrends und hebt die drei Projekte mit höchstem Handlungsbedarf (Top 3) hervor. Ergänzt Management-Zusammenfassungen und Projekt-KI-Q&A, ersetzt sie nicht. Auswahl-Logik Top 3: `[OFFEN]`.
- **Projekt-KI-Q&A** — Gemini-basierte Beantwortung von Fragen zum aktuell ausgewählten Projekt (Chips, vordefinierte Fragen, Freitext).
- **Quick-Reply-Chip** — Vordefinierte Bedienhilfe im Projekt-KI-Q&A; sendet eine Anfrage an Gemini, keine eigene Antwortlogik.

## 4. Features

### 4.1 Portfolio-Übersicht

**Description:** Startseite für Führungskräfte mit Portfolio-Gesundheit auf einen Blick. Realisiert UJ-1. Zeigt deterministische KPIs und KI-Einschätzungen in klar getrennten Bereichen.

**Functional Requirements:**

#### FR-1: Portfolio-KPI-Karten

Eine Führungskraft kann auf der Portfolio-Startseite zentrale KPI-Karten einsehen: aktive Projekte, durchschnittlicher Projektfortschritt, Budgetabweichung (aggregiert), Termintreue, Anzahl kritischer Risiken und Ampelstatus-Verteilung (Grün/Gelb/Rot). Realisiert UJ-1.

**Consequences (testable):**
- Alle genannten KPI-Karten zeigen Werte aus dem Backend, nicht von Gemini.
- Bei leerem Portfolio zeigen Karten einen definierten Leerzustand `[OFFEN: UX-Text]`.
- Aggregationslogik für Portfolio-KPIs: `[OFFEN]`.

#### FR-2: Portfolio-Projekttabelle

Eine Führungskraft kann alle Projekte des Portfolios in einer **vergleichbaren Management-Tabelle** einsehen. Mindestspalten: Projektname, Kunde oder Geschäftsbereich, Projektleitung, Ampelstatus, aktuelle Projektphase, Fortschritt in Prozent, geplantes Enddatum, prognostiziertes Enddatum (sofern deterministisch berechenbar), Terminabweichung in Tagen, Budgetverbrauch in Prozent, Budgetabweichung bzw. prognostizierter Endwert (sofern berechenbar), Aufwandsabweichung, Anzahl offener Risiken, Anzahl kritischer Risiken oder Probleme, letzte Datenaktualisierung. Realisiert UJ-1.

**Consequences (testable):**
- Tabellenspalten sind mindestens sortierbar nach: Ampelstatus, Fortschritt, Terminabweichung, Budgetabweichung, Anzahl kritischer Risiken, letzter Aktualisierung. Weitere sortierbare Spalten: `[OFFEN]`.
- Klick auf eine Zeile öffnet die Projekt-Detailansicht (→ FR-5).
- Prognostizierte Endwerte stammen aus Backend-Berechnung (→ FR-9), nicht aus Gemini.

#### FR-3: Portfolio-Diagramme

Eine Führungskraft kann Diagramme zu Statusverteilung, Budgetentwicklung und Projektfortschrittstrend auf Portfolio-Ebene einsehen. Realisiert UJ-1.

**Consequences (testable):**
- Diagrammdaten stammen aus Backend-Berechnungen.
- Mindestens ein Trenddiagramm für Fortschritt und eines für Budget ist sichtbar.

#### FR-4: Portfolio-Trendanalyse

Eine Führungskraft kann eine **Portfolio-Trendanalyse** als kompakten, portfolio-weiten KI-Text einsehen. Der Text fasst die wichtigsten Entwicklungen im gesamten Portfolio zusammen, nennt auffällige Budget-, Termin- und Risikotrends und hebt die drei Projekte mit höchstem Handlungsbedarf (Top 3) hervor. Realisiert UJ-1.

**Consequences (testable):**
- Ausgabe ist als KI-Einschätzung gekennzeichnet und bleibt kompakt (Zielumfang `[OFFEN: z. B. max. 300 Wörter]`).
- Top 3 nennt genau drei Projekte mit Kurzbegründung je Projekt.
- Text basiert auf freigegebenen Portfolio-Daten (berechnete KPIs), nicht auf erfundenen Werten.
- Ergänzt Management-Zusammenfassungen (→ FR-11) und Projekt-KI-Q&A (→ FR-16); ersetzt weder Projekt-Detailansicht noch Q&A.
- Bei Gemini-Ausfall zeigt der Bereich eine verständliche Fehlermeldung; KPI-Bereiche bleiben nutzbar (→ FR-15).
- Top-3-Auswahl-Logik (rein KI vs. deterministische Vorfilterung): `[OFFEN]`.

---

### 4.2 Projekt-Detailansicht

**Description:** Detailansicht für Projektleiter und Führungskräfte zu einem einzelnen Projekt. Realisiert UJ-2.

**Functional Requirements:**

#### FR-5: Projekt-Kernkennzahlen und Stammdaten

Ein Nutzer kann in der Projekt-Detailansicht **Projektstammdaten** einsehen: Projektname und Projekt-ID, Kunde oder Geschäftsbereich, Projektleitung, Startdatum, geplantes Enddatum, prognostiziertes Enddatum (sofern berechenbar), aktuelle Phase, Ampelstatus, Zeitpunkt der letzten Datenaktualisierung.

Ein Nutzer kann **Management-KPIs** einsehen: Projektfortschritt, Zeitverbrauch bzw. vergangene Projektdauer, Terminabweichung, Budget Plan/Ist und Verbrauch in Prozent, Budgetabweichung, Aufwand Plan/Ist und Abweichung, Restbudget und Restaufwand (sofern verfügbar), Anzahl offener und kritischer Risiken, Anzahl offener kritischer Probleme. Plan-, Ist- und Prognosewerte sind eindeutig unterscheidbar. Realisiert UJ-2.

**Consequences (testable):**
- Alle KPI-Werte werden vom Backend berechnet und angezeigt (→ FR-9).
- Berechnungsformeln: `[OFFEN]` (siehe KPI-Inventar-Addendum).
- Auffällige Zusammenhänge (z. B. Budgetverbrauch > Fortschritt, Zeitverbrauch > Fortschritt, überfällige Meilensteine, widersprüchlicher Ampelstatus) werden als **Management Insights** dargestellt (→ FR-20), nicht nur implizit in Einzel-KPIs.

#### FR-6: Risiken und Probleme (getrennt)

Ein Nutzer kann **Risiken** und **Probleme** getrennt einsehen. Das Dashboard zeigt diese Informationen an, ersetzt aber kein vollständiges Risiko- oder Maßnahmenmanagementsystem.

**Risiken** — Mindestfelder: Titel, Beschreibung, Eintrittswahrscheinlichkeit, Auswirkung, Schweregrad, Status, verantwortliche Rolle oder Person, Gegenmaßnahme, Fälligkeit. Skalen und Pflichtfelder im MVP: `[OFFEN]`.

**Probleme** — Mindestfelder: Titel, Beschreibung, Schweregrad, Status, Verantwortlichkeit, Zieltermin, aktuelle Gegenmaßnahme.

Realisiert UJ-2.

**Consequences (testable):**
- Risiken und Probleme erscheinen in getrennten Listen oder klar getrennten Bereichen.
- Anzahl offener Risiken und kritischer Probleme ist in Tabelle (→ FR-2) und Detail-KPIs (→ FR-5) sichtbar.
- Datenstruktur und Pflichtfelder im Domain-Modell: Architecture/Story-Ebene; fachliche Schwellenwerte `[OFFEN]`.

#### FR-7: Navigation Portfolio ↔ Projekt

Ein Nutzer kann von der Portfolio-Übersicht in die Projekt-Detailansicht wechseln und zurück zur Portfolio-Übersicht navigieren.

**Consequences (testable):**
- Zurück-Navigation erhält den zuletzt gesetzten Filterzustand `[ASSUMPTION: Filter-Persistenz]`.

---

### 4.3 Filter

**Description:** Portfolio eingrenzen für schnellere Analyse. Realisiert UJ-1.

**Functional Requirements:**

#### FR-8: Portfolio-Filter

Eine Führungskraft kann das Portfolio filtern nach: Kunde oder Geschäftsbereich, Projektleitung, Ampelstatus, Projektphase, aktiv oder abgeschlossen, Zeitraum, Risikostufe. Zusätzlich bleibt die Projektsuche nach Name `[ASSUMPTION]` aus dem MVP-Filter „Projekt" erhalten. Realisiert UJ-1.

**Consequences (testable):**
- KPI-Karten, Tabelle, Diagramme und Portfolio-Trendanalyse aktualisieren sich konsistent auf den gefilterten Datensatz `[ASSUMPTION]`.
- Definition „Zeitraum" (z. B. Berichtsmonat, Start-/Enddatum): `[OFFEN]`.
- Definition „Risikostufe" (Filterlogik): `[OFFEN]`.
- Mehrfachfilter kombinierbar `[ASSUMPTION]`.

---

### 4.4 Deterministische KPI-Berechnung

**Description:** Alle fachlichen Kennzahlen werden ausschließlich im Backend berechnet. Gemini berechnet keine KPIs.

**Functional Requirements:**

#### FR-9: Backend-KPI-Berechnung

Das System berechnet alle im KPI-Inventar definierten KPI-Kandidaten deterministisch im Backend und stellt sie über eine REST-API bereit.

**Consequences (testable):**
- Kein KPI-Wert im Frontend oder in Gemini-Antworten wird ohne Backend-Berechnung als Fakt dargestellt.
- KPI-Katalog entspricht den Bereichen im Brief-Addendum (Portfolio, Status, Budget/Aufwand, Risiken/Probleme, **Management Insights**, **Berichtsvergleich**); Formeln `[OFFEN]`.
- Gemini API-Key ist nicht im Frontend und nicht im Repository gespeichert `[ASSUMPTION: aus Brief]`.

#### FR-10: KPI-/KI-Trennung in der Darstellung

Das System trennt deterministische KPIs und KI-Einschätzungen visuell und semantisch in allen Ansichten.

**Consequences (testable):**
- KPI-Bereiche und KI-Bereiche sind für Nutzer unterscheidbar (Label, Layout oder beides) `[OFFEN: UX-Spezifikation]`.
- Fehlschlag laut Vision (vermischte Ebenen) ist in UX-Review explizit auszuschließen.

---

### 4.5 Gemini-Analyseschicht

**Description:** Serverseitige KI-Funktionen auf Basis freigegebener Projektdaten. Realisiert UJ-1, UJ-2.

**Functional Requirements:**

#### FR-11: Management-Zusammenfassung pro Projekt

Ein Nutzer kann in der Projekt-Detailansicht eine Management-Zusammenfassung als KI-Einschätzung einsehen. Gemini **interpretiert** die freigegebenen deterministischen Daten (KPIs, Management Insights, Risiken, Meilensteine) — ersetzt sie nicht. Realisiert UJ-2.

**Consequences (testable):**
- Zusammenfassung bezieht sich nur auf freigegebene Projektdaten des angezeigten Projekts (→ FR-14).
- Ausgabe ist als KI-Einschätzung gekennzeichnet.
- Zusammenfassung nennt die wichtigsten zugrunde liegenden Fakten in Worten; erfindet keine KPI-Werte, Ursachen oder Verantwortlichkeiten.
- Keine verbindlichen oder vollständig ausgearbeiteten Maßnahmenpläne (→ §5 Non-Goals).

#### FR-12: Verzögerungs- und Budget-Prognose (KI)

Ein Nutzer kann pro Projekt eine **KI-Prognose** zu Verzögerungsrisiko und Budgetrisiko einsehen. Gemini darf deterministische Hochrechnungen und Management Insights **erläutern**, darf sie aber nicht als verbindlichen KPI selbst erfinden oder ersetzen. Realisiert UJ-2.

**Consequences (testable):**
- Beide Prognosen sind als KI-Prognose gekennzeichnet.
- Prognosen ersetzen keine Ampelstatus-, Budget- oder Termin-KPIs (→ FR-5, FR-9).
- Bei unzureichender Datengrundlage: Hinweis ohne erfundene Werte (→ FR-14).

#### FR-13: Keine KI-seitigen Datenänderungen oder Entscheidungen

Das System löst durch Gemini-Ausgaben keine automatischen Projektentscheidungen aus und ändert keine Projektdaten.

**Consequences (testable):**
- Kein Gemini-Endpunkt schreibt in die Projektdatenbank.
- Keine UI-Aktion aus KI-Ausgaben heraus ändert Projektstatus, Budget oder Risiken ohne expliziten Nutzer-Workflow außerhalb des MVP `[NON-GOAL: Auto-Änderung]`.

#### FR-14: Keine erfundenen Daten durch Gemini

Wenn Gemini nicht genügend freigegebene Daten hat, antwortet es mit einem Hinweis auf fehlende Datengrundlage — ohne Werte zu erfinden. Realisiert UJ-2 Edge case.

**Consequences (testable):**
- Antwort enthält keinen erfundenen KPI-Wert.
- Verhalten ist in Mock-Szenarien mit unvollständigen Daten testbar `[OFFEN: welches Mock-Projekt]`.

#### FR-15: Graceful Degradation bei Gemini-Ausfall

Wenn die Gemini API nicht erreichbar ist, bleiben alle KPI-Funktionen nutzbar; KI-Bereiche zeigen eine verständliche Fehlermeldung. Realisiert UJ-1 Edge case.

**Consequences (testable):**
- Portfolio-KPIs, Tabelle, Filter und Projekt-Detail-KPIs laden erfolgreich.
- KI-Bereiche (Portfolio-Trendanalyse, Zusammenfassung, Prognose, Q&A) zeigen Fehlermeldung, kein leerer Bereich ohne Hinweis.

---

### 4.6 Projekt-KI-Q&A

**Description:** Vollständig Gemini-basiertes Q&A zum ausgewählten Projekt. Realisiert UJ-2.

**Functional Requirements:**

#### FR-16: Projekt-KI-Q&A (Freitext und vordefiniert)

Ein Nutzer kann in der Projekt-Detailansicht Fragen zum aktuell ausgewählten Projekt stellen — per Freitext oder vordefinierter Frage. Alle Anfragen werden an Gemini gesendet; Antworten sind KI-Einschätzungen. Gemini interpretiert freigegebene KPIs, Management Insights, Risiken und Meilensteine; erfindet keine Werte oder Verantwortlichkeiten. Unverbindliche Hinweise auf Prüf- oder Handlungsfelder sind erlaubt; keine verbindlichen Maßnahmenpläne. Realisiert UJ-2.

**Consequences (testable):**
- Antwort bezieht sich nur auf freigegebene Projektdaten dieses Projekts.
- Kein Q&A über das gesamte Portfolio (kein Portfolio-Chatbot).
- Mindestens drei vordefinierte Beispielfragen sind verfügbar (z. B. kritische Bewertung, Verzögerungsrisiko, Risiko-Priorisierung).
- Bei unzureichender Datengrundlage: expliziter Hinweis (→ FR-14).

#### FR-17: Quick-Reply-Chips

Ein Nutzer kann Quick-Reply-Chips nutzen, die eine vorformulierte Frage an Gemini senden. Chips haben keine eigene Antwortlogik. Realisiert UJ-2.

**Consequences (testable):**
- Chip-Klick löst dieselbe Gemini-Anfrage-Pipeline aus wie Freitext.
- Chip-Texte sind als KI-Anfrage erkennbar, Antwort als KI-Einschätzung gekennzeichnet.

#### FR-18: Kein Portfolio-Q&A

Ein Nutzer kann im MVP keine Gemini-Fragen stellen, die Daten mehrerer Projekte oder des gesamten Portfolios gleichzeitig auswerten.

**Consequences (testable):**
- Q&A-UI ist nur in der Projekt-Detailansicht verfügbar, nicht auf Portfolio-Startseite.

---

### 4.7 Mock-Daten (Pilot)

**Description:** Demonstrator mit simuliertem Portfolio für den internen Pilot.

**Functional Requirements:**

#### FR-19: Mock-Portfolio

Das System stellt ein Mock-Portfolio mit ca. 20 parallelen Projekten bereit, die unterschiedliche Situationen abbilden: im Plan, Terminverzug, erhöhter Budgetverbrauch, offene Risiken, widersprüchliche Signale, abgeschlossene Projekte. Seed-Daten decken erweiterte Tabellen- und Detailfelder sowie Insight-Szenarien ab, sobald Domain-Erweiterung umgesetzt ist (Architecture/Stories).

**Consequences (testable):**
- Mindestens ein Projekt pro Szenario-Typ ist im Datensatz vorhanden.
- Keine echten Kunden- oder Projektdaten; keine echten Daten werden an Gemini übertragen.
- Authentifizierung und Rollenverwaltung: `[OFFEN]` — MVP ohne vollständige Rollenverwaltung `[ASSUMPTION: aus Brief]`.

**Notes:** Demo auf Arbeitsrechner — Betriebsanforderung `[OFFEN]`, Details → PRD-Addendum bei Finalize.

---

### 4.8 Management Insights und Berichtsvergleich

**Description:** Deterministische Auffälligkeitserkennung und zeitlicher Vergleich für nachvollziehbare Einzelprojekt-Analyse. Ergänzt FR-5; KI interpretiert Insights (→ FR-11..FR-16), ersetzt sie nicht.

**Functional Requirements:**

#### FR-20: Deterministische Management Insights

Ein Nutzer kann pro Projekt einen Bereich **Management Insights** einsehen. Das Backend stellt festgestellte Auffälligkeiten deterministisch bereit, z. B.: Budgetverbrauch übersteigt Projektfortschritt; Fortschritt liegt hinter Zeitverbrauch; Endtermin wurde nach hinten prognostiziert; kritischer Meilenstein überfällig; mehrere Risiken betreffen denselben Projektbereich; Status hat sich seit letztem Berichtsstand verschlechtert; Daten seit längerer Zeit nicht aktualisiert; Ampelstatus und Kennzahlen ergeben widersprüchliche Signale. Konkrete Regeln und Schwellenwerte: `[OFFEN]`.

**Consequences (testable):**
- Jeder Insight zeigt mindestens: klare Aussage, zugrunde liegende Kennzahlen, Vergleichswert oder Schwellenwert `[OFFEN]`, betroffener Zeitraum, nachvollziehbare Begründung, Kennzeichnung als **deterministischer Hinweis** (nicht KI).
- Insights entstehen in `kpi.*` (→ FR-9, AD-3).
- Gemini darf Insights in Textform referenzieren, darf sie nicht erfinden (→ FR-14).

#### FR-21: Projektentwicklung und Berichtsstandsvergleich

Ein Nutzer kann die **zeitliche Entwicklung** eines Projekts gegenüber dem vorherigen Berichtsstand einsehen, sofern Daten vorhanden sind: Entwicklung von Fortschritt, Budgetverbrauch, Terminprognose, Ampelstatus, Risiken; aktueller vs. vorheriger Berichtsstand; Datum der letzten Aktualisierung.

**Consequences (testable):**
- Vergleichswerte sind deterministisch berechnet (→ FR-9).
- MVP-Datenmodell: `[ASSUMPTION]` — leichtgewichtige Berichtsstand-Snapshots (siehe Architecture Spine); keine vollständige Historisierung aller Felder.
- Fehlen historische Daten, zeigt die UI einen definierten Hinweis — kein erfundener Vergleich.

---

### 4.9 Constraints, Guardrails und Data Governance

**Description:** Querschnittliche Regeln für KI-Nutzung, Datenschutz und Pilot-Betrieb.

**Constraints:**

- Gemini API-Key nur serverseitig; nicht im Frontend oder Git-Repository.
- MVP nutzt ausschließlich Mock-Daten; keine echten Kunden- oder Projektdaten an Gemini.
- Alle Gemini-Ausgaben als KI-Einschätzung oder KI-Prognose gekennzeichnet.
- Keine automatischen Projektentscheidungen oder Datenänderungen durch Gemini.

**Data Governance (Pilot):**

- Freigegebene Projektdaten für Gemini: Umfang `[OFFEN]`.
- Anonymisierung bei späterer realer Nutzung: `[OFFEN]`.
- Speicherung von Gemini-Ausgaben: `[OFFEN]`.
- Interne Datenschutz- und Sicherheitsvorgaben: `[OFFEN]`.

**Feature-specific NFRs:**

- Portfolio-Startseite mit KPI-Karten muss innerhalb von 30 Sekunden den Aha-Moment ermöglichen `[ASSUMPTION: aus Vision]` — konkrete Ladezeit `[OFFEN]`.
- Authentifizierung und Rollenverwaltung im MVP: `[OFFEN]`.

## 5. Non-Goals (Explicit)

- Vollständige Ressourcenplanung im Dashboard
- Aufgaben- oder Projektplanung im Dashboard
- Ersatz für bestehende PM-Tools oder Reporting-Systeme
- Portfolio-Freitext-Chatbot (Q&A nur projektbezogen)
- Automatische Änderung von Projektdaten durch Gemini
- Automatische verbindliche Projektentscheidungen
- Vollständige Rollen- und Rechteverwaltung
- Kundenportal für externe Auftraggeber
- Live-Monitoring produktiver KI-Systeme
- Automatische Anbindung aller Kundensysteme
- Mobile App
- Komplexe Exportfunktionen
- Vollständig generierte Maßnahmenpläne durch KI
- Trainierte oder selbstlernende Modelle

## 6. MVP Scope

### 6.1 In Scope

Portfolio-Übersicht (KPI-Karten, erweiterte Management-Tabelle, Diagramme, Filter, Portfolio-Trendanalyse), Projekt-Detail (Stammdaten, Management-KPIs mit Plan/Ist/Prognose, Management Insights, Phasen/Meilensteine, Budget/Aufwand, getrennte Risiken/Probleme, Berichtsstandsvergleich), Backend-KPI-Berechnung, Gemini (Zusammenfassung, Prognose, Trendanalyse, Projekt-Q&A), Mock-Portfolio (~20 Projekte), Pilot (2–5 Nutzer), Degradation bei Gemini-Ausfall. Details: §4 Features.

### 6.2 Out of Scope for MVP

Siehe §5 Non-Goals. Zusätzlich deferred:

- Reale Datenquellen-Anbindung → nach erfolgreichem Pilot
- Bestätigte KPI-Definitionen mit fachlichem Sign-off → Pilot-Ergebnis
- Vollständige Auth/Rollen-Lösung → `[OFFEN]`, vermutlich vereinfachter Pilot-Zugang

## 7. Success Metrics

Der Pilot prüft die Produkthypothese. Konkrete Zielwerte und Messmethoden: `[OFFEN]`.

**Primary**

- **SM-1:** Führungskräfte identifizieren kritische Projekte schneller als ohne Dashboard. Validated by FR-1, FR-2, FR-4, FR-8, FR-20. Ziel: `[OFFEN]`.
- **SM-2:** Nutzer bewerten Projektstatus, Budget, Aufwand und Risiken als verständlich. Validated by FR-1, FR-5, FR-6, FR-10, FR-20, FR-21. Ziel: `[OFFEN]`.
- **SM-3:** Gemini-Ausgaben sind nachvollziehbar und beziehen sich erkennbar auf vorhandene Daten. Validated by FR-4, FR-10, FR-11, FR-12, FR-14, FR-16. Ziel: `[OFFEN]`.

**Secondary**

- **SM-4:** KI-Zusammenfassungen und Portfolio-Trendanalyse liefern wahrgenommenen Zusatznutzen gegenüber reiner KPI-Anzeige. Validated by FR-4, FR-11, FR-12, FR-16. Ziel: `[OFFEN]`.
- **SM-5:** Dashboard bleibt bei Gemini-Ausfall für KPI-Nutzung funktionsfähig. Validated by FR-15. Ziel: 100 % KPI-Verfügbarkeit bei simuliertem API-Ausfall.
- **SM-6:** Pilot liefert konkrete Anforderungen für reale Nutzung (Datenquellen, KPI-Definitionen, Datenschutz, Rollen). Validated by Pilot-Retrospektive. Methode: `[OFFEN]`.

**Counter-metrics (do not optimize)**

- **SM-C1:** Anzahl der Gemini-Anfragen pro Sitzung — hohe Nutzung allein ist kein Erfolg, wenn Entscheidungsqualität nicht steigt.
- **SM-C2:** Anzahl der sichtbaren KPI-Karten/Metriken — mehr Zahlen dürfen den Überblick nicht verschlechtern (Fehlschlag laut Vision).

## 8. Open Questions

1. Welche konkreten Projektdaten stehen für eine spätere reale Nutzung zur Verfügung?
2. Aus welchen Systemen kommen die Daten?
3. Wie werden Ampelstatus, Projektfortschritt und Portfolio-Aggregationen berechnet?
4. Welches Projektphasen-Modell gilt?
5. Welche Schwellenwerte führen zu Grün, Gelb, Rot?
6. Welche KPIs sind für Management und Projektleitung am wichtigsten?
7. Wer ist der fachliche Auftraggeber (Person/Rolle)?
8. Wer bestätigt die KPI-Definitionen?
9. Welche Daten dürfen an Gemini übertragen werden (Umfang freigegebener Daten)?
10. Müssen Kundennamen oder Projektdetails anonymisiert werden?
11. Welche Gemini-Ausgaben sollen gespeichert werden?
12. Wie wird die Qualität der Prognosen bewertet?
13. Welche internen Datenschutz- und Sicherheitsvorgaben gelten?
14. Wie wird die Anwendung auf einem Arbeitsrechner demonstriert?
15. Art und Umfang der Pilot-Evaluation?
16. Top-3-Auswahl in der Portfolio-Trendanalyse: rein KI oder mit deterministischer Vorfilterung?
17. Authentifizierung und Zugangskontrolle im MVP?
18. Maximale Länge / Format der Portfolio-Trendanalyse?
19. Ob die Produkthypothese zur heutigen Reporting-Situation zutrifft (Pilot-Validierung)?
20. MVP-Historisierung: reichen zwei Berichtsstand-Snapshots pro Projekt aus?
21. Welche Management-Insight-Regeln und Schwellenwerte gelten fachlich?
22. Projektleitung: Personenname, Rolle oder beides in Mock-Daten?

## 9. Assumptions Index

- **§2.3 UJ-1:** Authentifizierung im MVP vereinfacht oder offen — Pilot ohne vollständige Rollenverwaltung.
- **§4.2 FR-7:** Filterzustand bleibt bei Zurück-Navigation aus Portfolio-Detail erhalten.
- **§4.3 FR-8:** Mehrfachfilter kombinierbar; gefilterter Datensatz gilt auch für Portfolio-Trendanalyse.
- **§4.4 FR-9:** Gemini API-Key nur serverseitig, nicht im Repository (aus Brief).
- **§4.7 FR-19:** MVP ohne vollständige Rollenverwaltung (aus Brief).
- **§4.8:** Portfolio-Startseite ermöglicht Aha-Moment innerhalb von 30 Sekunden (aus Vision).
