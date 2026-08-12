-- Story 11.1: Workspace / AppUser / Membership + projects.workspace_id backfill (AD-14)
-- Extension-Point (v1 unused): optional later table project_membership
--   (id, workspace_id, project_id, user_id, …) for user↔project assignment.
-- Private settings tables (workspace_id + user_id) are deferred to Story 12.1.
-- No app_user / membership / password seeds (AD-12 / T16).

-- Default workspace UUID (must match WorkspaceIds.DEFAULT):
-- c0000000-0000-4000-8000-000000000001

CREATE TABLE workspace (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO workspace (id, name, created_at, updated_at)
VALUES (
    'c0000000-0000-4000-8000-000000000001',
    'Default Workspace',
    TIMESTAMP WITH TIME ZONE '2026-07-21 00:00:00+00',
    TIMESTAMP WITH TIME ZONE '2026-07-21 00:00:00+00'
);

CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_count INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_app_user_username UNIQUE (username)
);

CREATE TABLE workspace_membership (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_workspace_membership_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspace (id),
    CONSTRAINT fk_workspace_membership_user
        FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT uq_workspace_membership_workspace_user
        UNIQUE (workspace_id, user_id),
    CONSTRAINT chk_workspace_membership_role
        CHECK (role IN ('USER', 'ADMIN'))
);

CREATE INDEX idx_workspace_membership_user_id ON workspace_membership (user_id);

ALTER TABLE projects ADD COLUMN workspace_id UUID NULL;

UPDATE projects
SET workspace_id = 'c0000000-0000-4000-8000-000000000001'
WHERE workspace_id IS NULL;

-- Guard: SET NOT NULL fails on H2 and PostgreSQL if any NULL remains after backfill.
ALTER TABLE projects ALTER COLUMN workspace_id SET NOT NULL;

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_workspace
    FOREIGN KEY (workspace_id) REFERENCES workspace (id);

CREATE INDEX idx_projects_workspace_id ON projects (workspace_id);
