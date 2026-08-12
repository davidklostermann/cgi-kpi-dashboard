# User Stories — Security & Multi-User (Post-MVP)

> Ergänzt `stories-mvp.md`. Keine Implementierung hier.  
> Stack-Ist: Angular **22** · Spring Boot **3.5.16** · Java 21 · PostgreSQL  
> P0-Entscheidungen geschlossen 2026-07-21 — siehe Security-PRD / Addendum.

---

## Epic 11 — Authentifizierung und Benutzerbasis

**Abhängigkeiten:** MVP 1–9. Parallel/nach Epic 10 möglich.  
**Reihenfolge:** 11.1 → 11.2 → 11.3 → 11.4 → 11.5 → 11.6

### Story 11.1 — Datenmodell User/Workspace/Membership + Flyway-Plan

**Als** Entwickler **möchte ich** das Auth-/Workspace-Schema und den Migrationsplan **damit** Benutzer und Default-Workspace persistierbar sind.

**AC:**
- Geplante Tabellen: `workspace`, `app_user` (Hash-Feld, `must_change_password`, `active`), `workspace_membership` (role USER|ADMIN).
- Migrationsplan: Default-Workspace; `workspace_id` auf gemeinsamen Fachtabellen; Backfill bestehender Daten; FKs/Indizes; Datenverlust = Fail.
- Extension-Point für späteres `project_membership` dokumentiert (nicht implementiert).
- **Kein** Admin-Passwort und kein Default-User in Flyway-SQL.
- Private-Daten-Tabellen-Konzept (settings/filters/views) mit `workspace_id`+`user_id` spezifiziert.

**Abhängigkeiten:** —  
**Hinweis:** Schema-Migration implementieren in Dev-Story; hier Spezifikation/AC bindend.

### Story 11.2 — Bootstrap-Administrator (Env)

**Als** Betreiber **möchte ich** genau einen initialen Admin aus Env-Variablen **damit** kein festes Passwort im Repo liegt.

**AC:**
- Läuft nur wenn **kein** Benutzer existiert.
- Liest `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_PASSWORD` (nur Env).
- Passwort gehasht speichern; Klartext nie loggen.
- `must_change_password=true`; Admin-Rolle im Default-Workspace.
- Danach kein erneuter Bootstrap-Admin.
- Fehlende/leere Env → kein User, kein Default-Passwort; Login unmöglich bis konfiguriert.
- Tests: Bootstrap ≤1; kein Standardpasswort.

**Abhängigkeiten:** 11.1

### Story 11.3 — Spring-Security-Grundkonfiguration

**Als** Entwickler **möchte ich** Default Deny und Session-Security **damit** APIs nicht mehr offen sind.

**AC:**
- `spring-boot-starter-security`; Session-Auth; CSRF aktiv für Writes; Session-Fixation-Schutz.
- Cookie: HttpOnly; Secure in Prod-Profil.
- Unauth → 401 auf geschützte `/api/**` (außer Login).
- Kein JWT; kein localStorage-Token-Pfad.
- Single-Instance Session dokumentiert.

**Abhängigkeiten:** 11.1 (UserDetails/Laden)

### Story 11.4 — Login, Logout, `/api/auth/me` + Passwortwechsel-Zwang

**Als** Nutzer **möchte ich** mich anmelden/abmelden und meine Identität sehen **damit** eine Session entsteht.

**AC:**
- `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`.
- Deaktivierte Nutzer: Login abgelehnt.
- `must_change_password`: Login ok nur mit erzwungenem Change-Flow (Endpoint spezifizieren); sonst eingeschränkt/`me` signalisiert Pflicht.
- Tests: 401 unauth; parallele Sessions getrennt.

**Abhängigkeiten:** 11.2, 11.3

### Story 11.5 — Angular Authentifizierung (Login-UI, AuthService, Guards)

**Als** Nutzer **möchte ich** eine Login-Seite und sichtbaren Logout **damit** ich die App bedienen kann.

**AC:**
- Login-Page; Shell: User/Rolle/Logout; Guard → Login wenn unauth; 401-Interceptor.
- CSRF-Token für Writes; **kein** Auth-Token in `localStorage`.
- Logout cleared Client-State (Filter-Signals etc.).
- UX: Hinweis Passwort ändern wenn gefordert.

**Abhängigkeiten:** 11.4

### Story 11.6 — Session- und Auth-Basis-Tests

**Als** Entwickler **möchte ich** automatisierte Auth-Tests **damit** Regressionen auffallen.

**AC (mind.):**
- Unauth geschützt → 401  
- Bootstrap-Regeln (11.2)  
- Initial-Admin muss Passwort ändern  
- Deaktiviert: kein Login  
- Session Deaktivierter abgewiesen (kann Stub/Hook nutzen bis volle User-Mgmt in 13)  
- Parallele Sessions getrennt  
- CSRF Write blocked ohne Token  

