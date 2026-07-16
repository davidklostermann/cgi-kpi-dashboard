# Technische und fachliche Spezifikation

## 1. Zweck

Der Projekt-Assistent unterstützt Projektleitung und Management bei der Interpretation
bereits vorhandener, freigegebener Projektdaten. Er ersetzt weder die deterministische
Faktenlogik noch eine Fach- oder Steering-Entscheidung.

## 2. Zulässige Funktionen

- Projektsituation zusammenfassen
- belegte Hauptursachen und Auswirkungen darstellen
- Aufmerksamkeitspunkte priorisieren
- Maßnahmenoptionen als Vorschläge formulieren
- fehlende Daten nennen
- faktengebundene Fragen beantworten
- einen bearbeitbaren Entwurf für Statusbericht, Maßnahme oder Steering-Vorlage vorbereiten

## 3. Nicht zulässige Funktionen

- KPIs neu berechnen
- neue Projektfakten erzeugen
- Entscheidungen treffen
- Budget genehmigen
- Maßnahmen automatisch speichern, zuweisen oder versenden
- nicht belegte Owner oder Fristen erfinden
- Webwissen in Projektantworten einmischen
- API-Key an Angular ausliefern

## 4. Empfohlenes Datenmodell

### ProjectAiFact

```text
factId: string
category: KPI | BUDGET | INSIGHT | REPORT_DELTA | RISK | PROBLEM |
          ACTION | CAPACITY | PHASE | MILESTONE | MISSING_DATA
label: string
value: scalar | object
displayValue: string
sourceEntityType: string
sourceEntityId: string | null
factsAsOf: instant/date
detailRouteOrAnchor: string | null
```

`displayValue` dient nur der Darstellung. Fachlich relevante Rohwerte bleiben typisiert.

### ProjectAiAnalysisResponse

```text
projectId
factsAsOf
generatedAt
status
summary
priorities[]
suggestedActions[]
missingData[]
availableSources[]
```

### ProjectAiQuestionResponse

```text
answer
evidenceFactIds[]
factsAsOf
generatedAt
insufficientEvidence
```

## 5. Evidenzregeln

- Konkrete Prioritäten und Maßnahmen benötigen mindestens eine gültige Faktenreferenz.
- Die Referenz wird serverseitig gegen die tatsächlich an das Modell übergebenen Fakten geprüft.
- Die UI löst eine `factId` über eine serverseitig oder clientseitig bereitgestellte
  Source Map in einen sichtbaren Labeltext und einen Zielanker auf.
- Ein Beleglink darf nie ins Leere führen.
- Fehlt eine UI-Zielansicht, bleibt der Beleg als nicht anklickbarer Text sichtbar.
- Aussagen zu fehlenden Daten referenzieren einen `MISSING_DATA`-Fakt oder ein
  strukturiertes `missingData`-Element.

## 6. Fehlerzustände

### 400

Ungültige Projekt-ID, Request oder Frage.

### 404

Projekt existiert nicht.

### 409 oder 422

Projektkontext ist für eine Analyse unzureichend oder Modellantwort konnte wegen
ungültiger Evidenz nicht akzeptiert werden. An bestehende Fehlerkonventionen anpassen.

### 503

KI ist deaktiviert oder Provider nicht erreichbar.

Die Projekt-Detailseite bleibt in jedem Fall nutzbar.

## 7. Cache und Aktualität

Empfehlung:

- Cache-Key: Projekt-ID + letzter Projekt-Datenstand + Analyse-Schema-Version
- normale Anzeige darf Cache verwenden
- „Neu analysieren“ umgeht den Cache
- UI zeigt `factsAsOf` und `generatedAt`
- bei geändertem Projekt-Datenstand darf eine ältere Analyse nicht als „aktuell“ erscheinen

## 8. Datenschutz und Logging

- Nur für den Anwendungsfall notwendige Projektdaten übergeben.
- Keine sensiblen Personaldetails; nur Rollen-, Skill- und Kapazitätswirkungen.
- Keine Prompt- oder vollständigen Projektinhalte auf INFO-Level loggen.
- API-Key und Authorization Header niemals loggen.
- Provider-Fehler technisch korrelierbar, aber ohne vertrauliche Inhalte protokollieren.

## 9. UI-Abgleich mit der Referenz

Die HTML-Referenz zeigt unter anderem:

- sticky rechte KI-Card
- Header „Projekt-Assistent“
- Statusindikator
- Tabs „Überblick“, „Maßnahmen“, „Fragen“
- Management-Zusammenfassung in hervorgehobener Box
- priorisierte Cards mit Rang
- Belegzeilen
- Maßnahmenkarten mit Vorschlagsbadge
- Datenlücken-Hinweis
- Chat mit Vorschlagsfragen
- explizite Lade- und Fehlerzustände
- Disclaimer am Card-Ende

Diese Struktur soll erkennbar erhalten bleiben, aber in vorhandene Angular- und
SCSS-Patterns integriert werden.

## 10. Sinnvolle Umsetzungsetappen

1. Repositoryanalyse und Wiederverwendungsplan
2. DTOs und Fakten-IDs
3. serverseitiger Kontextaggregator
4. Mock-Provider und Response-Validator
5. Analyse-Endpoint
6. Angular-API-Service und Models
7. Überblick-Tab
8. Maßnahmen-Tab und bearbeitbarer Entwurf
9. Fragen-Endpoint und Chat-Tab
10. Accessibility und responsive Styling
11. Tests
12. Provider-Konfiguration, sofern fachlich entschieden
13. BMAD-Dokumentation und Abschlussreview
