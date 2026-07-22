---
title: 'SPEC-13.2 — Benutzerverwaltungs-UI'
type: 'feature'
created: '2026-07-22'
status: 'draft'
review_loop_iteration: 0
context: ["_bmad-output/implementation-artifacts/epic-13-context.md", "_bmad-output/implementation-artifacts/spec-user-management-api.md"]
---

## Intent

**Problem:** Administratoren verfügen aktuell über keine grafische Oberfläche, um Benutzer anzulegen, Rollen zu verwalten oder Passwörter zurückzusetzen. Dies macht den operativen Betrieb (Onboarding/Offboarding) aufwendig und fehleranfällig.

**Approach:** Einführung eines neuen Feature-Moduls `features/admin`. Dieses enthält eine tabellarische Übersicht der Benutzer sowie Dialoge für CRUD-Operationen und Passwort-Resets. Die Navigation wird für Benutzer mit der Rolle `ADMIN` um den Punkt „Administration“ erweitert.

## Boundaries & Constraints

**Always:**
- Zugriffsschutz: Die Admin-Oberfläche und der Navigationspunkt sind strikt auf Nutzer mit der Rolle `ADMIN` begrenzt (Client-seitige Prüfung via `AuthService`).
- Last-Admin-Guard: Das UI muss verhindern, dass der aktuell angemeldete Admin seinen eigenen Account deaktiviert oder die Admin-Rolle entzieht, sofern er der letzte aktive Administrator ist.
- Konsistenz: Verwendung von Angular Material (Table, Dialog, FormFields) entsprechend den bestehenden UX-Patterns des Projekts.
- Passwort-Sicherheit: Passwörter werden im UI niemals im Klartext angezeigt (außer ggf. kurzzeitig bei der Neuanlage/Reset zur Bestätigung).

**Ask First:**
- Ob bei einem Passwort-Reset durch den Admin das Feld `mustChangePassword` zwingend auf `true` gesetzt werden soll (vorgesehen: Ja).

**Never:**
- E-Mail-Versand bei Passwort-Reset oder Account-Erstellung (v1).
- Löschen von Benutzern (nur Deaktivierung erlaubt).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Benutzerliste laden | Admin navigiert zu /admin/users | Tabelle zeigt Username, Rolle, Status (Aktiv/Inaktiv), Letzte Änderung | 403 -> Access Denied Seite |
| Benutzer anlegen | Valide Daten (User, Pw, Rolle) | POST an API, Liste aktualisiert sich, Dialog schließt | 409 -> "Benutzer existiert bereits" |
| Letzter Admin Schutz | Admin versucht sich selbst zu deaktivieren | Toggle deaktiviert oder Fehlermeldung bei Klick | 400 -> "Letzter Admin darf nicht deaktiviert werden" |
| Passwort Reset | Admin vergibt neues Pw für User X | PUT /api/admin/users/{id}/password, User X muss bei nächstem Login ändern | API Fehler -> Alert-Message |

## Code Map

- `frontend/src/app/features/admin/` -- Neues Modul-Verzeichnis für Administrations-Features.
- `frontend/src/app/features/admin/admin.routes.ts` -- Definition der Admin-Routen (Lazy Loading).
- `frontend/src/app/features/admin/user-management/user-management.component.ts` -- Hauptkomponente: Liste der Benutzer.
- `frontend/src/app/features/admin/user-management/user-edit-dialog/` -- Dialog für Neuanlage und Bearbeitung (Rolle/Status).
- `frontend/src/app/core/api/admin-user-api.service.ts` -- API-Client für `/api/admin/users`.
- `frontend/src/app/shared/models/admin.model.ts` -- Frontend-Interfaces für `UserAdminResponse` und Requests.
- `frontend/src/app/core/navigation/side-nav.component.html` -- Integration des Admin-Links.
- `backend/src/main/java/com/cgi/kpi/dashboard/api/admin/` -- Ergänzung des Passwort-Reset-Endpoints (BE-Nacharbeit).

## Tasks & Acceptance

**Execution:**
- [ ] `backend/.../AdminUserController.java` -- Hinzufügen von `PUT /users/{id}/password` für administrativen Reset -- Ermöglicht Passwort-Verwaltung durch Admins.
- [ ] `frontend/src/app/shared/models/admin.model.ts` -- Definition `UserAdminDto`, `WorkspaceRole` (Enum) und Request-Interfaces -- Typsicherheit für Admin-Features.
- [ ] `frontend/src/app/core/api/admin-user-api.service.ts` -- Implementierung der CRUD-Methoden inkl. Passwort-Reset -- API-Anbindung.
- [ ] `frontend/src/app/features/admin/user-management/user-management.component.ts` -- Implementierung der Material-Tabelle mit Sortierung und Filtern -- Übersichtliche Verwaltung.
- [ ] `frontend/src/app/features/admin/user-management/user-edit-dialog/user-edit-dialog.component.ts` -- Formular für User-Details und Passwort-Felder -- Interaktive Verwaltung.
- [ ] `frontend/src/app/core/navigation/side-nav.component.ts` -- Logik für Sichtbarkeit des Administration-Links -- Rollenbasierte Navigation.
- [ ] `frontend/src/app/app.routes.ts` -- Registrierung der `/admin` Routen mit `AuthGuard` und Rollenprüfung -- Absicherung der Routen.

**Acceptance Criteria:**
- Ein Administrator sieht den Menüpunkt „Administration“ und kann die Benutzerliste öffnen.
- Ein regulärer Benutzer sieht den Menüpunkt NICHT und erhält beim manuellen Aufruf von `/admin` einen 403/Redirect.
- Die Benutzerliste zeigt den korrekten Status (Aktiv/Inaktiv) und die Rolle (USER/ADMIN).
- Die Deaktivierung des letzten Administrators wird im UI durch Deaktivieren der Aktion verhindert.
- Nach einem Passwort-Reset durch den Admin ist für den betroffenen Benutzer `mustChangePassword` auf `true` gesetzt.

## Design Notes

- **Last-Admin-Guard Logic:** Das UI sollte die `userId` des `AuthService.currentUser` mit der ID des zu bearbeitenden Nutzers vergleichen. Wenn `role == ADMIN` und `active == true`, muss geprüft werden, ob in der Liste weitere aktive Admins existieren (oder die API gibt ein Flag `isDeletable/isDeactivatable` mit).
- **Passwort-Reset UI:** Im Edit-Dialog sollte ein Bereich "Passwort zurücksetzen" existieren, der erst bei Klick die Passwort-Felder einblendet, um versehentliche Änderungen zu vermeiden.

## Verification

**Commands:**
- `npm test` -- expected: Alle Unit-Tests für Admin-Komponenten und Services bestehen.
- `mvn test -Dtest=AdminUserControllerIntegrationTest` -- expected: Backend-Integrationstests inkl. neuem Reset-Endpoint bestehen.

**Manual checks:**
- Login als Admin -> Navigation zu Administration -> User anlegen -> Logout.
- Login mit neuem User -> Passwort-Änderungszwang wird angezeigt.
- Login als Admin -> Versuch, sich selbst zu deaktivieren (Button sollte disabled sein).
- Login als regulärer User -> Administration-Link darf nicht erscheinen.
