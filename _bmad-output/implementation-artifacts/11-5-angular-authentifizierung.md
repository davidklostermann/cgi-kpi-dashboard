# Story 11.5: Angular Authentifizierung

Status: done

## Story

As a Nutzer,
I want eine Login-Seite und sichtbaren Logout,
so that ich die App bedienen kann (FR-22, AD-12).

## Acceptance Criteria

1. Login-Page mit Username/Passwort
2. Shell: User/Rolle/Logout in Top-Nav
3. Auth-Guard → Login bei unauth
4. 401-Interceptor → Login
5. CSRF-Token für Writes (Cookie → Header)
6. Kein Auth-Token in localStorage
7. Logout cleared Client-State (Filter-Signals)
8. UX: Passwort-ändern wenn `mustChangePassword`
9. Frontend-Tests grün

## Tasks

- [x] Auth API + Service + Models
- [x] CSRF + 401 Interceptors
- [x] Guards (auth, must-change-password)
- [x] Login + Change-Password Pages
- [x] Shell Top-Nav + Routes
- [x] Tests (118 Frontend-Tests grün)

### Dev Agent Record

- `AuthService` / `AuthApiService` — Session in Memory, kein localStorage
- `csrfInterceptor` + `unauthorizedInterceptor`
- `authGuard` + `mustChangePasswordGuard`
- Login + Change-Password Pages; Top-Nav mit User/Rolle/Logout
- Logout resettet `PortfolioFilterService`
- Login-UI CGI-konform überarbeitet; zentrale Fehlerabbildung `mapLoginError`
- Frontend-Tests: 129 grün (Stand Review)
