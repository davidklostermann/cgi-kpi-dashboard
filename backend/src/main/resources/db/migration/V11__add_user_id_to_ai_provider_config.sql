ALTER TABLE ai_provider_config ADD COLUMN user_id UUID;

-- Assign existing global configs to the default bootstrap admin user
-- IMPORTANT: Replace 'admin' with the actual BOOTSTRAP_ADMIN_USERNAME if it's different in your environment.
UPDATE ai_provider_config
SET user_id = (
    SELECT id FROM app_user
    WHERE username = 'admin' -- Default bootstrap admin username
    LIMIT 1
)
WHERE user_id IS NULL;

ALTER TABLE ai_provider_config ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE ai_provider_config ADD CONSTRAINT fk_ai_provider_config_user
    FOREIGN KEY (user_id) REFERENCES app_user(id);

ALTER TABLE ai_provider_config ADD CONSTRAINT uk_ai_provider_config
    UNIQUE (user_id, provider);