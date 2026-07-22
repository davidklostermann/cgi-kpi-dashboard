---
baseline_commit: 018e700ec68b4df002edf2ed063a284e2b07d626
---

# Story 12.3: AI-Cache Isolation oder Disable

Status: done

## Story

As a Workspace-Nutzer,
I want dass KI-Analyse-Cache-Einträge strikt an meinen Workspace gebunden sind,
so that kein Cross-Workspace-Leak über denselben projectId/factsAsOf-Key möglich ist (FR-25, AD-18).

## Scope

**In dieser Story:**

- `ProjectAiAnalysisService`-Cache-Key: `workspaceId|projectId|factsAsOf|providerConfigVersion`
- `workspaceId` aus `CurrentUserService.requireWorkspaceId()` — nie aus Client
- `providerConfigVersion`-Hook (Default `0`; vollständige Invalidierung bei Config-Change → Epic 13.4)
- `invalidateAll()` auf Cache-Komponente für späteren Admin-Cache-Bust
- Unit-Tests: gleiche projectId in verschiedenen Workspaces → getrennte Cache-Einträge; Config-Version im Key

**Out of Scope:**

- Portfolio-KI-Cache (aktuell kein Cache in `PortfolioAiAnalysisService`)
- Vollständige Fremdzugriffs-Suite → **Story 12.4**
- Persistente AI-Config + echtes `config_version++` → **Epic 13.3/13.4**
- Q&A-Cache (`ask()`) — nicht gecacht (bleibt so)

## Abhängigkeiten

- **Voraussetzung:** Story 12.1 done (workspaceId im Principal), 12.2 done (KI nur ADMIN)
- **Blockiert:** 13.4 (Cache-Bust gegen isolierten Cache), 12.4 (Cache-Isolationstests)

## Acceptance Criteria

1. **Cache-Key** enthält `workspaceId|projectId|factsAsOf|providerConfigVersion` (Reihenfolge fix, `|` als Trenner).
2. **workspaceId** kommt ausschließlich aus Security-Context (`CurrentUserService`), nicht aus Request/Context-DTO.
3. **Kein Cross-Workspace-Leak:** Zwei Workspaces mit gleicher projectId + factsAsOf liefern unterschiedliche Cache-Slots (Unit-Test).
4. **Config-Version-Hook:** `AiProviderConfigVersionProvider` (oder äquivalent) liefert Version; Änderung erzeugt neuen Key; `invalidateAll()` leert Cache für Epic 13.4.
5. **refresh=true** umgeht Cache weiterhin.
6. Bestehende KI-Tests bleiben grün.

## Tasks / Subtasks

- [x] `providerConfigVersion` in `AiProperties` + `AiProviderConfigVersionProvider` (AC: #4)
- [x] `ProjectAiAnalysisCache` mit scoped Key + `invalidateAll()` (AC: #1, #4)
- [x] `ProjectAiAnalysisService` auf neuen Key umstellen (AC: #2, #5)
- [x] Unit-Tests Cache-Isolation + Config-Version (AC: #3, #6)
- [x] Regression: Maven AI/Auth-Tests grün (AC: #6)

### Review Findings

- [x] [Review][Defer] `invalidateAll()` noch nicht an Config-Lifecycle angebunden — Epic 13.4 Follow-up
- [x] [Review][Defer] Cache-Poisoning-Integrationstest in Story-12.4-Suite — beabsichtigt, kein Blocker

### Senior Developer Review (AI)

**Outcome:** Approve  
**Date:** 2026-07-22

## Dev Notes

### Architektur-Invarianten

- **AD-18:** Cache-Key `workspaceId|projectId|factsAsOf|providerConfigVersion`; unsicher → disable (hier: Key fix statt disable)
- **Ist-Zustand:** `ProjectAiAnalysisService` nutzt unsicheren Key `projectId|factsAsOf` — **Gap**
- **Portfolio:** kein In-Memory-Cache → keine Änderung nötig

### Touchpoints

- `AiProperties.java` — `providerConfigVersion` (default 0)
- Neu: `AiProviderConfigVersionProvider.java`, `ProjectAiAnalysisCache.java`
- `ProjectAiAnalysisService.java` — Cache-Delegation
- `ProjectAiAnalysisServiceTest.java` — Isolationstests

### Testing

- Mock `CurrentUserService.requireWorkspaceId()` mit zwei verschiedenen UUIDs
- Zwei `analyze()`-Aufrufe mit gleicher projectId/factsAsOf aber verschiedenen Workspaces → ModelClient zweimal aufgerufen
- Zweiter Aufruf gleicher Workspace → ModelClient einmal (Cache-Hit)

### References

- [Source: `stories-security-multi-user.md` — Story 12.3]
- [Source: `security-decisions.md` — AD-18]
- [Source: `12-2-endpoint-policies-ki-admin.md` — Out of Scope Cache]

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

### Completion Notes List

- Unsicheren Cache-Key `projectId|factsAsOf` durch `workspaceId|projectId|factsAsOf|providerConfigVersion` ersetzt (AD-18).
- `ProjectAiAnalysisCache` als Spring-Component mit `invalidateAll()` für Epic 13.4 vorbereitet.
- `AiProviderConfigVersionProvider` liest Version aus `AiProperties.providerConfigVersion` (Default 0).
- Unit-Tests: Workspace-Isolation, Config-Version-Key, Cache-Hit/Miss, refresh-Bypass, invalidateAll.

### File List

- backend/src/main/java/com/cgi/kpi/dashboard/ai/cache/ProjectAiAnalysisCache.java (neu)
- backend/src/main/java/com/cgi/kpi/dashboard/ai/config/AiProviderConfigVersionProvider.java (neu)
- backend/src/main/java/com/cgi/kpi/dashboard/ai/config/AiProperties.java
- backend/src/main/java/com/cgi/kpi/dashboard/ai/service/ProjectAiAnalysisService.java
- backend/src/test/java/com/cgi/kpi/dashboard/ai/cache/ProjectAiAnalysisCacheTest.java (neu)
- backend/src/test/java/com/cgi/kpi/dashboard/ai/service/ProjectAiAnalysisServiceTest.java

### Change Log

- 2026-07-22: Story 12.3 — AI-Cache workspace-isoliert; Config-Version-Hook; Tests grün.