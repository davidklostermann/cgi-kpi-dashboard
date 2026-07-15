-- Story 3.1: Domain schema — projects, phases, milestones, risks, budget/effort (UUID PKs)

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    planned_end_date DATE NOT NULL,
    actual_end_date DATE,
    progress_percent INTEGER NOT NULL,
    schedule_deviation_days INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE project_phases (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    phase_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    sort_order INTEGER NOT NULL
);

CREATE TABLE milestones (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    due_date DATE NOT NULL,
    completed_date DATE,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE risks (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    severity VARCHAR(30) NOT NULL,
    mitigation_measure VARCHAR(2000),
    status VARCHAR(50) NOT NULL
);

CREATE TABLE project_budgets (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL UNIQUE REFERENCES projects (id) ON DELETE CASCADE,
    planned_budget NUMERIC(14, 2) NOT NULL,
    actual_budget NUMERIC(14, 2) NOT NULL,
    planned_effort_days NUMERIC(10, 2) NOT NULL,
    actual_effort_days NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL
);

CREATE INDEX idx_project_phases_project_id ON project_phases (project_id);
CREATE INDEX idx_milestones_project_id ON milestones (project_id);
CREATE INDEX idx_risks_project_id ON risks (project_id);
