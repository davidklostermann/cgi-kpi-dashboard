---
name: cgi-kpi-dashboard-security-multi-user
type: prd
status: draft
created: 2026-07-21
updated: 2026-07-21
decisions_closed: P0-2026-07-21
supersedes_partial:
  - NFR-8 (Kein Auth) — ersetzt durch FR-22..FR-32 / NFR-11..NFR-20
  - AD-6 (No authentication in pilot) — siehe architecture-cgi-kpi-dashboard-security
extends:
  - ../prd-cgi-kpi-dashboard-2026-07-13/prd.md
sources:
  - repository-analysis-2026-07-21
  - p0-decisions-2026-07-21
  - ../architecture/architecture-cgi-kpi-dashboard-security/ARCHITECTURE-SPINE.md
---

# PRD — Security & Multi-User (Post-MVP)

## 0. Purpose

Erweiterung des MVP-Pilots (ohne Auth) um sichere Mehrbenutzerfähigkeit, Rollen, Admin-Bereich, geschützte KI und verschlüsselte API-Key-Verwaltung.

FR-1..FR-21 bleiben gültig. Ergänzung: FR-22..FR-32, NFR-11..NFR-20.  
NFR-8 / AD-6 = historischer MVP → **SUPERSEDED** für diese Ausbaustufe.

## 1. Vision (Delta)

Ein Default-Workspace mit gemeinsamen Portfolio-/Projektdaten für alle aktiven Mitglieder. Persönliche Daten und Sessions sind nutzerbezogen isoliert. KI nur für ADMIN. API-Key administrierbar, verschlüsselt, nie vollständig an Clients.

## 2. Scope

**In:** lokaler Session-Login; USER/ADMIN; User-Mgmt; Admin-UI/API; Default Deny; Workspace-Isolation; private User-Daten; KI nur ADMIN; verschlüsselte Provider-Config; Audit; automatisierte Fremdzugriffs-Tests.

**Out (v1):** konkrete CGI-SSO-Wahl; produktiver Secret-Store-Vendor; E-Mail-Reset; `project_membership`; Multi-Workspace-SaaS; Frontend-only Security; JWT; horizontale Session-Skalierung.

## 3. Verbindliche v1-Entscheidungen (P0 geschlossen 2026-07-21)

### 3.1 Workspace

- Ein **Default-Workspace**; alle bestehenden Projekte/KPIs/Risiken/Meilensteine/Portfolio-Daten werden ihm zugeordnet.
- Alle **aktiven** Workspace-Mitglieder sehen die gemeinsamen Daten.
- **Keine** User↔Projekt-Zuordnung in v1; Schema/API so vorbereiten, dass später `project_membership` möglich ist.

### 3.2 Private Benutzerdaten

Immer isoliert mit `workspace_id` + `user_id` aus dem **Security Context** (nie aus Client-Parametern):

- persönliche Einstellungen, Filter, gespeicherte Ansichten, UI-Präferenzen
- mögliche spätere KI-Fragen/-Verläufe (in v1 **nicht** in PostgreSQL persistiert)

### 3.3 Authentifizierung

Lokaler Login · Spring Security · serverseitige Sessions · sichere Cookies · Login/Logout/`/api/auth/me` · CSRF · Session-Fixation-Schutz · **kein JWT** · **kein** Auth-Token in `localStorage`.  
OIDC/SSO nur architektonisch vorbereitet — keine konkrete CGI-SSO-Aussage.

### 3.4 Bootstrap-Admin

Nicht via Flyway mit festem Passwort. Nur wenn **kein Benutzer** existiert; Credentials ausschließlich aus Env `BOOTSTRAP_ADMIN_USERNAME` / `BOOTSTRAP_ADMIN_PASSWORD`; Hash vor Persistenz; Klartext nie loggen; Force-Change beim ersten Login; danach kein weiterer Bootstrap-Admin. Fehlende Env → **kein** Default-Passwort (siehe Addendum).

### 3.5 Bestehende Daten

Geplante Flyway-Migration: Default-Workspace anlegen, `workspace_id` ergänzen, Daten zuordnen, FKs/Indizes — Daten vollständig erhalten. **Noch nicht implementiert.**

