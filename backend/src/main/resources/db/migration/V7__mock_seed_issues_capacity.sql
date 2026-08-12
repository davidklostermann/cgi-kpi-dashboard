-- Seed: operational issues + capacity for Nexus Analytics Pilot (project 001)

UPDATE risks
SET category = 'RESSOURCEN',
    probability_percent = 40,
    owner_name = 'PMO',
    due_date = DATE '2026-08-15',
    metric_1_label = 'Wahrscheinlichkeit',
    metric_1_value = '40 %',
    metric_2_label = 'Status',
    metric_2_value = 'Geschlossen',
    metric_3_label = 'Auswirkung',
    metric_3_value = 'gering',
    metric_4_label = 'Bereich',
    metric_4_value = 'Kapazität'
WHERE id = 'd0000000-0000-4000-8000-000000000007';

INSERT INTO risks (
    id, project_id, title, description, severity, mitigation_measure, status,
    category, probability_percent, owner_name, due_date,
    metric_1_label, metric_1_value, metric_2_label, metric_2_value,
    metric_3_label, metric_3_value, metric_4_label, metric_4_value
) VALUES (
    'd0000000-0000-4000-8000-00000000001b',
    'a0000000-0000-4000-8000-000000000001',
    'API-Freigabe verzögert',
    'Externe API-Freigabe gefährdet den abhängigen Meilenstein und kann den kritischen Pfad verschieben.',
    'HIGH',
    'Freigabe eskalieren und Parallelpfad für Mock-API prüfen.',
    'OPEN',
    'INTEGRATION',
    60,
    'Schnittstellenverantwortung',
    DATE '2026-07-25',
    'Wahrscheinlichkeit', '60 %',
    'Betroffen', 'Meilenstein M-08',
    'Potenzial', '+8 bis 12 Tage',
    'Status', 'Offen'
);

INSERT INTO problems (
    id, project_id, title, description, severity, status, responsible, target_date, countermeasure,
    category,
    metric_1_label, metric_1_value, metric_2_label, metric_2_value,
    metric_3_label, metric_3_value, metric_4_label, metric_4_value
) VALUES
(
    'f0000000-0000-4000-8000-000000000010',
    'a0000000-0000-4000-8000-000000000001',
    'Cloud-Engineering-Kapazität unterdeckt',
    'Bedarf und verfügbare Kapazität im Cloud-Engineering-Team weichen deutlich voneinander ab und wirken auf den Terminplan.',
    'CRITICAL',
    'OPEN',
    'Projektleitung',
    DATE '2026-08-05',
    'Externe Verstärkung vorbereiten und Steering-Entscheidung einholen.',
    'RESSOURCEN',
    'Bedarf', '3,0 FTE',
    'Verfügbar', '1,0 FTE',
    'Lücke', '2,0 FTE',
    'Auswirkung', '+18 Tage'
),
(
    'f0000000-0000-4000-8000-000000000011',
    'a0000000-0000-4000-8000-000000000001',
    'Externe Unterstützung budgetwirksam',
    'Geplante externe Unterstützung stabilisiert Kapazität, erhöht jedoch den Forecast gegenüber dem Planbudget.',
    'HIGH',
    'OPEN',
    'Kaufmännische Steuerung',
    DATE '2026-07-31',
    'Optionen für externe Verstärkung gegenüberstellen und Forecast freigeben lassen.',
    'BUDGET',
    'Planbudget', '1,80 Mio. €',
    'Forecast', '1,97 Mio. €',
    'Abweichung', '+169 Tsd. €',
    'Wirkung', 'Kapazität ↑'
);

INSERT INTO project_role_capacities (
    id, project_id, role_name, required_fte, available_fte, coverage_percent, sort_order
) VALUES
    ('a1000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', 'Cloud Engineering', 3.00, 1.00, 33, 1),
    ('a1000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000001', 'Application Migration', 2.50, 2.30, 92, 2),
    ('a1000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000001', 'Test & Qualität', 1.80, 1.40, 78, 3),
    ('a1000000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000001', 'Change & Adoption', 1.00, 1.00, 100, 4);

INSERT INTO project_capacity_summaries (
    project_id, missing_fte, next_availability_date, overloaded_roles, external_options,
    impact_headline, impact_detail, facts_as_of
) VALUES (
    'a0000000-0000-4000-8000-000000000001',
    2.00,
    DATE '2026-08-05',
    1,
    2,
    'Kapazitätslücke mit Terminwirkung',
    'Die Unterdeckung im Cloud Engineering korreliert mit der beobachteten Terminabweichung. Externe Optionen sind vorbereitet, aber noch nicht entschieden.',
    TIMESTAMP WITH TIME ZONE '2026-07-10 08:00:00+00'
);
