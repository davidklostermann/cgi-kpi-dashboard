-- Fill remaining projects with aggregated Team & Capacity mock data (template from V7/V14).
-- Target projects without capacity yet: 0006–000e, 0010–0012, 0014 (13 projects).
-- Role/skill groups only — no personal details.

INSERT INTO project_role_capacities (
    id, project_id, role_name, required_fte, available_fte, coverage_percent, sort_order
) VALUES
    -- Customer Portal Redesign (...0006) AGILE
    ('a1800000-0000-4000-8000-000000000601', 'a0000000-0000-4000-8000-000000000006', 'Portal Frontend Engineering', 3.00, 2.70, 90, 1),
    ('a1800000-0000-4000-8000-000000000602', 'a0000000-0000-4000-8000-000000000006', 'UX / Design System', 1.50, 1.20, 80, 2),
    ('a1800000-0000-4000-8000-000000000603', 'a0000000-0000-4000-8000-000000000006', 'API Integration', 2.00, 1.60, 80, 3),
    ('a1800000-0000-4000-8000-000000000604', 'a0000000-0000-4000-8000-000000000006', 'QA Automation', 1.50, 1.10, 73, 4),

    -- Supply Chain Optimizer (...0007)
    ('a1800000-0000-4000-8000-000000000701', 'a0000000-0000-4000-8000-000000000007', 'Supply Chain Analytics', 2.50, 1.80, 72, 1),
    ('a1800000-0000-4000-8000-000000000702', 'a0000000-0000-4000-8000-000000000007', 'Optimization Modeling', 2.00, 1.20, 60, 2),
    ('a1800000-0000-4000-8000-000000000703', 'a0000000-0000-4000-8000-000000000007', 'Integration Engineering', 2.00, 1.50, 75, 3),

    -- Compliance Automation (...0008)
    ('a1800000-0000-4000-8000-000000000801', 'a0000000-0000-4000-8000-000000000008', 'Compliance Automation Engineering', 2.00, 1.90, 95, 1),
    ('a1800000-0000-4000-8000-000000000802', 'a0000000-0000-4000-8000-000000000008', 'Rules Engine', 1.50, 1.40, 93, 2),
    ('a1800000-0000-4000-8000-000000000803', 'a0000000-0000-4000-8000-000000000008', 'Audit Evidence Ops', 1.50, 1.20, 80, 3),

    -- Legacy Decommission (...0009) COMPLETED
    ('a1800000-0000-4000-8000-000000000901', 'a0000000-0000-4000-8000-000000000009', 'Decommission Coordination', 1.00, 1.00, 100, 1),
    ('a1800000-0000-4000-8000-000000000902', 'a0000000-0000-4000-8000-000000000009', 'Archive & Cutover Support', 1.50, 1.40, 93, 2),
    ('a1800000-0000-4000-8000-000000000903', 'a0000000-0000-4000-8000-000000000009', 'Knowledge Transfer', 1.00, 1.00, 100, 3),

    -- IoT Gateway Rollout (...000a)
    ('a1800000-0000-4000-8000-000000000a01', 'a0000000-0000-4000-8000-00000000000a', 'Edge Device Engineering', 3.00, 2.40, 80, 1),
    ('a1800000-0000-4000-8000-000000000a02', 'a0000000-0000-4000-8000-00000000000a', 'Gateway Operations', 2.00, 1.50, 75, 2),
    ('a1800000-0000-4000-8000-000000000a03', 'a0000000-0000-4000-8000-00000000000a', 'Field Rollout Coordination', 2.50, 1.80, 72, 3),

    -- Document AI Assistant (...000b)
    ('a1800000-0000-4000-8000-000000000b01', 'a0000000-0000-4000-8000-00000000000b', 'NLP / LLM Engineering', 2.50, 2.20, 88, 1),
    ('a1800000-0000-4000-8000-000000000b02', 'a0000000-0000-4000-8000-00000000000b', 'Document Pipeline', 2.00, 1.70, 85, 2),
    ('a1800000-0000-4000-8000-000000000b03', 'a0000000-0000-4000-8000-00000000000b', 'Prompt & Evaluation Ops', 1.50, 1.00, 67, 3),

    -- HR Self-Service Hub (...000c)
    ('a1800000-0000-4000-8000-000000000c01', 'a0000000-0000-4000-8000-00000000000c', 'HR Portal Engineering', 2.50, 1.90, 76, 1),
    ('a1800000-0000-4000-8000-000000000c02', 'a0000000-0000-4000-8000-00000000000c', 'Identity & Access', 1.50, 1.00, 67, 2),
    ('a1800000-0000-4000-8000-000000000c03', 'a0000000-0000-4000-8000-00000000000c', 'Process Automation', 2.00, 1.40, 70, 3),
    ('a1800000-0000-4000-8000-000000000c04', 'a0000000-0000-4000-8000-00000000000c', 'Change Enablement', 1.50, 1.20, 80, 4),

    -- Payment Hub Upgrade (...000d)
    ('a1800000-0000-4000-8000-000000000d01', 'a0000000-0000-4000-8000-00000000000d', 'Payment Platform Engineering', 3.00, 2.80, 93, 1),
    ('a1800000-0000-4000-8000-000000000d02', 'a0000000-0000-4000-8000-00000000000d', 'Clearing & Settlement', 2.00, 1.70, 85, 2),
    ('a1800000-0000-4000-8000-000000000d03', 'a0000000-0000-4000-8000-00000000000d', 'Security Controls', 1.50, 1.30, 87, 3),

    -- Quality Analytics Suite (...000e)
    ('a1800000-0000-4000-8000-000000000e01', 'a0000000-0000-4000-8000-00000000000e', 'Quality Data Engineering', 2.50, 1.80, 72, 1),
    ('a1800000-0000-4000-8000-000000000e02', 'a0000000-0000-4000-8000-00000000000e', 'Analytics Product', 2.00, 1.40, 70, 2),
    ('a1800000-0000-4000-8000-000000000e03', 'a0000000-0000-4000-8000-00000000000e', 'Shopfloor Integration', 2.00, 1.20, 60, 3),

    -- Threat Detection Platform (...0010)
    ('a1800000-0000-4000-8000-000000001001', 'a0000000-0000-4000-8000-000000000010', 'Detection Engineering', 3.00, 2.60, 87, 1),
    ('a1800000-0000-4000-8000-000000001002', 'a0000000-0000-4000-8000-000000000010', 'SOC Content Ops', 2.00, 1.80, 90, 2),
    ('a1800000-0000-4000-8000-000000001003', 'a0000000-0000-4000-8000-000000000010', 'Threat Intel Integration', 1.50, 1.20, 80, 3),

    -- Unified Reporting Layer (...0011)
    ('a1800000-0000-4000-8000-000000001101', 'a0000000-0000-4000-8000-000000000011', 'Reporting Architecture', 2.00, 1.40, 70, 1),
    ('a1800000-0000-4000-8000-000000001102', 'a0000000-0000-4000-8000-000000000011', 'Semantic Layer Engineering', 2.50, 1.70, 68, 2),
    ('a1800000-0000-4000-8000-000000001103', 'a0000000-0000-4000-8000-000000000011', 'Dashboard Delivery', 2.00, 1.60, 80, 3),

    -- Vendor Portal MVP (...0012) HYBRID
    ('a1800000-0000-4000-8000-000000001201', 'a0000000-0000-4000-8000-000000000012', 'Vendor UX Engineering', 2.00, 1.80, 90, 1),
    ('a1800000-0000-4000-8000-000000001202', 'a0000000-0000-4000-8000-000000000012', 'Onboarding Workflows', 1.50, 1.30, 87, 2),
    ('a1800000-0000-4000-8000-000000001203', 'a0000000-0000-4000-8000-000000000012', 'Integration Testing', 1.50, 1.10, 73, 3),

    -- Zero Trust Rollout (...0014)
    ('a1800000-0000-4000-8000-000000001401', 'a0000000-0000-4000-8000-000000000014', 'Zero Trust Architecture', 2.50, 2.20, 88, 1),
    ('a1800000-0000-4000-8000-000000001402', 'a0000000-0000-4000-8000-000000000014', 'Identity Hardening', 2.00, 1.70, 85, 2),
    ('a1800000-0000-4000-8000-000000001403', 'a0000000-0000-4000-8000-000000000014', 'Endpoint Policy Ops', 2.00, 1.50, 75, 3);

