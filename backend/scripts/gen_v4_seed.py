"""One-off generator for V4__mock_seed_extension.sql — run from backend/."""

PROJECTS = [
    ("001", "Dr. Anna Keller", "2026-07-10T08:00:00+00", "2026-06-30", 62, 475000.00, 0, "ON_TRACK", 0),
    ("002", "Markus Brenner", "2026-07-08T08:00:00+00", "2026-06-15", 48, 398000.00, 21, "AT_RISK", 1),
    ("003", "Sandra Vogt", "2026-07-09T08:00:00+00", "2026-04-15", 71, 655000.00, 45, "CRITICAL", 1),
    ("004", "Thomas Richter", "2026-07-11T08:00:00+00", "2026-08-31", 55, 652000.00, 8, "AT_RISK", 0),
    ("005", "Julia Hartmann", "2026-07-07T08:00:00+00", "2026-07-31", 78, 1012500.00, 5, "CRITICAL", 0),
    ("006", "Peter Lang", "2026-07-12T08:00:00+00", "2026-09-30", 40, 365000.00, 0, "ON_TRACK", 2),
    ("007", "Elena Fischer", "2026-07-06T08:00:00+00", "2026-11-15", 33, 488000.00, 14, "AT_RISK", 3),
    ("008", "Michael Braun", "2026-07-10T08:00:00+00", "2026-07-31", 58, 481600.00, 12, "ON_TRACK", 1),
    ("009", "Laura Schneider", "2026-07-05T08:00:00+00", "2025-12-31", 100, 315000.00, 0, "COMPLETED", 0),
    ("00a", "Christian Wolf", "2026-07-11T08:00:00+00", "2026-11-30", 25, 520000.00, 0, "ON_TRACK", 0),
    ("00b", "Nicole Berger", "2026-07-09T08:00:00+00", "2026-12-31", 18, 265000.00, 0, "ON_TRACK", 0),
    ("00c", "Sabine Krause", "2026-07-08T08:00:00+00", "2026-06-15", 52, 395000.00, 9, "AT_RISK", 0),
    ("00d", "Daniel Koch", "2026-07-10T08:00:00+00", "2027-01-31", 12, 590000.00, 0, "ON_TRACK", 0),
    ("00e", "Frank Neumann", "2026-07-07T08:00:00+00", "2026-05-15", 67, 455000.00, 11, "AT_RISK", 0),
    ("00f", "Katrin Schulz", "2026-07-06T08:00:00+00", "2026-05-01", 44, 512000.00, 28, "CRITICAL", 0),
    ("010", "Oliver Weiss", "2026-04-01T08:00:00+00", "2027-02-28", 8, 330000.00, 0, "ON_TRACK", 0),
    ("011", "Heike Zimmermann", "2026-07-09T08:00:00+00", "2026-04-10", 73, 442000.00, 6, "AT_RISK", 0),
    ("012", "Ralf Peters", "2026-07-11T08:00:00+00", "2027-03-31", 5, 285000.00, 0, "ON_TRACK", 0),
    ("013", "Monika Lehmann", "2026-07-05T08:00:00+00", "2026-03-15", 81, 698000.00, 19, "CRITICAL", 1),
    ("014", "Stefan Huber", "2026-07-12T08:00:00+00", "2027-05-15", 3, 610000.00, 0, "ON_TRACK", 0),
]

