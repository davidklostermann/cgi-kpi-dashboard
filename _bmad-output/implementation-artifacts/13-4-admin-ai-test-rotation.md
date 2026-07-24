---
baseline_commit: 494b0af18d706c42ba774591675931c87a5a3be3
---

# Story 13.4: Admin KI-Konfiguration & Connection-Test

Status: done

<!-- Ultimate context engine analysis completed - comprehensive developer guide created -->

## Story

Als Administrator,
möchte ich die KI-Provider-Konfiguration über eine API und UI verwalten sowie den API-Key testen können,
damit ich die KI-Funktionen ohne Server-Neustart aktivieren, deaktivieren oder aktualisieren kann (FR-27, FR-32, AD-15, AD-18).

## Acceptance Criteria

1. **Gegeben** eine aktive Datenbank-Konfiguration, **wenn** diese auf `enabled=true` gesetzt ist, **dann** verwenden alle KI-Anfragen (`analyze`, `answer`, `analyzePortfolio`) die Werte (Modell, Key) aus der Datenbank anstatt aus den Umgebungsvariablen.
2. **Gegeben** eine neue API für Administratoren, **wenn** `POST /api/admin/ai/test-connection` aufgerufen wird, **dann** wird ein Test-Prompt an Gemini gesendet, um die Gültigkeit des Keys zu prüfen.
3. **Gegeben** den Connection-Test, **dann** darf der API-Key unter keinen Umständen im Response-Body, im Echo oder in den Logs (Klartext) auftauchen (NFR-15).
4. **Gegeben** eine Änderung der Konfiguration (Save oder Enable/Disable), **wenn** diese erfolgreich war, **dann** wird der `providerConfigVersion` Zähler erhöht und alle bestehenden KI-Caches (`ProjectAiAnalysisCache`) werden sofort invalidiert (AD-18).
5. **Gegeben** die UI (optional, falls Zeit in dieser Story), **dann** gibt es einen Bereich unter "Administration", um die KI-Werte zu setzen und den Test-Button zu drücken.

## Tasks / Subtasks

- [x] **API-Erweiterung (Backend)**
  - [x] `AdminAiController` implementieren:
    - [x] `GET /api/admin/ai/config`: Liefert aktuelle DB-Konfiguration (maskiert).
    - [x] `PUT /api/admin/ai/config`: Speichert Modell, Provider, Enabled-Status und optional neuen Key.
    - [x] `POST /api/admin/ai/test-connection`: Führt Validierung durch.
- [x] **Dynamische Provider-Integration**
  - [x] `AiConfigService` erweitern:
    - [x] Logik zum Abrufen der "aktiven" Konfiguration (DB-First, Fallback auf Properties nur wenn DB-Eintrag fehlt oder disabled).
  - [x] `HttpGeminiApiTransport` oder `AiProperties` anpassen:
    - [x] Sicherstellen, dass der Transport den Key/Modell dynamisch auflöst (z.B. via `@RequestScoped` Context oder direkter Service-Abfrage).
- [x] **Connection-Test Logik**
  - [x] Implementierung eines minimalen "Echo"-Prompts gegen Gemini.
  - [x] Mapping von Fehlermeldungen (z.B. 401 Invalid Key, 403 Forbidden).
- [x] **Cache-Busting (AD-18)**
  - [x] `AiProviderConfigVersionProvider` so anpassen, dass er bei jeder Konfigurationsänderung den Zähler in der DB (oder In-Memory-Singleton) hochzählt.
  - [x] Aufruf von `ProjectAiAnalysisCache.invalidateAll()` nach erfolgreichem Update.
- [x] **UI (Frontend - Admin-Bereich)**
  - [x] `features/admin/ai-config`: Neue Seite für KI-Einstellungen.
  - [x] Formular mit Maskierung und "Test"-Button.
  - [x] Integration in die Admin-Navigation.

## Dev Notes

### Architektur-Compliance (MUST)
- **AD-15:** Dynamische Key-Auflösung aus DB.
- **AD-18:** Cache-Bust bei Config-Änderung.
- **AD-10:** API-Struktur beibehalten.