INSERT INTO project_capacity_summaries (
    project_id, missing_fte, next_availability_date, overloaded_roles, external_options,
    impact_headline, impact_detail, facts_as_of
) VALUES
    (
        'a0000000-0000-4000-8000-000000000006',
        1.40,
        DATE '2026-08-10',
        1,
        1,
        'QA und Design leicht unterdeckt',
        'Frontend und API sind weitgehend abgedeckt; UX und QA Automation limitieren die Sprint-Stabilitaet.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-000000000007',
        2.00,
        DATE '2026-08-18',
        2,
        1,
        'Optimization Modeling ist Engpass',
        'Analytics und Integration sind knapp, das Modeling-Team begrenzt die naechsten Optimierungslauefe.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-000000000008',
        0.50,
        DATE '2026-08-05',
        0,
        0,
        'Compliance-Rollen stabil abgedeckt',
        'Automation und Rules Engine sind auf Kurs; Audit Evidence Ops bleibt mit kleiner Reserve.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-000000000009',
        0.10,
        NULL,
        0,
        0,
        'Abschlussphase mit voller Abdeckung',
        'Decommission und Knowledge Transfer sind abgeschlossen bzw. ausreichend besetzt.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-00000000000a',
        1.80,
        DATE '2026-08-22',
        1,
        1,
        'Field Rollout braucht Verstaerkung',
        'Edge Engineering ist stabil; Gateway- und Field-Kapazitaet limitieren parallele Standort-Rollouts.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-00000000000b',
        1.10,
        DATE '2026-08-12',
        1,
        1,
        'Evaluation Ops unter Ziel',
        'NLP Engineering und Pipeline sind abgedeckt; Prompt- und Evaluation-Kapazitaet begrenzt Release-Zyklen.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-00000000000c',
        2.00,
        DATE '2026-08-20',
        2,
        2,
        'Identity und Automation unterdeckt',
        'Portal Engineering ist akzeptabel; Identity & Access sowie Process Automation belasten den Go-Live-Plan.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-00000000000d',
        0.70,
        DATE '2026-08-07',
        0,
        0,
        'Payment Hub weitgehend abgedeckt',
        'Platform und Security Controls sind stabil; Clearing hat nur geringe Unterdeckung.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-00000000000e',
        2.10,
        DATE '2026-08-25',
        2,
        1,
        'Shopfloor Integration kritisch knapp',
        'Data Engineering und Analytics Product sind unter Ziel; Shopfloor Integration bremst die Suite-Einfuehrung.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-000000000010',
        0.90,
        DATE '2026-08-09',
        0,
        1,
        'Detection Engineering leicht unter Ziel',
        'SOC Content und Threat Intel sind stabil; Detection Engineering braucht moderate Nachbesetzung.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-000000000011',
        1.80,
        DATE '2026-08-16',
        2,
        1,
        'Semantic Layer und Architektur knapp',
        'Dashboard Delivery ist akzeptabel; Reporting Architecture und Semantic Layer limitieren die Konsolidierung.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-000000000012',
        0.80,
        DATE '2026-08-11',
        1,
        1,
        'Integration Testing unter Ziel',
        'UX und Onboarding sind weitgehend abgedeckt; Integration Testing begrenzt den MVP-Abschluss.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    ),
    (
        'a0000000-0000-4000-8000-000000000014',
        1.10,
        DATE '2026-08-28',
        1,
        1,
        'Endpoint Policy Ops leicht unterdeckt',
        'Zero Trust Architecture und Identity Hardening sind stabil; Endpoint Policy Ops begrenzt Parallel-Rollouts.',
        TIMESTAMP WITH TIME ZONE '2026-07-15 08:00:00+00'
    );