### 3.6 KI

Alle bestehenden KI-Endpunkte nur ADMIN; URL- + Methoden- + Service-Level-AuthZ; Workspace-/Projektberechtigung; Frontend allein unzureichend.

| Methode | Pfad |
|---|---|
| GET | `/api/portfolio/ai/trend-analysis` |
| GET | `/api/projects/{id}/ai/analysis` |
| POST | `/api/projects/{id}/ai/questions` |

### 3.7 KI-Persistenz / Cache

Keine dauerhafte Speicherung von KI-Q&A in PostgreSQL; keine Chat-Historie über Sessions. Cache-Key mind. `workspaceId|projectId|factsAsOf|providerConfigVersion` (+ `userId` wenn nutzerspezifisch). Unsicher → Cache deaktivieren. Config-Change → Cache verwerfen.

### 3.8 Keys

Dev: Master-Key nur Env, nicht in DB/Git, kein hardcoded Fallback; ohne Master-Key keine Entschlüsselung aktivierter DB-Config.  
Prod: externer Secret Store/KMS = **Blocker vor Produktion** (Vendor OFFEN).

### 3.9 Session-Betrieb

v1 = **eine** Backend-Instanz; parallele getrennte Sessions OK. JVM-Sessions nicht multi-instance-fähig; später Spring Session JDBC oder Redis möglich — **keine** Prod-Wahl jetzt.

### 3.10 Rollen

| | USER | ADMIN |
|---|---|---|
| Gemeinsame Workspace-Daten | ja | ja |
| Persönliche Settings | ja | ja |
| KI | nein | ja |
| Admin / User-Mgmt / AI-Config / Audit | nein | ja |

Schutz: letzter aktiver Admin nicht deaktivierbar / nicht degradierbar; API-Key nie vollständig auslesbar; Deaktivierte: kein Login; bestehende Session spätestens am nächsten Request abgewiesen.

## 4. Functional Requirements

**FR-22** Login/Logout; Session; `/api/auth/me`.  
**FR-23** Rollen USER/ADMIN; Backend-AuthZ.  
**FR-24** User-Mgmt (ADMIN) inkl. Last-Admin-Guards.  
**FR-25** Nur Workspace-Daten; Context-IDs; IDOR-Schutz; Private Daten user-isoliert.  
**FR-26** KI nur ADMIN (alle Ist-Endpunkte).  
**FR-27** KI-Provider-Config (ADMIN).  
**FR-28** API-Key verschlüsselt; nie vollständig auslesbar/loggbar.  
**FR-29** Audit sicherheitsrelevanter Admin-Aktionen.  
**FR-30** Parallele getrennte Sessions (Single-Instance).  
**FR-31** Deaktivierte Nutzer: kein Zugang; Session abweisen.  
**FR-32** Connection-Test, Disable, Key-Rotation; Cache-Invalidierung.

## 5. Non-Functional Requirements

NFR-11 Default Deny · NFR-12 Password-Hash · NFR-13 sichere Session-Cookies · NFR-14 CSRF · NFR-15 keine Secrets in Logs/API · NFR-16 automatisierte Fremdzugriffs-/Vermischungstests · NFR-17 externes Master-Key-Material · NFR-18 Missbrauchsschutz Login/KI · NFR-19 parallele Zugriffe ohne Shared-State-Leaks · NFR-20 Auditability.

NFR-8 historisch SUPERSEDED.

## 6. Acceptance Tests (Pflicht später)

Siehe `security-addendum.md` §Acceptance Tests (inkl. Bootstrap, Cache, Master-Key, Private Settings).

## 7. Open Decisions

Nur noch **P1/P2** — siehe Addendum. P0 geschlossen.

## 8. Relationship to MVP

| MVP | Post-MVP |
|---|---|
| AD-6 kein Auth | AD-12 Session-Auth |
| AD-8 Env Gemini-Key | PARTIALLY SUPERSEDED → AD-15/16 |
| Offene APIs | Default Deny + Rollen |
| KI für alle | KI nur ADMIN |