PROBLEMS = [
    ("f0000000-0000-4000-8000-000000000001", "002", "Change-Request-Stau", "Offene CRs blockieren Release-Train.", "HIGH", "OPEN", "Markus Brenner", "2026-05-30", "Priorisierungs-Workshop"),
    ("f0000000-0000-4000-8000-000000000002", "003", "Schnittstellen-Ausfall", "ERP-Adapter liefert unvollständige Buchungen.", "CRITICAL", "OPEN", "Sandra Vogt", "2026-04-01", "Hotfix mit Partner"),
    ("f0000000-0000-4000-8000-000000000003", "005", "Budget-Überschreitung aktiv", "Ist-Budget übersteigt Plan deutlich.", "HIGH", "OPEN", "Julia Hartmann", "2026-06-30", "Scope-Reduktion prüfen"),
    ("f0000000-0000-4000-8000-000000000004", "007", "Hardware-Verzug", "Edge-Gateways nicht termingerecht geliefert.", "HIGH", "OPEN", "Elena Fischer", "2026-08-15", "Alternativ-Lieferant"),
    ("f0000000-0000-4000-8000-000000000005", "00f", "Mesh-Rollout gestoppt", "Produktions-Cluster nicht migrationsbereit.", "CRITICAL", "OPEN", "Katrin Schulz", "2026-04-20", "Pilot-Cluster isolieren"),
    ("f0000000-0000-4000-8000-000000000006", "013", "Integrations-Meilenstein überfällig", "Meilenstein OVERDUE blockiert Abnahme.", "CRITICAL", "OPEN", "Monika Lehmann", "2026-02-28", "Eskalation an Steuerkreis"),
    ("f0000000-0000-4000-8000-000000000007", "004", "Datenqualität Reporting", "Masterdaten inkonsistent in KPI-Layer.", "MEDIUM", "OPEN", "Thomas Richter", "2026-07-31", "Data-Steward-Runde"),
    ("f0000000-0000-4000-8000-000000000008", "00c", "Testfenster verschoben", "UAT-Slot verloren wegen Abhängigkeit.", "MEDIUM", "OPEN", "Sabine Krause", "2026-06-01", "Neuen Slot reservieren"),
]

PREV_STATUS = {
    "011": "ON_TRACK",
}


def pid(hex_suffix: str) -> str:
    return f"a0000000-0000-4000-8000-000000000{hex_suffix}"


def main() -> None:
    lines = ["-- Story 3.7: Mock seed extension (FR-19, FR-20, FR-21)", ""]

    lines.append("UPDATE projects SET project_lead = CASE id")
    for hex_id, lead, _, _, *_ in PROJECTS:
        lines.append(f"    WHEN '{pid(hex_id)}' THEN '{lead}'")
    lines.append("END;")
    lines.append("")

    lines.append("UPDATE projects SET last_data_update = CAST(CASE id")
    for hex_id, _, ldu, _, *_ in PROJECTS:
        ts = ldu.replace("T", " ")
        lines.append(f"    WHEN '{pid(hex_id)}' THEN '{ts}'")
    lines.append("END AS TIMESTAMP WITH TIME ZONE);")
    lines.append("")

    lines.append("UPDATE projects SET predicted_end_date = CASE id")
    for hex_id, _, _, ped, *_ in PROJECTS:
        lines.append(f"    WHEN '{pid(hex_id)}' THEN DATE '{ped}'")
    lines.append("END;")
    lines.append("")

    lines.append("INSERT INTO problems (id, project_id, title, description, severity, status, responsible, target_date, countermeasure) VALUES")
    problem_rows = []
    for row in PROBLEMS:
        uuid, proj, title, desc, sev, status, resp, target, cm = row
        problem_rows.append(
            f"    ('{uuid}', '{pid(proj)}', '{title}', '{desc}', '{sev}', '{status}', '{resp}', DATE '{target}', '{cm}')"
        )
    lines.append(",\n".join(problem_rows) + ";")
    lines.append("")

    lines.append(
        "INSERT INTO project_report_snapshots (id, project_id, snapshot_date, progress_percent, actual_budget, schedule_deviation_days, status, open_risk_count) VALUES"
    )
    snap_rows = []
    snap_idx = 1
    for hex_id, _, _, _, progress, budget, sched, status, risks in PROJECTS:
        prev_progress = max(progress - 5, 0)
        prev_budget = round(budget * 0.95, 2)
        prev_sched = max((sched or 0) - 2, 0)
        prev_status = PREV_STATUS.get(hex_id, status)
        prev_risks = max(risks - 1, 0)
        snap_rows.append(
            f"    ('f1000000-0000-4000-8000-{snap_idx:012x}', '{pid(hex_id)}', DATE '2026-06-01', {prev_progress}, {prev_budget:.2f}, {prev_sched if sched else 'NULL'}, '{prev_status}', {prev_risks})"
        )
        snap_idx += 1
        sched_sql = str(sched) if sched is not None else "NULL"
        snap_rows.append(
            f"    ('f1000000-0000-4000-8000-{snap_idx:012x}', '{pid(hex_id)}', DATE '2026-07-01', {progress}, {budget:.2f}, {sched_sql}, '{status}', {risks})"
        )
        snap_idx += 1
    lines.append(",\n".join(snap_rows) + ";")

    path = "src/main/resources/db/migration/V4__mock_seed_extension.sql"
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines) + "\n")
    print(f"Wrote {path}")


if __name__ == "__main__":
    main()
