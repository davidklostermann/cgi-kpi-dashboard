"""Generate V2__mock_seed.sql with fixed UUIDs (Story 3.3)."""

from pathlib import Path

projects = [
    (1, "Nexus Analytics Pilot", "Acme Fabrications GmbH", "ON_TRACK", "2025-03-01", "2026-06-30", None, 62, 0),
    (2, "Cloud Migration Wave 2", "Beta Systems AG", "AT_RISK", "2025-01-15", "2026-04-30", None, 48, 21),
    (3, "ERP Integration Stream", "Delta Logistics SE", "CRITICAL", "2024-09-01", "2026-03-31", None, 71, 45),
    (4, "Data Platform Modernization", "Epsilon Retail OHG", "AT_RISK", "2025-02-01", "2026-08-31", None, 55, 8),
    (5, "AI Governance Framework", "Gamma Industries KG", "CRITICAL", "2024-11-01", "2026-05-31", None, 78, 5),
    (6, "Customer Portal Redesign", "Horizon Media GmbH", "ON_TRACK", "2025-04-01", "2026-09-30", None, 40, 0),
    (7, "Supply Chain Optimizer", "Ion Trading AG", "AT_RISK", "2025-05-15", "2026-10-31", None, 33, 14),
    (8, "Compliance Automation", "Kappa Finance SE", "ON_TRACK", "2025-06-01", "2026-07-31", None, 58, 12),
    (9, "Legacy Decommission", "Lambda Telecom GmbH", "COMPLETED", "2024-01-01", "2025-12-31", "2025-11-15", 100, 0),
    (10, "IoT Gateway Rollout", "Mu Engineering AG", "ON_TRACK", "2025-07-01", "2026-11-30", None, 25, 0),
    (11, "Document AI Assistant", "Nova Services GmbH", "ON_TRACK", "2025-08-01", "2026-12-31", None, 18, 0),
    (12, "HR Self-Service Hub", "Omega Consulting AG", "AT_RISK", "2025-02-15", "2026-06-15", None, 52, 9),
    (13, "Payment Hub Upgrade", "Pi Insurance SE", "ON_TRACK", "2025-09-01", "2027-01-31", None, 12, 0),
    (14, "Quality Analytics Suite", "Quasar Manufacturing OHG", "AT_RISK", "2025-03-15", "2026-05-15", None, 67, 11),
    (15, "Service Mesh Adoption", "Rho Energy GmbH", "CRITICAL", "2024-12-01", "2026-04-15", None, 44, 28),
    (16, "Threat Detection Platform", "Sigma Health AG", "ON_TRACK", "2025-10-01", "2027-02-28", None, 8, 0),
    (17, "Unified Reporting Layer", "Tau Mobility GmbH", "AT_RISK", "2025-01-01", "2026-03-31", None, 73, 6),
    (18, "Vendor Portal MVP", "Upsilon Pharma SE", "ON_TRACK", "2025-11-01", "2027-03-31", None, 5, 0),
    (19, "Warehouse Automation", "Vega Retail AG", "CRITICAL", "2024-10-15", "2026-02-28", None, 81, 19),
    (20, "Zero Trust Rollout", "Zenith Telecom OHG", "ON_TRACK", "2025-12-01", "2027-04-30", None, 3, 0),
]

budgets = {
    1: (500000, 475000, 120, 108),
    2: (420000, 398000, 95, 88),
    3: (680000, 655000, 180, 172),
    4: (580000, 652000, 140, 155),
    5: (750000, 1012500, 200, 245),
    6: (390000, 365000, 85, 72),
    7: (510000, 488000, 110, 98),
    8: (440000, 481600, 100, 112),
    9: (320000, 315000, 90, 89),
    10: (560000, 520000, 130, 115),
    11: (280000, 265000, 70, 62),
    12: (410000, 395000, 98, 91),
    13: (620000, 590000, 150, 138),
    14: (470000, 455000, 115, 107),
    15: (530000, 512000, 125, 118),
    16: (350000, 330000, 88, 80),
    17: (460000, 442000, 105, 99),
    18: (300000, 285000, 75, 68),
    19: (720000, 698000, 190, 182),
    20: (640000, 610000, 160, 148),
}

