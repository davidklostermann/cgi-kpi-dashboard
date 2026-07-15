---
story_key: 1-1-backend-grundgeruest-spring-boot
epic: 1
baseline_commit: NO_VCS
---

# Story 1.1: Backend-Grundgerüst Spring Boot

Status: done

## Story

Als Entwickler  
möchte ich ein lauffähiges Spring-Boot-Backend im Monorepo  
damit REST-APIs bereitgestellt werden können.

## Acceptance Criteria

1. **Gegeben** ein neues `backend/`-Modul, **wenn** `mvn spring-boot:run` ausgeführt wird, **dann** startet die Anwendung auf Port 8080.
2. **Gegeben** der laufende Server, **wenn** `GET /actuator/health` aufgerufen wird, **dann** antwortet er mit Status UP.
3. **Gegeben** die Paketstruktur, **wenn** geprüft wird, **dann** existieren Seeds für `api`, `application`, `domain`, `kpi`, `kpi.reader`, `ai`, `infrastructure`.

## Tasks / Subtasks

- [x] Task 1: Maven-Modul und Spring Boot Parent (AC: 1)
  - [x] `backend/pom.xml` mit Java 21, Spring Boot 3.5.16, web + actuator
  - [x] `CgiKpiDashboardApplication` Main-Klasse
  - [x] `application.yml` mit `server.port: 8080` und Actuator-Exposure
- [x] Task 2: Layered-Package-Seeds (AC: 3)
  - [x] Pakete `api`, `application`, `domain`, `kpi`, `kpi.reader`, `ai`, `infrastructure` unter `com.cgi.kpi.dashboard`
  - [x] Je Paket `package-info.java` zur Dokumentation der Schicht
- [x] Task 3: Tests (AC: 1, 2)
  - [x] `@SpringBootTest` Context-Load-Test
  - [x] Integrationstest `GET /actuator/health` → UP
- [x] Task 4: Verifikation
  - [x] `mvn test` grün
  - [x] `mvn spring-boot:run` startet (Smoke optional)

### Review Findings

- [x] [Review][Patch] AC1: Port 8080 nur in YAML, nicht per Test verifiziert [backend/src/main/resources/application.yml]
- [x] [Review][Patch] Zwei `@SpringBootTest`-Klassen laden redundant den vollen Context (~2× Startup) [backend/src/test/java/com/cgi/kpi/dashboard/]
- [x] [Review][Patch] Kein `backend/.gitignore` — `target/` würde bei Git-Init mit eingecheckt [backend/]
- [x] [Review][Defer] Kein Maven Wrapper (`mvnw`) — Reproduzierbarkeit [backend/] — deferred, Story 1.3 Dev-Setup
- [x] [Review][Defer] `groupId` `com.cgi.kpi` vs. Base-Package `com.cgi.kpi.dashboard` — deferred, kosmetisch
- [x] [Review][Defer] Mockito JDK-Agent-Warnung in Test-Output — deferred, Spring Boot 3.5 Standard

## Senior Developer Review (AI)

**Datum:** 2026-07-14  
**Ergebnis:** Approved (Patch-Items 2026-07-14 behoben)  
**Reviewer:** BMAD Code Review (Blind Hunter + Edge Case Hunter + Acceptance Auditor)

### Zusammenfassung

Story 1.1 erfüllt die Kern-ACs (Health UP, Paket-Seeds vorhanden, Tests grün). Die Implementierung ist schlank und architekturkonform. Drei verbesserungswürdige Punkte betreffen Testabdeckung (Port 8080), Test-Effizienz und Repo-Hygiene.

### Acceptance Auditor

| AC | Status | Evidenz |
|---|---|---|
| AC1 Port 8080 / `spring-boot:run` | **Teilweise** | `application.yml` setzt Port; kein automatisierter Port-Assert |
| AC2 `/actuator/health` UP | **Erfüllt** | `ActuatorHealthIntegrationTest` |
| AC3 Paket-Seeds | **Erfüllt** | 7× `package-info.java` inkl. `kpi.reader` |

## Dev Notes

- **AD-1:** Single deployable backend — ein Maven-Artefakt.
- **Kein JPA/Flyway/PostgreSQL** in dieser Story (Story 3.x).
- Base-Paket: `com.cgi.kpi.dashboard` [Source: ARCHITECTURE-SPINE.md §Structural Seed]
- Stack: Java 21, Spring Boot 3.5.16, Actuator [Source: ARCHITECTURE-SPINE.md §Stack]
- Business-Endpunkte und Gemini **nicht** in Scope.

### Project Structure Notes

```
backend/
  pom.xml
  src/main/java/com/cgi/kpi/dashboard/
    CgiKpiDashboardApplication.java
    api/package-info.java
    application/package-info.java
    domain/package-info.java
    kpi/package-info.java
    kpi/reader/package-info.java
    ai/package-info.java
    infrastructure/package-info.java
  src/main/resources/application.yml
  src/test/java/.../CgiKpiDashboardApplicationTests.java
  src/test/java/.../ActuatorHealthIntegrationTest.java
```

### References

- [Source: _bmad-output/planning-artifacts/architecture/.../ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/planning-artifacts/stories/stories-mvp.md — Story 1.1]

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

- `mvn test` — 2 Tests, BUILD SUCCESS (Spring Boot 3.5.16, Java 21)

### Completion Notes List

- Spring-Boot-Backend unter `backend/` mit Web + Actuator angelegt.
- Schicht-Pakete gemäß Architecture Spine als `package-info.java` Seeds dokumentiert.
- Health-Endpoint via MockMvc-Integrationstest verifiziert (`status: UP`).
- Kein Git-Repo — `baseline_commit: NO_VCS`.
- Code Review 2026-07-14: Port-Test, Test-Konsolidierung, `.gitignore` angewendet.

### File List

- backend/pom.xml
- backend/.gitignore
- backend/src/main/java/com/cgi/kpi/dashboard/CgiKpiDashboardApplication.java
- backend/src/main/java/com/cgi/kpi/dashboard/api/package-info.java
- backend/src/main/java/com/cgi/kpi/dashboard/application/package-info.java
- backend/src/main/java/com/cgi/kpi/dashboard/domain/package-info.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/package-info.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/reader/package-info.java
- backend/src/main/java/com/cgi/kpi/dashboard/ai/package-info.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/package-info.java
- backend/src/main/resources/application.yml
- backend/src/test/java/com/cgi/kpi/dashboard/CgiKpiDashboardApplicationTests.java

### Change Log

- 2026-07-14: Story 1.1 implementiert — Backend-Grundgerüst Spring Boot 3.5.16
- 2026-07-14: Code-Review-Patches — Port-Assert, Tests konsolidiert, `.gitignore`
