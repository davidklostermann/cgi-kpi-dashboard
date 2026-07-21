---
name: security-decisions
parent: architecture-cgi-kpi-dashboard-security
created: 2026-07-21
updated: 2026-07-21
---

# Security Architecture Decisions (AD-12 … AD-18)

## MVP markers

- **AD-6** No auth in pilot → **SUPERSEDED** → AD-12/13  
- **AD-8** Secrets server-side → **PARTIALLY SUPERSEDED** → AD-15/16 (Env bleibt für Master-Key/Bootstrap)  
- **AD-11** CGI EDS — unverändert (Security startet bei AD-12)

---

## AD-12 — Session-basierte Authentifizierung [DECIDED]

- **Kontext:** Mehrbenutzer-SPA; keine SSO-Vorgabe im Repo.
- **Entscheidung:** Spring Security Session; Cookie HttpOnly (+ Secure Prod); Login/Logout/me; CSRF; Fixation-Schutz; Lockout/Rate-Limit; **kein JWT**; kein Token in localStorage. OIDC später vorbereitbar.
- **Betrieb v1:** eine Backend-Instanz; parallele Sessions OK. Multi-Instance später: Spring Session JDBC oder Redis (Wahl OFFEN).
- **Bootstrap:** Env `BOOTSTRAP_ADMIN_USERNAME`/`PASSWORD` nur wenn keine User; Hash; Force-Change; kein Flyway-Passwort; fehlende Env ≠ Default-Passwort.
- **Begründung:** Server-stateful; XSS-härter als localStorage-JWT; passt zu CSRF-Modell.
- **Konsequenzen:** Sticky/Session-Store bei Scale-out nötig.
- **Alternativen:** JWT v1 (abgelehnt); sofort OIDC (vorbereiten, nicht implementieren).
- **Risiken:** Session-Diebstahl, CSRF, schwaches Bootstrap-Handling.
- **Status:** DECIDED (P0)
- **Komponenten:** SecurityConfig, AuthController, BootstrapAdminRunner, Angular AuthService

## AD-13 — Rollen- und objektbezogene Autorisierung [DECIDED]

- **Entscheidung:** USER/ADMIN; Default Deny; `/api/admin/**` und `/**/ai/**` → ADMIN; gemeinsame Daten = aktives Workspace-Membership; private Daten = Context userId; Client-IDs keine AuthZ-Quelle; Last-Admin-Guards; Deaktivierte Sessions abweisen.
- **Status:** DECIDED (P0)
- **Komponenten:** Method Security, Access services, Admin user API

## AD-14 — Workspace-basierte Datenisolierung [DECIDED]

- **Entscheidung:** Ein Default-Workspace; Backfill aller bestehenden fachlichen Daten; alle aktiven Members sehen Portfolio/Projekte; **kein** project_membership in v1; Extension-Point dokumentieren.
- **Status:** DECIDED (P0)
- **Komponenten:** Domain + geplante Flyway (Epic 11/12)

## AD-15 — Verschlüsselte KI-Provider-Konfiguration [DECIDED]

- **Entscheidung:** `ai_provider_config` AES-GCM; maskiert; Decrypt transient; Config-Version für Cache-Bust; Key nie vollständig auslesbar.
- **Status:** DECIDED (P0 Design; Implementierung Epic 13)
- **Komponenten:** EncryptionService, Admin AI API/UI

## AD-16 — Externes Master-Key-Management [DECIDED Dev / BLOCKED Prod]

- **Entscheidung:** Master-Key außerhalb DB; Dev = Env, kein hardcoded Fallback; ohne Key keine Decrypt aktiver DB-Config. Prod = Secret Store/KMS **Vendor OFFEN = Blocker vor Produktion**.
- **Status:** DECIDED (Dev) / OPEN (Prod Vendor)
- **Komponenten:** KeyResolver, Deploy-Runbook

## AD-17 — Security Audit Logging [PROPOSED]

- Unverändert Ziel: Append-only Audit für User-Mgmt, Role, AI-Config, Enable/Disable, Connection-Test (ohne Secrets), Login-Failures aggregiert.
- **Status:** PROPOSED (Epic 14; Regeln P0 Rollen greifen schon in Epic 13)

## AD-18 — Nutzer- und mandantenfähige Caches [DECIDED]

- **Entscheidung:** Cache-Key `workspaceId|projectId|factsAsOf|providerConfigVersion`[+userId]; sonst Cache in Security-v1 **disable**; Config-Change invalidiert.
- **KI-Persistenz v1:** keine Q&A in PostgreSQL.
- **Status:** DECIDED (P0)
- **Komponenten:** ProjectAiAnalysisService (+ Portfolio falls Cache kommt)