PHASE_TEMPLATES = [
    ("Analyse & Konzeption", "ANALYSE", 0, 3),
    ("Umsetzung", "UMSETZUNG", 3, 9),
    ("Rollout & Betrieb", "ROLLOUT", 9, 12),
]


def pid(n: int) -> str:
    return f"a0000000-0000-4000-8000-{n:012x}"


def phase_id(n: int) -> str:
    return f"b0000000-0000-4000-8000-{n:012x}"


def ms_id(n: int) -> str:
    return f"c0000000-0000-4000-8000-{n:012x}"


def risk_id(n: int) -> str:
    return f"d0000000-0000-4000-8000-{n:012x}"


def budget_id(n: int) -> str:
    return f"e0000000-0000-4000-8000-{n:012x}"


def main() -> None:
    lines: list[str] = []
    lines.append("-- Story 3.3: Reproducible mock portfolio (~20 projects, FR-19)")
    lines.append("-- Fixed UUIDs for pilot environments; fictional customer names only")
    lines.append("")
    lines.append(
        "INSERT INTO projects (id, name, customer_name, status, start_date, planned_end_date, "
        "actual_end_date, progress_percent, schedule_deviation_days, created_at) VALUES"
    )

    proj_rows = []
    for n, name, customer, status, start, planned, actual, prog, sched in projects:
        actual_sql = f"'{actual}'" if actual else "NULL"
        proj_rows.append(
            f"    ('{pid(n)}', '{name}', '{customer}', '{status}', '{start}', '{planned}', "
            f"{actual_sql}, {prog}, {sched}, '2026-01-15 08:00:00+00')"
        )
    lines.append(",\n".join(proj_rows) + ";")
    lines.append("")

    lines.append(
        "INSERT INTO project_phases (id, project_id, name, phase_type, start_date, end_date, sort_order) VALUES"
    )
    phase_rows = []
    phase_idx = 1
    for n, _, _, _, start, *_ in projects:
        start_year = int(start[:4])
        start_month = int(start[5:7])
        for sort_order, (phase_name, phase_type, mo_off, dur) in enumerate(PHASE_TEMPLATES, start=1):
            sm = start_month + mo_off
            sy = start_year + (sm - 1) // 12
            sm = ((sm - 1) % 12) + 1
            em = sm + dur
            ey = sy + (em - 1) // 12
            em = ((em - 1) % 12) + 1
            sd = f"{sy:04d}-{sm:02d}-01"
            ed = f"{ey:04d}-{em:02d}-28"
            phase_rows.append(
                f"    ('{phase_id(phase_idx)}', '{pid(n)}', '{phase_name}', '{phase_type}', "
                f"'{sd}', '{ed}', {sort_order})"
            )
            phase_idx += 1
    lines.append(",\n".join(phase_rows) + ";")
    lines.append("")

    lines.append(
        "INSERT INTO milestones (id, project_id, name, due_date, completed_date, status) VALUES"
    )
    ms_rows = []
    ms_idx = 1
    for n, _, _, status, start, planned, actual, _, sched in projects:
        if status == "COMPLETED":
            ms_rows.append(
                f"    ('{ms_id(ms_idx)}', '{pid(n)}', 'Kick-off abgeschlossen', '{start}', '{start}', 'DONE')"
            )
            ms_idx += 1
            ms_rows.append(
                f"    ('{ms_id(ms_idx)}', '{pid(n)}', 'Go-Live', '{planned}', '{actual}', 'DONE')"
            )
        elif sched and sched > 10:
            ms_rows.append(
                f"    ('{ms_id(ms_idx)}', '{pid(n)}', 'Integrations-Meilenstein', '{planned}', NULL, 'OVERDUE')"
            )
            ms_idx += 1
            ms_rows.append(
                f"    ('{ms_id(ms_idx)}', '{pid(n)}', 'Abnahme Phase 2', '{planned}', NULL, 'AT_RISK')"
            )
        else:
            ms_rows.append(
                f"    ('{ms_id(ms_idx)}', '{pid(n)}', 'Design-Freeze', '{planned}', NULL, 'ON_TRACK')"
            )
            ms_idx += 1
            ms_rows.append(
                f"    ('{ms_id(ms_idx)}', '{pid(n)}', 'Pilot-Release', '{planned}', NULL, 'ON_TRACK')"
            )
        ms_idx += 1
    lines.append(",\n".join(ms_rows) + ";")
    lines.append("")

    lines.append(
        "INSERT INTO risks (id, project_id, title, description, severity, mitigation_measure, status) VALUES"
    )
    risk_rows = []
    risk_idx = 1

    def add_risk(proj_n, title, desc, sev, mit, st):
        nonlocal risk_idx
        risk_rows.append(
            f"    ('{risk_id(risk_idx)}', '{pid(proj_n)}', '{title}', '{desc}', "
            f"'{sev}', '{mit}', '{st}')"
        )
        risk_idx += 1

    add_risk(6, "API-Stabilität extern", "Schnittstellenpartner liefert unregelmäßige Releases.", "MEDIUM",
             "Wöchentliche Abstimmung mit Vendor.", "OPEN")
    add_risk(6, "Testdaten unvollständig", "Staging-Umgebung deckt Randfälle nicht ab.", "HIGH",
             "Synthetische Datensätze ergänzen.", "OPEN")
    add_risk(7, "Lieferketten-Störung", "Hardware-Lieferung verzögert sich um 3 Wochen.", "HIGH",
             "Alternativen-Lieferant prüfen.", "OPEN")
    add_risk(7, "Change-Widerstand", "Key-User akzeptieren neue Prozesse zögerlich.", "MEDIUM",
             "Schulungsplan intensivieren.", "OPEN")
    add_risk(7, "Integrationskomplexität", "Altsystem-Schnittstelle undokumentiert.", "HIGH",
             "Reverse-Engineering-Workshop.", "OPEN")
    add_risk(8, "Regulatorische Änderung", "Neue Compliance-Vorgabe tritt früher in Kraft.", "HIGH",
             "Impact-Analyse mit Legal.", "OPEN")

    for pn in [1, 2, 3, 4, 5, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]:
        add_risk(pn, "Ressourcenverfügbarkeit", "Urlaubszeit reduziert Teamkapazität temporär.", "LOW",
                 "Urlaubsplanung mit PMO abstimmen.", "CLOSED")
    for pn in [2, 3, 15, 19]:
        add_risk(pn, "Scope Creep", "Zusätzliche Anforderungen ohne Change Request.", "MEDIUM",
                 "Change-Board etablieren.", "OPEN")

    lines.append(",\n".join(risk_rows) + ";")
    lines.append("")

    lines.append(
        "INSERT INTO project_budgets (id, project_id, planned_budget, actual_budget, "
        "planned_effort_days, actual_effort_days, currency) VALUES"
    )
    budget_rows = []
    for n in range(1, 21):
        planned, actual, pe, ae = budgets[n]
        budget_rows.append(
            f"    ('{budget_id(n)}', '{pid(n)}', {planned:.2f}, {actual:.2f}, {pe:.2f}, {ae:.2f}, 'EUR')"
        )
    lines.append(",\n".join(budget_rows) + ";")

    out = Path(__file__).resolve().parents[1] / "src/main/resources/db/migration/V2__mock_seed.sql"
    out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote {out}")


if __name__ == "__main__":
    main()
