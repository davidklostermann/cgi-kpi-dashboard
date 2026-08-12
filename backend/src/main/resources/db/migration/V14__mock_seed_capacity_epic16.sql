-- Epic 16: additional aggregated project capacity seeds.
-- No personal details: role/skill groups only, with required vs available FTE.

INSERT INTO project_role_capacities (
    id, project_id, role_name, required_fte, available_fte, coverage_percent, sort_order
) VALUES
    ('a1600000-0000-4000-8000-000000000201', 'a0000000-0000-4000-8000-000000000002', 'Cloud Platform Engineering', 4.00, 2.60, 65, 1),
    ('a1600000-0000-4000-8000-000000000202', 'a0000000-0000-4000-8000-000000000002', 'Migration Factory', 3.50, 2.90, 83, 2),
    ('a1600000-0000-4000-8000-000000000203', 'a0000000-0000-4000-8000-000000000002', 'Cutover Management', 1.50, 0.80, 53, 3),
    ('a1600000-0000-4000-8000-000000000204', 'a0000000-0000-4000-8000-000000000002', 'Test Automation', 2.00, 1.60, 80, 4),

    ('a1600000-0000-4000-8000-000000000301', 'a0000000-0000-4000-8000-000000000003', 'ERP Integration Architecture', 2.00, 0.90, 45, 1),
    ('a1600000-0000-4000-8000-000000000302', 'a0000000-0000-4000-8000-000000000003', 'SAP Interface Engineering', 3.00, 1.50, 50, 2),
    ('a1600000-0000-4000-8000-000000000303', 'a0000000-0000-4000-8000-000000000003', 'Data Migration', 2.50, 1.70, 68, 3),
    ('a1600000-0000-4000-8000-000000000304', 'a0000000-0000-4000-8000-000000000003', 'UAT Coordination', 1.50, 1.00, 67, 4),

    ('a1600000-0000-4000-8000-000000000401', 'a0000000-0000-4000-8000-000000000004', 'Data Engineering', 3.00, 2.70, 90, 1),
    ('a1600000-0000-4000-8000-000000000402', 'a0000000-0000-4000-8000-000000000004', 'Cloud Cost Engineering', 1.50, 0.70, 47, 2),
    ('a1600000-0000-4000-8000-000000000403', 'a0000000-0000-4000-8000-000000000004', 'BI Enablement', 2.00, 2.00, 100, 3),

    ('a1600000-0000-4000-8000-000000000501', 'a0000000-0000-4000-8000-000000000005', 'AI Governance Advisory', 2.00, 1.80, 90, 1),
    ('a1600000-0000-4000-8000-000000000502', 'a0000000-0000-4000-8000-000000000005', 'Policy Engineering', 1.50, 1.10, 73, 2),
    ('a1600000-0000-4000-8000-000000000503', 'a0000000-0000-4000-8000-000000000005', 'Risk & Compliance Review', 2.00, 1.20, 60, 3),

    ('a1600000-0000-4000-8000-000000000f01', 'a0000000-0000-4000-8000-00000000000f', 'Platform Security Engineering', 3.00, 1.20, 40, 1),
    ('a1600000-0000-4000-8000-000000000f02', 'a0000000-0000-4000-8000-00000000000f', 'Service Mesh Operations', 2.50, 1.30, 52, 2),
    ('a1600000-0000-4000-8000-000000000f03', 'a0000000-0000-4000-8000-00000000000f', 'Security Testing', 2.00, 0.90, 45, 3),

    ('a1600000-0000-4000-8000-000000001301', 'a0000000-0000-4000-8000-000000000013', 'Automation Engineering', 3.00, 2.10, 70, 1),
    ('a1600000-0000-4000-8000-000000001302', 'a0000000-0000-4000-8000-000000000013', 'Site Rollout Coordination', 2.00, 1.50, 75, 2),
    ('a1600000-0000-4000-8000-000000001303', 'a0000000-0000-4000-8000-000000000013', 'Supplier Integration', 1.50, 0.90, 60, 3);

INSERT INTO project_capacity_summaries (
    project_id, missing_fte, next_availability_date, overloaded_roles, external_options,
    impact_headline, impact_detail, facts_as_of
) VALUES
    (
        'a0000000-0000-4000-8000-000000000002',
        3.10,
        DATE '2026-08-12',
        2,
        2,
        'Rollenabdeckung fuer Cutover unter Ziel',
        'Cloud Platform und Cutover Management sind unterdeckt; externe Optionen reduzieren Terminrisiko nur bei zeitnaher Entscheidung.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-000000000003',
        3.90,
        DATE '2026-08-19',
        3,
        1,
        'Integrationsskills kritisch unterdeckt',
        'Architektur- und SAP-Interface-Kapazitaet reichen nicht fuer die parallelen Integrationspakete; UAT bleibt abhaengig von Nachbesetzung.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-000000000004',
        1.10,
        DATE '2026-08-01',
        1,
        1,
        'Kostensteuerungs-Skill knapp',
        'Cloud Cost Engineering ist der Engpass fuer Forecast-Validierung; technische Umsetzung bleibt weitgehend abgedeckt.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-000000000005',
        1.40,
        DATE '2026-08-08',
        1,
        2,
        'Compliance Review braucht Verstaerkung',
        'Governance Advisory ist stabil, Risk & Compliance Review limitiert jedoch die Freigabe der Playbooks.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-00000000000f',
        4.10,
        DATE '2026-08-26',
        3,
        2,
        'Security- und Plattformrollen kritisch',
        'Service-Mesh-Rollout und Security Testing sind gleichzeitig unterdeckt; externe Optionen muessen vor dem naechsten Steering geklaert werden.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-000000000013',
        2.00,
        DATE '2026-08-14',
        1,
        1,
        'Rollout-Kapazitaet teilweise unter Ziel',
        'Automation Engineering und Supplier Integration bleiben knapp, die Site-Koordination ist fuer den naechsten Standort ausreichend stabil.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    );
