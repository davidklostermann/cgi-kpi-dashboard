---
name: threat-model
parent: architecture-cgi-kpi-dashboard-security
created: 2026-07-21
updated: 2026-07-21
---

# Threat Model — Security Multi-User

P = Wahrscheinlichkeit, I = Impact (L/M/H) — vor Mitigation / mit Plan

| # | Bedrohung | Asset | Weg | I | P | Schutz (v1) | Nachweis |
|---|---|---|---|---|---|---|---|
| T01 | Unauth Zugriff | APIs | HTTP | H | H→L | Default Deny, Session | 401 |
| T02 | Horizontale Escalation | Projektdaten | fremde IDs | H | M | Workspace-Membership (alle sehen gemeinsame WS-Daten; kein Cross-WS) | IDOR/WS-Tests |
| T03 | Vertikale Escalation | Admin/KI | USER→admin/ai | H | M | Role + Service checks | 403 |
| T04 | Manipulierte Context-IDs | Isolation | Body/Query user/workspace | H | M | Context-only | Ignore/Reject-Tests |
| T05 | Session Theft | Account | Cookie | H | M | HttpOnly, Secure, SameSite, TTL | Config-Review |
| T06 | CSRF | Writes | Cross-site | H | M | CSRF | CSRF-Tests |
| T07 | Brute-Force | Accounts | Login spray | H | M | Lockout/Rate Limit | Abuse-Tests |
| T08 | API-Key in Logs | Gemini Key | Logging | H | M | Redaction | Log-Scan |
| T09 | API-Key in Response | Gemini Key | Admin GET | H | M | Mask only | Response-Assert |
| T10 | Key at rest Klartext | Gemini Key | DB dump | H | M | AES-GCM + externes MK | Ciphertext-Assert; MK∉PG |
| T11 | Shared Cache Leak | KI | JVM Map | M | H→L | Tenant Key oder disable | Cross-WS Cache-Test |
| T12 | Unsichere Admin-APIs | User/Key | fehlende AuthZ | H | M | `/api/admin/**` ADMIN | 403 |
| T13 | Letzter Admin weg | Ops | Self-disable | H | L | Guards | API-Tests |
| T14 | Race Admin | Roles/Config | parallel | M | M | Versioning/locking | Concurrency |
| T15 | Stale Session nach Disable | Zugang | alte Cookie | H | M | Reject on next request | Integration |
| T16 | Bootstrap/Seed Passwort | Admin | festes PW in Flyway | H | M→L | Env-only Bootstrap, Force-Change, kein Default | Bootstrap-Tests |
| T17 | Secrets in Git | Keys | Commit | H | L | gitignore + scan | CI |
| T18 | Vulnerable Deps | Runtime | CVE | H | M | Dependency scan | CI |
| T19 | KI-Missbrauch Kosten | Budget | Unauth/USER Q&A | H | H→L | ADMIN-only + Rate Limit | 403 + limit |
| T20 | Actuator | Info | Exposure | M | M | Authz/Prod harden | Config |
| T21 | Private Settings Leak | User prefs | Cross-user read | M | M | user_id Scope | Isolation-Test |
| T22 | Multi-Instance Session Loss | Auth | Scale-out ohne Store | M | L (v1 single) | Dokumentiert; JDBC/Redis später | Architektur |

## Kritische Ist-Risiken (vor Implementierung)

1. Offene APIs inkl. KI (T01/T19)  
2. Unscoped AI-Cache (T11)  
3. Kein Prod-KMS (AD-16 Blocker)  
4. Bootstrap falsch implementiert → T16  
