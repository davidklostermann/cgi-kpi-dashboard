-- Project operational issues enrichment + team capacity (detail facts sections)

ALTER TABLE risks ADD COLUMN category VARCHAR(50);
ALTER TABLE risks ADD COLUMN probability_percent INTEGER;
ALTER TABLE risks ADD COLUMN owner_name VARCHAR(200);
ALTER TABLE risks ADD COLUMN due_date DATE;
ALTER TABLE risks ADD COLUMN metric_1_label VARCHAR(80);
ALTER TABLE risks ADD COLUMN metric_1_value VARCHAR(120);
ALTER TABLE risks ADD COLUMN metric_2_label VARCHAR(80);
ALTER TABLE risks ADD COLUMN metric_2_value VARCHAR(120);
ALTER TABLE risks ADD COLUMN metric_3_label VARCHAR(80);
ALTER TABLE risks ADD COLUMN metric_3_value VARCHAR(120);
ALTER TABLE risks ADD COLUMN metric_4_label VARCHAR(80);
ALTER TABLE risks ADD COLUMN metric_4_value VARCHAR(120);

ALTER TABLE problems ADD COLUMN category VARCHAR(50);
ALTER TABLE problems ADD COLUMN metric_1_label VARCHAR(80);
ALTER TABLE problems ADD COLUMN metric_1_value VARCHAR(120);
ALTER TABLE problems ADD COLUMN metric_2_label VARCHAR(80);
ALTER TABLE problems ADD COLUMN metric_2_value VARCHAR(120);
ALTER TABLE problems ADD COLUMN metric_3_label VARCHAR(80);
ALTER TABLE problems ADD COLUMN metric_3_value VARCHAR(120);
ALTER TABLE problems ADD COLUMN metric_4_label VARCHAR(80);
ALTER TABLE problems ADD COLUMN metric_4_value VARCHAR(120);

CREATE TABLE project_role_capacities (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    role_name VARCHAR(120) NOT NULL,
    required_fte NUMERIC(6, 2) NOT NULL,
    available_fte NUMERIC(6, 2) NOT NULL,
    coverage_percent INTEGER NOT NULL,
    sort_order INTEGER NOT NULL
);

CREATE TABLE project_capacity_summaries (
    project_id UUID PRIMARY KEY REFERENCES projects (id) ON DELETE CASCADE,
    missing_fte NUMERIC(6, 2) NOT NULL,
    next_availability_date DATE,
    overloaded_roles INTEGER NOT NULL,
    external_options INTEGER NOT NULL,
    impact_headline VARCHAR(200) NOT NULL,
    impact_detail VARCHAR(1000) NOT NULL,
    facts_as_of TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_project_role_capacities_project_id ON project_role_capacities (project_id);
