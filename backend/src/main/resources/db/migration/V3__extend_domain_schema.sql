-- Story 3.6: Additive domain extension — project master data, problems, report snapshots (FR-5, FR-6, FR-21)

ALTER TABLE projects ADD COLUMN project_lead VARCHAR(200);
ALTER TABLE projects ADD COLUMN last_data_update TIMESTAMP WITH TIME ZONE;
ALTER TABLE projects ADD COLUMN predicted_end_date DATE;

CREATE TABLE problems (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    severity VARCHAR(30) NOT NULL,
    status VARCHAR(50) NOT NULL,
    responsible VARCHAR(200),
    target_date DATE,
    countermeasure VARCHAR(2000)
);

CREATE TABLE project_report_snapshots (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    snapshot_date DATE NOT NULL,
    progress_percent INTEGER NOT NULL,
    actual_budget NUMERIC(14, 2) NOT NULL,
    schedule_deviation_days INTEGER,
    status VARCHAR(50) NOT NULL,
    open_risk_count INTEGER NOT NULL
);

CREATE INDEX idx_problems_project_id ON problems (project_id);
CREATE INDEX idx_project_report_snapshots_project_id ON project_report_snapshots (project_id);
