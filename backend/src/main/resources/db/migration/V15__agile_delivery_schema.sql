-- Epic 17 / Story 17.1: Agile delivery model (additive; no changes to V1–V14)

ALTER TABLE projects
    ADD COLUMN delivery_method VARCHAR(20) NOT NULL DEFAULT 'WATERFALL';

ALTER TABLE projects
    ADD CONSTRAINT chk_projects_delivery_method
        CHECK (delivery_method IN ('AGILE', 'HYBRID', 'WATERFALL'));

CREATE TABLE project_sprints (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name VARCHAR(40) NOT NULL,
    sequence_no INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    lifecycle VARCHAR(20) NOT NULL,
    story_points_planned INTEGER NOT NULL,
    story_points_completed INTEGER NOT NULL,
    carry_over_points INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_project_sprints_lifecycle CHECK (lifecycle IN ('PAST', 'ACTIVE', 'FUTURE')),
    CONSTRAINT chk_project_sprints_sp_nonneg CHECK (
        story_points_planned >= 0
        AND story_points_completed >= 0
        AND carry_over_points >= 0
    ),
    CONSTRAINT chk_project_sprints_date_order CHECK (end_date >= start_date),
    CONSTRAINT uq_project_sprints_project_sequence UNIQUE (project_id, sequence_no),
    CONSTRAINT uq_project_sprints_project_id_id UNIQUE (project_id, id)
);

CREATE INDEX idx_project_sprints_project_id ON project_sprints (project_id);

CREATE TABLE project_work_items (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    sprint_id UUID,
    external_key VARCHAR(40) NOT NULL,
    title VARCHAR(300) NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    story_points INTEGER,
    assignee VARCHAR(120),
    is_blocker BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_project_work_items_type CHECK (item_type IN ('STORY', 'BUG', 'TASK')),
    CONSTRAINT chk_project_work_items_status CHECK (
        status IN ('BACKLOG', 'TODO', 'IN_PROGRESS', 'DONE')
    ),
    CONSTRAINT chk_project_work_items_priority CHECK (
        priority IN ('LOW', 'MEDIUM', 'HIGH', 'BLOCKER')
    ),
    CONSTRAINT chk_project_work_items_sp_nonneg CHECK (
        story_points IS NULL OR story_points >= 0
    ),
    CONSTRAINT uq_project_work_items_project_external_key UNIQUE (project_id, external_key),
    CONSTRAINT fk_project_work_items_sprint_same_project
        FOREIGN KEY (project_id, sprint_id)
        REFERENCES project_sprints (project_id, id)
        ON DELETE SET NULL
);

CREATE INDEX idx_project_work_items_project_id ON project_work_items (project_id);
CREATE INDEX idx_project_work_items_sprint_id ON project_work_items (sprint_id);
CREATE INDEX idx_project_work_items_blocker ON project_work_items (project_id, is_blocker);
