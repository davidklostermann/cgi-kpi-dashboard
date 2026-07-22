---
id: SPEC-user-management-api
companions: ["_bmad-output/implementation-artifacts/epic-13-context.md"]
sources: ["_bmad-output/planning-artifacts/stories/stories-security-multi-user.md"]
---

# SPEC-13.1 — Benutzerverwaltungs-API + Last-Admin-Guards

## Warum
Administratoren benötigen eine Schnittstelle zur Verwaltung von Benutzern, um den operativen Betrieb (Onboarding, Offboarding, Rollenanpassungen) sicherzustellen. Dabei muss verhindert werden, dass sich das System durch Deaktivierung des letzten Administrators selbst aussperrt (Last-Admin-Guard).

## Capabilities

- **CAP-1: Benutzer-CRUD für Admins**
  - **intent:** Ein Administrator kann über eine REST-API Benutzer auflisten, anlegen und bearbeiten (Status, Rolle, Username).
  - **success:** Ein neuer Benutzer kann angelegt werden und erscheint in der Liste; Änderungen an bestehenden Benutzern werden persistiert.

- **CAP-2: Last-Admin-Guard**
  - **intent:** Das System verhindert die Deaktivierung oder Rollenänderung des letzten aktiven Administrators.
  - **success:** Ein Versuch, den einzigen verbleibenden aktiven Admin zu deaktivieren oder zum USER herabzustufen, schlägt mit einem 400 Bad Request fehl.

- **CAP-3: Sofortiger Session-Abwurf**
  - **intent:** Sobald ein Benutzer deaktiviert wird, werden seine laufenden Sessions ungültig.
  - **success:** Ein deaktivierter Benutzer erhält beim nächsten API-Aufruf (auch mit gültigem Session-Cookie) einen 401 Unauthorized.

- **CAP-4: Sicherheits-Audit**
  - **intent:** Alle administrativen Änderungen an Benutzern werden revisionssicher geloggt.
  - **success:** Aktionen wie "User Created", "Role Changed", "User Deactivated" erscheinen im App-Log (oder einer Audit-Tabelle) mit Zeitstempel und ausführendem Admin.

## Constraints

- Alle Endpunkte müssen durch `CurrentUserService.requireAdmin()` geschützt sein.
- Passwörter werden niemals im Klartext verarbeitet oder geloggt.
- Die `workspace_membership` muss beim Anlegen eines Nutzers für den Default-Workspace (v1) mit erstellt werden.

## Non-goals

- E-Mail-Versand bei Passwort-Reset (v1: Admin setzt Passwort manuell/direkt).
- Unterstützung mehrerer Workspaces pro User (v1: 1:1 Beziehung/Default-Workspace).
- Komplexes UI (Teil von Story 13.2).

## Success Signal

Ein Admin kann einen neuen User anlegen, diesen zum Admin befördern, und danach versuchen, sich selbst zu deaktivieren (was fehlschlägt). Nach Deaktivierung des neuen Users kann dieser sich nicht mehr einloggen und bestehende Aufrufe scheitern.

## Code Map

- `backend/src/main/java/com/cgi/kpi/dashboard/api/admin/AdminUserController.java`: REST-Controller für `/api/admin/users`.
- `backend/src/main/java/com/cgi/kjava/com/cgi/kpi/dashboard/domain/service/admin/AdminUserService.java`: Geschäftslogik für User-Management und Guards.
- `backend/src/main/java/com/cgi/kpi/dashboard/api/admin/dto/UserAdminDto.java`: Datentransferobjekt für User-Details inkl. Rolle.
- `backend/src/main/java/com/cgi/kpi/dashboard/domain/model/AuditLog.java`: (Optional/v1) Einfache Entität für Sicherheitsereignisse.

## Tasks & Acceptance

### 1. Admin-Controller & DTOs
- Implementierung `GET /api/admin/users` (Liste aller User mit Rollen).
- Implementierung `POST /api/admin/users` (Username, Passwort, Rolle).
- Implementierung `PUT /api/admin/users/{id}` (Update von `active`, `role`, `mustChangePassword`).
- **AC:** Alle Endpunkte prüfen `requireAdmin()`.

### 2. AdminService & Guards
- Logik für `Last-Admin-Guard`: Vor `updateActiveStatus` oder `updateRole` prüfen, ob mind. ein weiterer aktiver Admin existiert.
- Integration mit `WorkspaceMembership`: User beim Anlegen dem Default-Workspace zuordnen.
- **AC:** `ApiException` bei Verletzung des Last-Admin-Schutzes.

### 3. Session-Invalidierung
- Sicherstellen, dass `CustomUserDetailsService` (oder der Auth-Provider) bei jedem Request den `active`-Status in der DB prüft oder Integration mit `SessionRegistry` zum Invalidieren.
- **AC:** Deaktivierter User fliegt sofort raus.

### 4. Audit-Logging
- Strukturierte Log-Ausgaben oder Speicherung in `AuditLog` für: `USER_CREATED`, `USER_DEACTIVATED`, `ROLE_CHANGED`, `PASSWORD_RESET`.
- **AC:** Logs enthalten `actor_id` (Admin) und `target_id` (betroffener User).

## Verifikation

- **Automatisierte Tests:**
  - Integration-Test: Admin-API Zugriff verweigert für `ROLE_USER`.
  - Unit-Test: `AdminUserService` verhindert Deaktivierung des letzten Admins.
  - Integration-Test: Deaktivierter User verliert API-Zugriff trotz Session-Cookie.
- **Manuelle Demo:**
  - Erstellen eines neuen Nutzers via API.
  - Versuch, den eigenen Account (einziger Admin) zu deaktivieren.
