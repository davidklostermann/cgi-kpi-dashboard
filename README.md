# CGI KPI Dashboard — Lokales Entwicklungs-Setup

Monorepo für das interne KPI-Dashboard (Angular + Spring Boot + PostgreSQL).

## Voraussetzungen

| Tool | Version |
|---|---|
| Java | 21 |
| Maven | 3.9.x (oder `./mvnw` im `backend/`-Ordner) |
| Node.js | 20+ |
| npm | 11+ |
| PostgreSQL | 16.x |

## 1. PostgreSQL starten

PostgreSQL lokal installieren und den Dienst starten.

**Datenbank anlegen** (einmalig):

```sql
CREATE DATABASE cgi_kpi_dashboard;
```

> **Hinweis:** Ab Story 3.1 verbindet sich das Backend mit PostgreSQL. Die Datenbank `cgi_kpi_dashboard` muss existieren; **Flyway** legt das Schema beim ersten Start automatisch an (Migrationen unter `backend/src/main/resources/db/migration/`).

### Flyway-Migrationen

- Beim Start wendet Spring Boot Flyway alle SQL-Skripte aus `db/migration/` an (aktuell: `V1__create_domain_schema.sql`, `V2__mock_seed.sql`).
- Bei erneutem Start sind Migrationen **idempotent** — bereits angewendete Versionen werden nicht erneut ausgeführt.
- Schema-History liegt in der Tabelle `flyway_schema_history`.
- Manuelle Prüfung nach Backend-Start:

```sql
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
```

Erwartung: Version `1`, `success = true`.

Optional für spätere Stories — Umgebungsvariablen in `backend/.env` (siehe `.env.example`):

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cgi_kpi_dashboard
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<your-password>
```

## 2. Backend starten

```bash
cd backend
./mvnw spring-boot:run
```

Windows (PowerShell):

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend läuft auf **http://localhost:8080**.

**Smoke-Test:**

```bash
curl http://localhost:8080/actuator/health
```

Erwartete Antwort: `{"status":"UP"}`

### Backend-Secrets (AD-8)

Secrets gehören **nur** ins Backend — nie ins Frontend oder Git.

```bash
cp backend/.env.example backend/.env
# GEMINI_API_KEY etc. in backend/.env eintragen (wird von Git ignoriert)
```

**Erster Admin (Story 11.2):** Wenn noch kein Benutzer in der Datenbank existiert, legt das Backend beim Start optional einen Admin an — nur wenn `BOOTSTRAP_ADMIN_USERNAME` und `BOOTSTRAP_ADMIN_PASSWORD` in der Umgebung gesetzt sind (z. B. in `backend/.env`). Es gibt kein Default-Passwort im Code oder in Flyway. Ohne diese Variablen startet die App normal, Login ist erst nach Konfiguration möglich (ab Story 11.4).

**API-Authentifizierung (Story 11.3/11.4):** Alle `/api/**`-Endpunkte erfordern eine gültige Session (Cookie-basiert). Login via `POST /api/auth/login` (CSRF-Token erforderlich), Logout via `POST /api/auth/logout`, Identität via `GET /api/auth/me`. Bootstrap-Admin muss Initialpasswort über `POST /api/auth/change-password` ändern. Sessions sind JVM-lokal (Single-Backend-Instance v1); horizontale Skalierung erfordert später Spring Session JDBC/Redis.

**Frontend-Authentifizierung (Story 11.5):** Login unter `/login`; Session-Cookie (kein Token in `localStorage`). CSRF-Header für schreibende Requests automatisch via Interceptor.

## 3. Frontend starten

In einem zweiten Terminal:

```bash
cd frontend
npm install
npm start
```

Frontend läuft auf **http://localhost:4200** mit Dev-Proxy:

| Pfad | Ziel |
|---|---|
| `/api/*` | `http://localhost:8080/api/*` |
| `/actuator/*` | `http://localhost:8080/actuator/*` |

Die API-Basis-URL ist in `frontend/src/environments/environment.ts` konfiguriert (`apiBaseUrl: '/api'` — keine Secrets).

### Proxy-Smoke-Test

Mit laufendem Backend und Frontend:

```bash
curl http://localhost:4200/actuator/health
curl http://localhost:4200/api/
```

Der Actuator-Health-Check über den Dev-Proxy sollte `{"status":"UP"}` liefern. API-Endpunkte antworten erst ab späteren Stories (404 ist ohne implementierte Controller erwartbar).

## Tests

```bash
# Backend
cd backend && ./mvnw test

# Frontend
cd frontend && npm test
```

## Projektstruktur

```
cgi-kpi-dashboard/
  backend/          # Spring Boot 3.5.16, Port 8080
  frontend/         # Angular 22, Port 4200, Dev-Proxy
  _bmad-output/     # Planungs- und Implementierungsartefakte
```

Weitere Architekturdetails: `_bmad-output/planning-artifacts/architecture/`.