**Abhängigkeiten:** 11.4, 11.5

**DoD Epic 11:** FR-22; NFR-12/13 Basis; AD-12; kein Flyway-Default-Passwort.

---

## Epic 12 — Autorisierung und Datenisolierung

**Abhängigkeiten:** Epic 11 (mind. 11.3–11.4).  
**Reihenfolge:** 12.1 → 12.2 → 12.3 → 12.4

### Story 12.1 — Workspace-Scope auf Repositories + Private Settings Isolation

**AC:** Alle gemeinsamen Queries workspace-scoped; aktive Members sehen alle WS-Projekte; private Settings nur eigener user_id; manipulierte IDs ignoriert/abgewiesen; kein Cross-Workspace.

### Story 12.2 — Endpoint Policies: Fakten auth, KI+Admin nur ADMIN

**AC:** Fakten-APIs authenticated + WS;  
`GET /api/portfolio/ai/trend-analysis`, `GET /api/projects/{id}/ai/analysis`, `POST /api/projects/{id}/ai/questions` → ADMIN + WS/Projektberechtigung (v1 = WS-Membership); URL + Method + Service-Check; USER → 403.

### Story 12.3 — AI-Cache Isolation oder Disable

**AC:** Key `workspaceId|projectId|factsAsOf|providerConfigVersion` oder Cache disabled; kein fremder Workspace im Cache; Config-Version-Hook vorbereitet (voll mit Epic 13).

### Story 12.4 — Fremdzugriffs- und Isolationstests

**AC:** USER→Admin/KI 403; ADMIN→KI erlaubt; außerhalb WS kein Zugriff; private Settings A≠B; Cache-Isolation; manipulierte Context-IDs.

**DoD Epic 12:** FR-23/25/26 Basis; NFR-11/16; AD-13/14/18.

---

## Epic 13 — Administration und API-Key

**Abhängigkeiten:** Epic 11 vollständig; Epic 12 Policies (12.2) empfohlen vor KI-Freigabe unter AuthZ.  
**Reihenfolge:** 13.1 → 13.2 → 13.3 → 13.4

### Story 13.1 — Benutzerverwaltungs-API + Last-Admin-Guards

**AC:** Anlegen/Aktivieren/Deaktivieren/Rolle; letzter aktiver Admin geschützt; Deaktivierung weist Sessions ab; Audit-Events; Tests.

### Story 13.2 — Benutzerverwaltungs-UI

**AC:** Übersicht, Anlegen, Status, Rolle, Admin-Passwort-Reset (kein E-Mail v1).

### Story 13.3 — AI Config Model + EncryptionService

**AC:** `ai_provider_config`; AES-GCM; Master-Key Env; nie in PG; kein hardcoded Fallback; ohne MK keine Decrypt; masked suffix; Key nicht in Logs/Responses; Master-Key∉PG-Test.

### Story 13.4 — Admin AI API/UI + Connection-Test + Rotation + Cache-Bust

**AC:** Config setzen/ersetzen/enable; Connection-Test ohne Key-Echo; `config_version++`; Cache verwerfen; KI nur ADMIN + enabled.

**DoD Epic 13:** FR-24/27/28/32; NFR-15/17; AD-15/16 Dev.

---

## Epic 14 — Security Hardening und Betrieb

**Abhängigkeiten:** Epic 11–13.  
Stories 14.1 CSRF/CORS/Cookies/Headers · 14.2 Rate-Limit + Audit-UI · 14.3 Scans + parallele Last-Tests + Runbooks (Session-Scale-Hinweis JDBC/Redis; Prod-KMS-Blocker).

**DoD:** FR-29/30; NFR-14/18/19/20.

---

## Abhängigkeiten Epic 11 → 12 → 13

```
11.1 Schema ─┬─► 11.2 Bootstrap ─┐
             └─► 11.3 Security ──┴─► 11.4 Login/Me ─► 11.5 Angular ─► 11.6 Tests
                                      │
                                      ▼
                                   12.1 Scope ─► 12.2 Policies ─┬─► 12.3 Cache ─► 12.4 Tests
                                                                │
                                                                ▼
                                   13.1 User-API ─► 13.2 UI / 13.3 Crypto ─► 13.4 Admin-AI
```

- **12 braucht 11:** ohne Principal/Session keine AuthZ-Tests.  
- **13 braucht 11:** Admin-APIs auf Session+ADMIN.  
- **13 braucht 12.2** (empfohlen): KI-Endpoints erst nach Role-Policy freigeben.  
- **13.4 braucht 12.3:** Cache-Bust gegen isolierten/disabled Cache.

---

## Empfohlener Implementierungsstart

**Story 11.1** — Datenmodell + Flyway-Plan/Migration (ohne Passwort-Seeds).