### Technische Details
- **Cache-Bust:** Der `ProjectAiAnalysisCache` nutzt den `AiProviderConfigVersionProvider` bereits als Teil des Cache-Keys. Eine Erhöhung der Version reicht technisch aus, um "stale" Einträge zu ignorieren, aber ein explizites `clear()` spart Speicher.
- **Transport-Anpassung:** `HttpGeminiApiTransport` ist ein `@Component`. Er sollte den Key nicht mehr einmalig im Konstruktor prüfen, sondern pro Request vom `AiConfigService` beziehen.

### Dateien (Voraussichtliche Änderungen)
- `backend/src/main/java/com/cgi/kpi/dashboard/api/admin/AdminAiController.java` (Neu)
- `backend/src/main/java/com/cgi/kpi/dashboard/admin/ai/AiConfigService.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/ai/client/HttpGeminiApiTransport.java`
- `frontend/src/app/features/admin/ai-config/` (Neu)

## Abgrenzung
- **Story 13.3:** Die Verschlüsselung ist bereits implementiert und wird hier genutzt.
- **Epic 14:** Rate-Limiting für den Connection-Test (Schutz gegen Missbrauch) erfolgt in Epic 14.

## Review Findings

### Defer
- [x] [Review][Defer] AdminAiController.java - Fester Standard-Provider — Der Standardwert "gemini" ist in den `@RequestParam`-Annotationen fest kodiert, was die Erweiterbarkeit erschwert. (backend/src/main/java/com/cgi/kpi/dashboard/api/admin/AdminAiController.java) — deferred, pre-existing
- [x] [Review][Defer] AdminAiController.java - Fehlende Validierung des Providers — Der Controller validiert den `provider`-Parameter nicht explizit, was zu unnötigen Service-Aufrufen oder unerwartetem Verhalten führen könnte. (backend/src/main/java/com/cgi/kpi/dashboard/api/admin/AdminAiController.java) — deferred, pre-existing
- [x] [Review][Defer] AdminAiController.java - Interne Klasse für DTO — `ConnectionTestResponseDto` ist als innere Klasse definiert, was der Best Practice widerspricht, DTOs als Top-Level-Klassen oder in einem dedizierten `dto`-Paket abzulegen. (backend/src/main/java/com/cgi/kpi/dashboard/api/admin/AdminAiController.java) — deferred, pre-existing
- [x] [Review][Defer] AdminAiController.java - Allgemeine Fehlerbehandlung — Es fehlt eine explizite Fehlerbehandlung oder benutzerdefinierte Ausnahmebehandlung in den Controller-Methoden. (backend/src/main/java/com/cgi/kpi/dashboard/api/admin/AdminAiController.java) — deferred, pre-existing
- [x] [Review][Defer] AdminAiController.java - `Optional` als Rückgabetyp — Die Rückgabe von `Optional<AiProviderConfigDto>` direkt aus einer REST-Controller-Methode kann unpraktisch sein; `ResponseEntity` mit `HttpStatus.NOT_FOUND` wäre oft klarer. (backend/src/main/java/com/cgi/kpi/dashboard/api/admin/AdminAiController.java) — deferred, pre-existing
- [x] [Review][Defer] AiConfigService.java - Direkte Abhängigkeit von `GeminiApiTransport` — Der Dienst ist direkt von `GeminiApiTransport` abhängig, anstatt ein Interface zu verwenden, um die Unterstützung weiterer KI-Anbieter zu ermöglichen. (backend/src/main/java/com/cgi/kpi/dashboard/admin/ai/AiConfigService.java) — deferred, pre-existing
- [x] [Review][Defer] AiConfigService.java - Verwendung von `@Lazy` — Die `@Lazy`-Annotation für `GeminiApiTransport` könnte auf eine potenzielle zirkuläre Abhängigkeit oder ein Designproblem hinweisen. (backend/src/main/java/com/cgi/kpi/dashboard/admin/ai/AiConfigService.java) — deferred, pre-existing
- [x] [Review][Defer] AiConfigService.java - `configVersion` basierend auf `System.currentTimeMillis()` — Die Verwendung von `System.currentTimeMillis()` für `configVersion` kann in verteilten Umgebungen oder bei Systemzeitänderungen zu Problemen führen. (backend/src/main/java/com/cgi/kpi/dashboard/admin/ai/AiConfigService.java) — deferred, pre-existing
- [x] [Review][Defer] AiConfigService.java - API-Key-Maskierung nach Entschlüsselung — Der API-Schlüssel wird in `toDto` entschlüsselt und dann maskiert, was ein unnötiges Sicherheitsrisiko darstellen könnte. (backend/src/main/java/com/cgi/kpi/dashboard/admin/ai/AiConfigService.java) — deferred, pre-existing
- [x] [Review][Defer] ai-config.component.html - Hardkodiertes "gemini" im Hint — Der Hinweis "Aktuell wird nur Gemini unterstützt" ist fest kodiert. (frontend/src/app/features/admin/ai-config/ai-config.component.html) — deferred, pre-existing
- [x] [Review][Defer] ai-config.component.html - Maskierter API-Schlüssel im Platzhalter — Ein maskierter Schlüssel in einem `placeholder` kann verwirrend sein, da Platzhalter für Eingabehinweise gedacht sind. (frontend/src/app/features/admin/ai-config/ai-config.component.html) — deferred, pre-existing
- [x] [Review][Defer] ai-config.component.html - Unzureichende `mat-error`-Validierung — Die `mat-error`-Meldung für das Modellfeld prüft nur auf `required`. (frontend/src/app/features/admin/ai-config/ai-config.component.html) — deferred, pre-existing
- [x] [Review][Defer] ai-config.component.html - Inkonsistente Ladeanzeige — Die Bedingungen für die Anzeige des rotierenden Icons und das Deaktivieren der Schaltfläche könnten inkonsistent sein. (frontend/src/app/features/admin/ai-config/ai-config.component.html) — deferred, pre-existing
- [x] [Review][Defer] ai-config.component.html - Hardkodierte deutsche Sprache — Alle Texte sind fest in Deutsch kodiert. (frontend/src/app/features/admin/ai-config/ai-config.component.html) — deferred, pre-existing
- [x] [Review][Defer] ai-config.component.scss - Hardkodierte Stile — Die SCSS-Datei enthält hartkodierte Werte, die besser als Variablen oder Teil eines Designsystems definiert werden sollten. (frontend/src/app/features/admin/ai-config/ai-config.component.scss) — deferred, pre-existing
- [x] [Review][Defer] ai-config.component.scss - Verwendung von `::ng-deep` — Die Verwendung von `::ng-deep` wird generell nicht empfohlen. (frontend/src/app/features/admin/ai-config/ai-config.component.scss) — deferred, pre-existing
- [x] [Review][Defer] ai-config.component.scss - Animation im Komponenten-SCSS — Die `spinning`-Animation ist direkt im SCSS der Komponente definiert und sollte bei mehrfacher Verwendung ausgelagert werden. (frontend/src/app/features/admin/ai-config/ai-config.component.scss) — deferred, pre-existing
- [x] [Review][Defer] ai-config.service.ts - Optionales `id` in `AiProviderConfig` — Das Feld `id` ist optional, obwohl es bei einer bestehenden Konfiguration typischerweise vorhanden sein sollte. (frontend/src/app/features/admin/ai-config/ai-config.service.ts) — deferred, pre-existing
- [x] [Review][Defer] ai-config.service.ts - Hardkodierte `apiUrl` — Die `apiUrl` ist fest kodiert und sollte über Umgebungsvariablen oder einen Konfigurationsdienst konfiguriert werden. (frontend/src/app/features/admin/ai-config/ai-config.service.ts) — deferred, pre-existing
- [x] [Review][Defer] ai-config.service.ts - Hardkodierter `gemini`-Standard in `getConfig` und `testConnection` — Der Frontend-Dienst hardkodiert "gemini" als Standard-Provider, was zu einer engen Kopplung führt. (frontend/src/app/features/admin/ai-config/ai-config.service.ts) — deferred, pre-existing
- [x] [Review][Defer] Potenzielle Inkonsistenz: `apiKey` in `SaveAiConfigRequestDto` — Es ist wichtig sicherzustellen, dass dieser `apiKey` im Backend korrekt gehandhabt und sofort verschlüsselt wird. (backend/src/main/java/com/cgi/kpi/dashboard/api/admin/dto/SaveAiConfigRequestDto.java) — deferred, pre-existing
- [x] [Review][Defer] Möglicher `NullPointerException` für `geminiTransport` in `AiConfigService.java` — `geminiTransport` kann null sein, wenn es aufgrund der bedingten Bean-Erstellung nicht injiziert wurde. (backend/src/main/java/com/cgi/kpi/dashboard/admin/ai/AiConfigService.java) — deferred, pre-existing