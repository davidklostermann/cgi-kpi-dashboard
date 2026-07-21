---
name: security-addendum
parent: prd-cgi-kpi-dashboard-security-multi-user
created: 2026-07-21
updated: 2026-07-21
---

# Security Addendum

## Auth Architecture (v1) — DECIDED

- Spring Security, **serverseitige Sessions**, lokaler Login.
- Endpoints: `POST /api/auth/login` (öffentlich + Rate Limit/Lockout), `POST /api/auth/logout` (auth + CSRF), `GET /api/auth/me` (auth).
- Cookies: HttpOnly; Secure in Prod; SameSite Lax oder Strict (Prod-Detail P1); Session-Fixation-Schutz; CSRF auf Writes.
- **Kein JWT**, kein Auth-Token in `localStorage`.
- Principal inkl. `userId`/`workspaceId`/`roles` aus Security Context.
- OIDC/SSO: Adapter-Schnittstelle vorbereiten; **keine** konkrete CGI-SSO-Behauptung.

### Session-Betrieb (v1) — DECIDED

- Eine Backend-Instanz; parallele getrennte Sessions mehrerer Nutzer.
- JVM-lokale Sessions **nicht** für Horizontal Scaling.
- Später optional: Spring Session JDBC oder Redis — **keine Prod-Wahl** jetzt.

## Bootstrap Administrator — DECIDED

| Regel | Vorgabe |
|---|---|
| Trigger | Nur wenn **kein** Benutzer in der DB existiert |
| Credentials | Nur Env `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_PASSWORD` |
| Hash | Vor Persistenz (NFR-12); Klartext **nie** loggen |
| Force change | Admin **muss** Initialpasswort beim ersten Login ändern |
| Idempotenz | Nach erfolgreicher Erzeugung kein weiterer Bootstrap-Admin |
| Kein Fallback | Fehlende/ leere Env → **kein** Default-Passwort, **kein** unsicherer User |
| Flyway | **Kein** festes Admin-Passwort in Migrationen/Seeds |

### Verhalten ohne User und ohne Bootstrap-Env

Anwendung startet; Login unmöglich bis Env gesetzt und Neustart/Bootstrap erneut versucht (oder manueller Ops-Eingriff). Status/Health darf das signalisieren **ohne** Secrets. Kein silent Default-Admin.

Keine echten Werte in Repo/Docs eintragen.

## Workspace & Data Isolation — DECIDED

```
Default-Workspace
├── memberships (USER | ADMIN) — alle aktiven sehen gemeinsame Daten
├── projects, KPIs, risks, phases, portfolio facts (workspace_id)
└── private user data (workspace_id + user_id): settings, filters, views, UI prefs
    └── (später) KI-Verläufe — in v1 nicht in PostgreSQL
```

- Geplante Migration: Default-Workspace, `workspace_id`-Spalten, Backfill, FKs, Indizes, Daten vollständig erhalten.
- Später erweiterbar: `project_membership` (v1 ungenutzt).
- Client-gesendete `user_id`/`workspace_id` für AuthZ **ignorieren/abweisen**.

## AI Endpoints (absichern)

| Methode | Pfad | AuthZ |
|---|---|---|
| GET | `/api/portfolio/ai/trend-analysis` | ADMIN + Workspace |
| GET | `/api/projects/{id}/ai/analysis` | ADMIN + Workspace/Projekt |
| POST | `/api/projects/{id}/ai/questions` | ADMIN + Workspace/Projekt |

Schichten: URL-Policy · Methoden-Security · Service-Check. Angular Guards nur UX.

## AI Persistenz & Cache — DECIDED

- Keine Persistenz von KI-Fragen/Antworten in PostgreSQL (v1); keine Cross-Session-Chat-Historie.
- Ist-Cache `ProjectAiAnalysisService` (`projectId|factsAsOf`) **unsicher** → vor Multi-User: Key = `workspaceId|projectId|factsAsOf|providerConfigVersion` oder **Cache deaktivieren**.
- Nutzerwpezifische Ergebnisse später: zusätzlich `userId`.
- Config-Change → betroffene Einträge verwerfen (`config_version`).

## AI Provider Config & Master-Key — DECIDED (Dev) / OFFEN (Prod Vendor)

Tabelle `ai_provider_config` wie zuvor (encrypted_api_key, nonce, key_version, masked_key_suffix, enabled, config_version, …).

- AES-GCM (JDK); Decrypt nur transient vor Provider-Call.
- Dev Master-Key: Env (z. B. `APP_AI_MASTER_KEY`); nicht in DB/Git; **kein** hardcoded Fallback.
- Ohne Master-Key: aktivierte DB-Config **nicht** entschlüsselbar → KI-Calls fehlschlagen sicher.
- **Prod Blocker:** externer Secret Store/KMS — Vendor unbekannt, vor Go-Live klären.

## Acceptance Tests (Pflicht)

1. Unauth geschützte API → 401  
2. USER Admin-Endpoint → 403  
3. USER KI-Endpoint → 403  
4. ADMIN KI-Endpoint → erlaubt (bei Config)  
5. Nutzer außerhalb Workspace → kein Zugriff  
6. Manipulierte `user_id`/`workspace_id` → ignoriert/abgewiesen  
7. Private Settings User A unsichtbar für B  
8. Bootstrap ≤ 1 initialer Admin  
9. Bootstrap kein Standardpasswort  
10. Initial-Admin muss Passwort ändern  
11. Deaktivierter User: kein Login  
12. Session Deaktivierter → abgewiesen  
13. Letzter aktiver Admin nicht deaktivierbar  
14. API-Key nicht in Logs/Antworten  
15. Master-Key nicht in PostgreSQL  
16. Cache ohne fremden Workspace  
17. Parallele Sessions getrennt  
18. CSRF auf Writes blockiert  
19. Letzter Admin kann Rolle nicht entfernen  

## Open Decisions (Rest)

| Prio | Frage | Status | Hinweis |
|---|---|---|---|
| ~~P0~~ | Workspace-Sicht, keine project_membership, lokaler Login, Bootstrap, Default-WS, KI-Persistenz/Cache | **CLOSED 2026-07-21** | siehe oben |
| P1 | CGI-SSO/OIDC Pflicht / Timing? | OFFEN | Architektur vorbereitet |
| P1 | Prod-Hosting? | OFFEN | |
| P1 | Prod Secret Store/KMS Vendor? | OFFEN | **Blocker vor Produktion** |
| P1 | Session Idle/Absolute TTL, max. parallele Sessions/User, SameSite final | OFFEN | Empfehlung: ≤8h idle / ≤12h abs / ≤3 sessions; SameSite Lax |
| P1 | Vorrang Env-`GEMINI_API_KEY` vs. DB-Config während Übergang | OFFEN | Empfehlung: DB wenn enabled, sonst Dev-Env |
| P2 | Passwort selbst ändern (nicht nur Admin-Reset)? | Empfehlung: ja | |
| P2 | E-Mail Passwort-Reset? | Empfehlung: nein v1 | |
| P2 | Audit-Retention? | Empfehlung: ≥90 Tage | |
