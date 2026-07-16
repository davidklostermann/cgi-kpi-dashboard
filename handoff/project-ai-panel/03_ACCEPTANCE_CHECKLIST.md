# Abnahme-Checkliste

## Architektur

- [ ] Angular ruft keinen externen Modellprovider direkt auf.
- [ ] Kein API-Key befindet sich im Frontend oder Git.
- [ ] API-Aufrufe liegen in der bestehenden `core/api`-Schicht.
- [ ] Vorhandene Projekt-Services und DTO-Konventionen werden wiederverwendet.
- [ ] KI- und Faktenbereich bleiben getrennt.
- [ ] Keine KPI-Berechnung wurde in die KI-Schicht verschoben.

## Backend

- [ ] Es gibt einen serverseitigen Projekt-Faktenkontext.
- [ ] Jedes Faktum besitzt eine stabile `factId`.
- [ ] Modellantworten sind strukturiert und validiert.
- [ ] Unbekannte Belegreferenzen werden nicht an das Frontend weitergegeben.
- [ ] Fehlende Daten werden strukturiert ausgewiesen.
- [ ] KI deaktiviert/Providerfehler werden kontrolliert behandelt.
- [ ] Entwicklung und CI funktionieren ohne echten Provider-Key.
- [ ] Logs enthalten keine Secrets oder vollständigen vertraulichen Prompts.
- [ ] Analyse zeigt Datenstand und Erzeugungszeitpunkt.

## Überblick-Tab

- [ ] Management-Zusammenfassung ist vorhanden.
- [ ] Aufmerksamkeitspunkte sind priorisiert.
- [ ] Jeder konkrete Punkt enthält sichtbare Belege.
- [ ] Beleglinks öffnen oder markieren die passende Faktenquelle.
- [ ] Fehlende Daten werden sichtbar genannt.
- [ ] „Neu analysieren“ besitzt Loading- und Error-State.

## Maßnahmen-Tab

- [ ] Jede Maßnahme ist als KI-Vorschlag gekennzeichnet.
- [ ] Owner und Termin werden nur bei belegbarer Grundlage gesetzt.
- [ ] Belege sind vorhanden.
- [ ] Keine Maßnahme wird automatisch gespeichert oder ausgelöst.
- [ ] Ein vorbereiteter Entwurf ist bearbeitbar.
- [ ] Die Entscheidungshoheit bleibt ausdrücklich beim Menschen.

## Fragen-Tab

- [ ] Antworten verwenden nur den Projektkontext.
- [ ] Antworten enthalten Belege.
- [ ] Unbelegte Fragen werden mit einer klaren Datenlücken-Antwort beantwortet.
- [ ] Chat besitzt Lade- und Fehlerzustände.
- [ ] Chat-Verlauf verwendet `aria-live="polite"`.
- [ ] Es findet keine Websuche statt.

## UI und Accessibility

- [ ] Layout entspricht erkennbar der HTML-Referenz.
- [ ] Rechte KI-Spalte ist auf Desktop sinnvoll sticky.
- [ ] Mobile/Tablet-Darstellung ordnet die KI-Card unter dem Faktenbereich ein.
- [ ] Tabs funktionieren per Maus und Tastatur.
- [ ] Home/End und Pfeiltasten funktionieren im Tab-Pattern.
- [ ] Fokus ist sichtbar.
- [ ] Farbe ist nicht die einzige Statusinformation.
- [ ] KI-Fehler blockieren die Faktenansicht nicht.
- [ ] Der Disclaimer ist dauerhaft sichtbar.

## Tests und Qualität

- [ ] Backend-Unit- und Integrationstests sind grün.
- [ ] Angular-Component- und Service-Tests sind grün.
- [ ] Angular-Build ist grün.
- [ ] Maven-Build ist grün.
- [ ] Typecheck/Linting sind grün, soweit vorhanden.
- [ ] Keine bestehenden Tests wurden ohne Begründung entfernt oder abgeschwächt.
- [ ] BMAD-Artefakte und Sprintstatus wurden korrekt aktualisiert.
- [ ] Abschlussbericht nennt alle geänderten Dateien und verbleibenden Grenzen.
