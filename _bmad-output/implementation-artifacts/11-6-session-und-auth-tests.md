# Story 11.6: Session- und Auth-Basis-Tests

Status: done

## Story

As a Entwickler,
I want automatisierte Auth-Tests,
so that Regressionen auffallen (FR-22, FR-30, FR-31, NFR-11/14).

## Acceptance Criteria

1. Unauth geschützte APIs → 401
2. Bootstrap-Regeln (11.2) abgedeckt
3. Initial-Admin muss Passwort ändern (`mustChangePassword`)
4. Deaktiviert: kein Login
5. Session deaktivierter Nutzer wird beim nächsten Request abgewiesen
6. Parallele Sessions getrennt
7. CSRF: Write ohne Token blockiert (Auth-Endpoints inkl.)

## Tasks

- [x] Bestehende Auth-/Security-/Bootstrap-Tests inventarisieren
- [x] Session-Revalidierung für deaktivierte Nutzer (`ActiveAccountSessionFilter`)
- [x] CSRF-Negativtests für Login/Logout/Change-Password
- [x] Integrationstest: deaktivierter Nutzer nach Login → 401
- [x] Bootstrap-Passwortwechsel-Flow (unabhängig vom DB-Passwort)
- [x] Validierungsfehler mit konkreter Meldung (min. 8 Zeichen)
- [x] Maven- + Frontend-Tests grün

## Review: Initialpasswort / Passwortwechsel (2026-07-22)

### Ursache (kein DB-Passwort-Bug im Code)

`SPRING_DATASOURCE_PASSWORD` wird **nur** für die JDBC-Verbindung verwendet — **nicht** für Benutzerpasswort-Validierung. `AuthService.changePassword` nutzt ausschließlich `newPassword` + `PasswordEncoder` (BCrypt).

Beobachtetes Verhalten entstand höchstwahrscheinlich durch:
1. **Generische Fehlermeldung** bei `@Valid`-Fehlern (`Invalid request body`) statt „mindestens 8 Zeichen“
2. **Ablehnung**, wenn neues Passwort = aktuelles Initialpasswort (`New password must differ from current password`)
3. **Frontend** zeigte englische/rohe Backend-Meldungen nicht verständlich an

Zufallstreffer mit PostgreSQL-Passwort: vermutlich ≥8 Zeichen und ≠ Initialpasswort.

### Korrekturen

- `GlobalExceptionHandler`: Feldvalidierungs-Meldung statt generischem Text
- `ChangePasswordRequestDto`: explizite Validierungsmessages
- `mapChangePasswordError` (Frontend): deutsche UI-Meldungen
- `BootstrapPasswordChangeFlowIntegrationTest`: vollständiger E2E-Flow + DB-Unabhängigkeit
- `AuthServiceChangePasswordTest`: Unit-Nachweis für `newPassword`-Encoding

## Dev Notes

- Bootstrap-Tests: `BootstrapAdmin*IntegrationTest`, `BootstrapAdminServiceTest`
- Auth-Flow: `AuthControllerIntegrationTest`
- Security-Chain: `SecurityFilterChainIntegrationTest`
- Deferred 11.4: Passwortwechsel invalidiert andere Sessions nicht — bewusst offen

### Dev Agent Record

- `ActiveAccountSessionFilter` lädt Principal pro Request neu; deaktivierte/entfernte Nutzer → Logout + 401
- CSRF-Negativtests in `AuthControllerIntegrationTest`
- Bestehende Tests decken parallele Sessions, mustChangePassword, Bootstrap ab

### File List

- `backend/src/main/java/com/cgi/kpi/dashboard/security/web/ActiveAccountSessionFilter.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/security/config/SecurityConfig.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/api/auth/AuthControllerIntegrationTest.java`
- `backend/src/test/java/com/cgi/kpi/dashboard/api/auth/BootstrapPasswordChangeFlowIntegrationTest.java` (neu)
- `backend/src/test/java/com/cgi/kpi/dashboard/api/auth/AuthServiceChangePasswordTest.java` (neu)
- `backend/src/main/java/com/cgi/kpi/dashboard/api/auth/dto/ChangePasswordRequestDto.java`
- `backend/src/main/java/com/cgi/kpi/dashboard/api/error/GlobalExceptionHandler.java`
- `frontend/src/app/shared/utils/change-password-error.util.ts` (neu)
- `frontend/src/app/shared/utils/change-password-error.util.spec.ts` (neu)
- `frontend/src/app/features/auth/change-password-page.component.ts`
- `frontend/src/app/features/auth/change-password-page.component.spec.ts` (neu)
