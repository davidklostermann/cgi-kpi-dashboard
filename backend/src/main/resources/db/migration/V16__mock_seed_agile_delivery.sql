-- Epic 17 / Story 17.1: Deterministic agile delivery seed
-- UUID namespace: a170… sprints, b170… work items (avoid collisions with milestones/risks)
-- AGILE: Customer Portal Redesign (...0006)
-- HYBRID: Cloud Migration Wave 2 (...0002)
-- Remaining projects keep DEFAULT WATERFALL and have no sprint/work-item rows

UPDATE projects
SET delivery_method = 'AGILE'
WHERE id = 'a0000000-0000-4000-8000-000000000006';

UPDATE projects
SET delivery_method = 'HYBRID'
WHERE id = 'a0000000-0000-4000-8000-000000000002';

-- ---------------------------------------------------------------------------
-- AGILE project ...0006 — five sprints (PAST / PAST / ACTIVE / FUTURE / FUTURE)
-- ---------------------------------------------------------------------------
INSERT INTO project_sprints (
    id, project_id, name, sequence_no, start_date, end_date, lifecycle,
    story_points_planned, story_points_completed, carry_over_points
) VALUES
    ('a1700000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000006',
     'S1', 1, '2026-01-06', '2026-01-19', 'PAST', 40, 38, 2),
    ('a1700000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000006',
     'S2', 2, '2026-01-20', '2026-02-02', 'PAST', 42, 40, 2),
    ('a1700000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000006',
     'S3', 3, '2026-02-03', '2026-02-16', 'ACTIVE', 45, 22, 0),
    ('a1700000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000006',
     'S4', 4, '2026-02-17', '2026-03-02', 'FUTURE', 48, 0, 0),
    ('a1700000-0000-4000-8000-000000000005', 'a0000000-0000-4000-8000-000000000006',
     'S5', 5, '2026-03-03', '2026-03-16', 'FUTURE', 50, 0, 0);

INSERT INTO project_work_items (
    id, project_id, sprint_id, external_key, title, item_type, status, priority,
    story_points, assignee, is_blocker
) VALUES
    ('b1700000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000006',
     'a1700000-0000-4000-8000-000000000001', 'CPR-101', 'Login redesign', 'STORY', 'DONE', 'MEDIUM', 8, 'Team Portal', FALSE),
    ('b1700000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000006',
     'a1700000-0000-4000-8000-000000000001', 'CPR-102', 'Profile settings', 'STORY', 'DONE', 'MEDIUM', 5, 'Team Portal', FALSE),
    ('b1700000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000006',
     'a1700000-0000-4000-8000-000000000002', 'CPR-110', 'Notification center', 'STORY', 'DONE', 'HIGH', 13, 'Team Portal', FALSE),
    ('b1700000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000006',
     'a1700000-0000-4000-8000-000000000003', 'CPR-201', 'Dashboard widgets', 'STORY', 'IN_PROGRESS', 'HIGH', 8, 'Team Portal', FALSE),
    ('b1700000-0000-4000-8000-000000000005', 'a0000000-0000-4000-8000-000000000006',
     'a1700000-0000-4000-8000-000000000003', 'CPR-202', 'SSO session timeout', 'BUG', 'IN_PROGRESS', 'BLOCKER', 5, 'Team Portal', TRUE),
    ('b1700000-0000-4000-8000-000000000006', 'a0000000-0000-4000-8000-000000000006',
     'a1700000-0000-4000-8000-000000000003', 'CPR-203', 'Accessibility fixes', 'TASK', 'TODO', 'MEDIUM', 3, 'Team Portal', FALSE),
    ('b1700000-0000-4000-8000-000000000007', 'a0000000-0000-4000-8000-000000000006',
     NULL, 'CPR-301', 'Offline cache', 'STORY', 'BACKLOG', 'LOW', 8, NULL, FALSE),
    ('b1700000-0000-4000-8000-000000000008', 'a0000000-0000-4000-8000-000000000006',
     'a1700000-0000-4000-8000-000000000003', 'CPR-204', 'Export PDF hangs', 'BUG', 'TODO', 'BLOCKER', 2, 'Team Portal', TRUE);

-- ---------------------------------------------------------------------------
-- HYBRID project ...0002 — three sprints (PAST / ACTIVE / FUTURE)
-- ---------------------------------------------------------------------------
INSERT INTO project_sprints (
    id, project_id, name, sequence_no, start_date, end_date, lifecycle,
    story_points_planned, story_points_completed, carry_over_points
) VALUES
    ('a1700000-0000-4000-8000-000000000011', 'a0000000-0000-4000-8000-000000000002',
     'S1', 1, '2026-01-13', '2026-01-26', 'PAST', 30, 28, 2),
    ('a1700000-0000-4000-8000-000000000012', 'a0000000-0000-4000-8000-000000000002',
     'S2', 2, '2026-01-27', '2026-02-09', 'ACTIVE', 32, 15, 0),
    ('a1700000-0000-4000-8000-000000000013', 'a0000000-0000-4000-8000-000000000002',
     'S3', 3, '2026-02-10', '2026-02-23', 'FUTURE', 35, 0, 0);

INSERT INTO project_work_items (
    id, project_id, sprint_id, external_key, title, item_type, status, priority,
    story_points, assignee, is_blocker
) VALUES
    ('b1700000-0000-4000-8000-000000000011', 'a0000000-0000-4000-8000-000000000002',
     'a1700000-0000-4000-8000-000000000011', 'CMW-41', 'Lift-and-shift batch A', 'STORY', 'DONE', 'HIGH', 13, 'Cloud Squad', FALSE),
    ('b1700000-0000-4000-8000-000000000012', 'a0000000-0000-4000-8000-000000000002',
     'a1700000-0000-4000-8000-000000000012', 'CMW-55', 'Network peering gap', 'BUG', 'IN_PROGRESS', 'BLOCKER', 5, 'Cloud Squad', TRUE),
    ('b1700000-0000-4000-8000-000000000013', 'a0000000-0000-4000-8000-000000000002',
     'a1700000-0000-4000-8000-000000000012', 'CMW-56', 'Cutover checklist', 'TASK', 'TODO', 'MEDIUM', 3, 'Cloud Squad', FALSE),
    ('b1700000-0000-4000-8000-000000000014', 'a0000000-0000-4000-8000-000000000002',
     NULL, 'CMW-70', 'DR runbook', 'STORY', 'BACKLOG', 'MEDIUM', 8, NULL, FALSE);
