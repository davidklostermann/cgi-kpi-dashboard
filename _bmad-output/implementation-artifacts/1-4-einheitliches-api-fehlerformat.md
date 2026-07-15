---
story_key: 1-4-einheitliches-api-fehlerformat
epic: 1
baseline_commit: NO_VCS
---

# Story 1.4: Einheitliches API-Fehlerformat

Status: done

## Story

Als Frontend-Entwickler  
möchte ich strukturierte Fehlerantworten  
damit Fehlerpanels konsistent sind.

## Acceptance Criteria

1. **Gegeben** ein Backend-Fehler, **wenn** die API antwortet, **dann** enthält der Body `{ "code", "message" }`.
2. **Gegeben** ein Global Exception Handler, **wenn** eine unbehandelte Exception auftritt, **dann** wird 500 mit strukturiertem Body zurückgegeben.

## Tasks / Subtasks

- [x] Task 1: `ApiErrorResponse`-DTO `{ code, message }` (AC: 1)
- [x] Task 2: `ApiException` für explizite API-Fehler (AC: 1)
- [x] Task 3: `GlobalExceptionHandler` mit `@RestControllerAdvice` (AC: 1, 2)
- [x] Task 4: `WebMvcTest` für Fehler-Handler (AC: 1, 2)
- [x] Task 5: Integrationstest unbekannter `/api/*`-Pfad (AC: 1)
- [x] Task 6: Story-Dokumentation aktualisieren

### Review Findings

- [x] [Review][Defer] Kein Handler für Bean-Validation (`MethodArgumentNotValidException`) — folgt mit ersten REST-Controllern
- [x] [Review][Defer] Kein dedizierter 503-Handler für KI-Ausfall — Story AI-Endpunkte (AD-5/AD-7)
- [x] [Review][Dismiss] `ErrorProbeController` nur in Tests — korrekt, kein Prod-Endpunkt

## Senior Developer Review (AI)

**Datum:** 2026-07-14  
**Ergebnis:** Approved — alle ACs erfüllt  
**Reviewer:** BMAD Code Review (Blind Hunter + Edge Case Hunter + Acceptance Auditor)

### Prüfmatrix

| Prüffokus | Ergebnis | Evidenz |
|---|---|---|
| Fehlerformat `{ code, message }` | **Bestanden** | `ApiErrorResponse` Record |
| Explizite API-Fehler | **Bestanden** | `ApiException` → 404 mit `NOT_FOUND` |
| Unhandled Exception → 500 | **Bestanden** | `INTERNAL_ERROR`, generische Message (kein Stack-Leak) |
| 404 unter `/api/*` | **Bestanden** | `NoResourceFoundException`-Handler |
| WebMvcTest | **Bestanden** | 2 Tests in `GlobalExceptionHandlerWebMvcTest` |
| Kein Frontend-Scope | **Bestanden** | Keine Frontend-Änderungen |
| Architektur Conventions | **Bestanden** | JSON `{ "code", "message" }` gemäß Spine |

### Acceptance Auditor

| AC | Status | Evidenz |
|---|---|---|
| AC1 Strukturierter Fehler-Body | **Erfüllt** | WebMvcTest + Integrationstest |
| AC2 Unhandled → 500 strukturiert | **Erfüllt** | `handleUnhandled` → `INTERNAL_ERROR` |

## Dev Notes

- Handler in `api.error`-Package (api-Schicht, AD-10 Backend).
- Unhandled exceptions loggen intern, liefern generische Message (Security).
- **Nicht enthalten:** Frontend-Fehler-UI (Story 2.x / später).

### References

- [Source: ARCHITECTURE-SPINE.md — Consistency Conventions API errors]
- [Source: stories-mvp.md — Story 1.4]

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

- `.\mvnw.cmd test` — 6/6 grün

### Completion Notes List

- `ApiErrorResponse`, `ApiException`, `GlobalExceptionHandler` implementiert.
- WebMvcTest für explizite und unbehandelte Exceptions.
- Integrationstest für unbekannte `/api/*`-Pfade → `NOT_FOUND`.
- Code Review 2026-07-14: Approved.

### File List

- backend/src/main/java/com/cgi/kpi/dashboard/api/error/ApiErrorResponse.java
- backend/src/main/java/com/cgi/kpi/dashboard/api/error/ApiException.java
- backend/src/main/java/com/cgi/kpi/dashboard/api/error/GlobalExceptionHandler.java
- backend/src/test/java/com/cgi/kpi/dashboard/api/error/ErrorProbeController.java
- backend/src/test/java/com/cgi/kpi/dashboard/api/error/GlobalExceptionHandlerWebMvcTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/api/error/GlobalExceptionHandlerIntegrationTest.java

### Change Log

- 2026-07-14: Story 1.4 — Einheitliches API-Fehlerformat implementiert
- 2026-07-14: BMAD Code Review — Approved
