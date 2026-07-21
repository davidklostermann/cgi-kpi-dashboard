---
name: cgi-kpi-dashboard-security
type: architecture-spine
purpose: security-multi-user-extension
altitude: feature
paradigm: layered-modular-monolith + session-auth
scope: Post-MVP — Auth, Roles, Workspace isolation, Admin AI config
status: draft
created: 2026-07-21
updated: 2026-07-21
decisions_closed: P0-2026-07-21
binds: [FR-22..FR-32, NFR-11..NFR-20]
extends: ../architecture-cgi-kpi-dashboard-2026-07-13/ARCHITECTURE-SPINE.md
sources:
  - ../../prds/prd-cgi-kpi-dashboard-security-multi-user/prd.md
  - ../../prds/prd-cgi-kpi-dashboard-security-multi-user/security-addendum.md
companions:
  - threat-model.md
  - security-decisions.md
---

# Architecture Spine — Security & Multi-User

Erweitert den MVP-Spine. Historische ADs bleiben; Überholtes = SUPERSEDED.

## Baseline Ist (Code)

Spring Boot 3.5.16, kein Security · Angular 22, kein Auth · AI-Key Env · KI-Endpoints offen · `ProjectAiAnalysisService` Cache `projectId|factsAsOf` · Flyway V1–V7 ohne User-Tabellen.

## v1 Invariants (P0 DECIDED)

1. **Ein Default-Workspace**; alle bestehenden fachlichen Daten zugeordnet; alle aktiven Members sehen gemeinsame Daten; kein `project_membership` in v1 (Erweiterung vorbereiten).
2. **Private Daten** immer `workspace_id` + `user_id` aus Security Context.
3. **Session-Auth** (AD-12): lokal, Spring Security, CSRF, Fixation-Schutz, kein JWT/localStorage.
4. **Bootstrap-Admin** nur via Env, nur wenn DB leer an Users; Force password change; kein Flyway-Default-Passwort.
5. **KI nur ADMIN** auf allen drei Ist-Endpoints; URL + Method + Service.
6. **Kein KI-Chat-Persist** in PG; Cache tenant-keyed oder disabled; Config-Change busts cache.
7. **Master-Key** nur Env (Dev); nie in PG; kein hardcoded Fallback; Prod KMS = Blocker.
8. **Single Backend-Instance** für Sessions; multi-instance später JDBC/Redis Session Store.

## Package Delta (geplant)

```
…security/     SecurityConfig, Session, CSRF, CurrentUser
…domain/       User, Workspace, Membership, Role, AuditEvent (+ später project_membership)
…admin/        User admin, AI config use cases
…ai/crypto/    EncryptionService, KeyResolver (external)
```

AD-2 bleibt: `ai.*` nur über `kpi.*` Reader.

## AuthZ Layers

URL/Method → Service → Objekt (Workspace; Projekt = Workspace-Mitgliedschaft in v1). Frontend = UX only.

## Geplante Schema-Migration (nicht implementiert)

1. `workspace` + Default-Zeile  
2. `app_user`, `workspace_membership` (role)  
3. `workspace_id` auf gemeinsamen Tabellen + Backfill + FK/Index  
4. Tabellen für private Settings (workspace_id, user_id)  
5. Später optional `project_membership` — Spalte/Tabelle vorsehen oder dokumentiertes Extension-Point  

Kein Admin-Passwort in SQL-Seeds.

## SUPERSEDED

| MVP | → |
|---|---|
| AD-6 | AD-12, AD-13 |
| AD-8 (Env-only Provider-Key) | AD-15, AD-16 (teilweise) |
| Deferred „Auth nach Pilot“ | Epics 11–14 |

Details: `security-decisions.md` · Threats: `threat-model.md`.
