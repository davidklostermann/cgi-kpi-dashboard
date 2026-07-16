# Übergabepaket – CGI Projekt-Assistent

Dieses Paket beschreibt die Umsetzung des in `reference/cgi-project-ai-panel-preview.html`
gezeigten Projekt-Assistenten in der bestehenden Angular-/Spring-Boot-Anwendung.

## Inhalt

- `01_CURSOR_MASTER_PROMPT.md`  
  Diesen Inhalt vollständig als ersten Auftrag in Cursor einfügen.
- `02_IMPLEMENTATION_SPEC.md`  
  Fachliche und technische Detailvorgaben.
- `03_ACCEPTANCE_CHECKLIST.md`  
  Abnahmekriterien für Implementierung und Review.
- `contracts/ai-api-example.json`  
  Beispiel für einen strukturierten API-Response.
- `contracts/project-ai-context-example.json`  
  Beispiel des serverseitig erzeugten, freigegebenen Faktenkontexts.
- `reference/cgi-project-ai-panel-preview.html`  
  Verbindliche visuelle Referenz für Struktur, Hierarchie und Interaktionen.
- `docs/project-context.txt`  
  Projektkontext und aktueller BMAD-Stand.

## Empfohlene Verwendung in Cursor

1. ZIP entpacken.
2. Den entpackten Ordner in das Root-Verzeichnis des Git-Repositories legen, zum Beispiel:
   `handoff/project-ai-panel/`
3. In Cursor das Repository öffnen.
4. `01_CURSOR_MASTER_PROMPT.md` vollständig an den Agenten übergeben.
5. Cursor ausdrücklich Zugriff auf das Repository und den Übergabeordner erlauben.
6. Nach der Implementierung `03_ACCEPTANCE_CHECKLIST.md` Punkt für Punkt prüfen.

## Wichtige Leitplanke

Die HTML-Datei ist eine visuelle und funktionale Referenz, aber kein Quellcode,
der unverändert in Angular kopiert werden soll. Cursor soll vorhandene Komponenten,
Services, DTOs, Styles, Tests und Naming-Konventionen des Repositories wiederverwenden.

Ein API-Key oder Modellaufruf gehört niemals in das Angular-Frontend.
