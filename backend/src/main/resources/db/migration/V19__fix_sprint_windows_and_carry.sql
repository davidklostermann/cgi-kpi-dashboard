-- Align sprint demo windows to Aug 2026 and fill ACTIVE carry-over for KPI bar.

-- Rich AGILE (...0006)
UPDATE project_sprints SET start_date = DATE '2026-06-09', end_date = DATE '2026-06-22'
WHERE id = 'a1700000-0000-4000-8000-000000000001';
UPDATE project_sprints SET start_date = DATE '2026-06-23', end_date = DATE '2026-07-06'
WHERE id = 'a1700000-0000-4000-8000-000000000002';
UPDATE project_sprints
SET start_date = DATE '2026-07-07',
    end_date = DATE '2026-07-20',
    carry_over_points = GREATEST(0, story_points_planned - story_points_completed)
WHERE id = 'a1700000-0000-4000-8000-000000000003';
UPDATE project_sprints SET start_date = DATE '2026-07-21', end_date = DATE '2026-08-03'
WHERE id = 'a1700000-0000-4000-8000-000000000004';
UPDATE project_sprints SET start_date = DATE '2026-08-04', end_date = DATE '2026-08-17'
WHERE id = 'a1700000-0000-4000-8000-000000000005';

-- Rich HYBRID (...0002)
UPDATE project_sprints SET start_date = DATE '2026-07-07', end_date = DATE '2026-07-20'
WHERE id = 'a1700000-0000-4000-8000-000000000011';
UPDATE project_sprints
SET start_date = DATE '2026-07-21',
    end_date = DATE '2026-08-03',
    carry_over_points = GREATEST(0, story_points_planned - story_points_completed)
WHERE id = 'a1700000-0000-4000-8000-000000000012';
UPDATE project_sprints SET start_date = DATE '2026-08-04', end_date = DATE '2026-08-17'
WHERE id = 'a1700000-0000-4000-8000-000000000013';

-- Compact V17 packs: shift windows by lifecycle and fill ACTIVE carry-over
UPDATE project_sprints
SET start_date = DATE '2026-07-07',
    end_date = DATE '2026-07-20'
WHERE lifecycle = 'PAST'
  AND id >= 'a1700000-0000-4000-8000-000000000021';

UPDATE project_sprints
SET start_date = DATE '2026-07-21',
    end_date = DATE '2026-08-03',
    carry_over_points = GREATEST(0, story_points_planned - story_points_completed)
WHERE lifecycle = 'ACTIVE'
  AND id >= 'a1700000-0000-4000-8000-000000000021';

UPDATE project_sprints
SET start_date = DATE '2026-08-04',
    end_date = DATE '2026-08-17'
WHERE lifecycle = 'FUTURE'
  AND id >= 'a1700000-0000-4000-8000-000000000021';
