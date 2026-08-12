"""Generate V5 trend snapshot migration (12 months per project)."""
from datetime import date

try:
    from dateutil.relativedelta import relativedelta
except ImportError:
    def relativedelta(months=0):
        class R:
            pass
        return R()

projects = [
    ("a0000000-0000-4000-8000-000000000001", 57, 451250, None, "ON_TRACK", 62, 475000, 0, "ON_TRACK", 0),
    ("a0000000-0000-4000-8000-000000000002", 43, 378100, 19, "AT_RISK", 48, 398000, 21, "AT_RISK", 1),
    ("a0000000-0000-4000-8000-000000000003", 66, 622250, 43, "CRITICAL", 71, 655000, 45, "CRITICAL", 1),
    ("a0000000-0000-4000-8000-000000000004", 50, 619400, 6, "AT_RISK", 55, 652000, 8, "AT_RISK", 0),
    ("a0000000-0000-4000-8000-000000000005", 73, 961875, 3, "CRITICAL", 78, 1012500, 5, "CRITICAL", 0),
    ("a0000000-0000-4000-8000-000000000006", 35, 346750, None, "ON_TRACK", 40, 365000, 0, "ON_TRACK", 2),
    ("a0000000-0000-4000-8000-000000000007", 28, 463600, 12, "AT_RISK", 33, 488000, 14, "AT_RISK", 3),
    ("a0000000-0000-4000-8000-000000000008", 53, 457520, 10, "ON_TRACK", 58, 481600, 12, "ON_TRACK", 1),
    ("a0000000-0000-4000-8000-000000000009", 95, 299250, None, "COMPLETED", 100, 315000, 0, "COMPLETED", 0),
    ("a0000000-0000-4000-8000-00000000000a", 20, 494000, None, "ON_TRACK", 25, 520000, 0, "ON_TRACK", 0),
    ("a0000000-0000-4000-8000-00000000000b", 13, 251750, None, "ON_TRACK", 18, 265000, 0, "ON_TRACK", 0),
    ("a0000000-0000-4000-8000-00000000000c", 47, 375250, 7, "AT_RISK", 52, 395000, 9, "AT_RISK", 0),
    ("a0000000-0000-4000-8000-00000000000d", 7, 560500, None, "ON_TRACK", 12, 590000, 0, "ON_TRACK", 0),
    ("a0000000-0000-4000-8000-00000000000e", 62, 432250, 9, "AT_RISK", 67, 455000, 11, "AT_RISK", 0),
    ("a0000000-0000-4000-8000-00000000000f", 39, 486400, 26, "CRITICAL", 44, 512000, 28, "CRITICAL", 0),
    ("a0000000-0000-4000-8000-000000000010", 3, 313500, None, "ON_TRACK", 8, 330000, 0, "ON_TRACK", 0),
    ("a0000000-0000-4000-8000-000000000011", 68, 419900, 4, "ON_TRACK", 73, 442000, 6, "AT_RISK", 0),
    ("a0000000-0000-4000-8000-000000000012", 0, 270750, None, "ON_TRACK", 5, 285000, 0, "ON_TRACK", 0),
    ("a0000000-0000-4000-8000-000000000013", 76, 663100, 17, "CRITICAL", 81, 698000, 19, "CRITICAL", 1),
    ("a0000000-0000-4000-8000-000000000014", 0, 579500, None, "ON_TRACK", 3, 610000, 0, "ON_TRACK", 0),
]

months: list[date] = []
cursor = date(2025, 8, 1)
while cursor <= date(2026, 7, 1):
    months.append(cursor)
    if cursor.month == 12:
        cursor = date(cursor.year + 1, 1, 1)
    else:
        cursor = date(cursor.year, cursor.month + 1, 1)

rows: list[str] = []
idx = 0
for project in projects:
    pid = project[0]
    june_prog, june_budget, june_dev, june_status = project[1], project[2], project[3], project[4]
    july_prog, july_budget, july_dev, july_status, july_risks = project[5], project[6], project[7], project[8], project[9]

    for month in months:
        if month == date(2026, 6, 1):
            prog, budget, dev, status, risks = june_prog, june_budget, june_dev, june_status, 0
        elif month == date(2026, 7, 1):
            prog, budget, dev, status, risks = july_prog, july_budget, july_dev, july_status, july_risks
        else:
            months_before_june = (date(2026, 6, 1).year - month.year) * 12 + (date(2026, 6, 1).month - month.month)
            prog = max(0, min(100, june_prog - months_before_june * 3))
            budget = round(june_budget * (prog / june_prog if june_prog else 0.85), 2)
            if june_dev is None:
                dev = None
            else:
                dev = max(0, june_dev - months_before_june)
            status = june_status
            risks = 0

        dev_sql = "NULL" if dev is None else str(int(dev))
        sid = f"f2000000-0000-4000-8000-{idx:012x}"
        rows.append(
            f"    ('{sid}', '{pid}', DATE '{month.isoformat()}', {prog}, {budget:.2f}, {dev_sql}, '{status}', {risks})"
        )
        idx += 1

out = [
    "-- Story 5.4 review: replace 2-month snapshots with 12-month history (2025-08 .. 2026-07)",
    "DELETE FROM project_report_snapshots;",
    "INSERT INTO project_report_snapshots (id, project_id, snapshot_date, progress_percent, actual_budget, schedule_deviation_days, status, open_risk_count) VALUES",
    ",\n".join(rows) + ";",
]

path = "src/main/resources/db/migration/V5__mock_seed_trend_history.sql"
with open(path, "w", encoding="utf-8") as handle:
    handle.write("\n".join(out))
print(f"Wrote {path} with {len(rows)} rows")
