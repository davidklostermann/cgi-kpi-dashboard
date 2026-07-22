-- Story 12.1: private UI preferences isolated by workspace + user (AD-13/AD-14).
-- AuthZ IDs always come from the security context, never from client parameters.

CREATE TABLE user_ui_preferences (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    preferences_json TEXT NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_user_ui_preferences_workspace FOREIGN KEY (workspace_id) REFERENCES workspace (id),
    CONSTRAINT fk_user_ui_preferences_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT uq_user_ui_preferences_workspace_user UNIQUE (workspace_id, user_id)
);

CREATE INDEX idx_user_ui_preferences_user ON user_ui_preferences (user_id);
