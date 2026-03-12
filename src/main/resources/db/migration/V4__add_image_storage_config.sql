-- V4: Insert default image storage configuration
INSERT INTO tb_site_configs (config_key, config_value, config_type, description, is_public, created_at, updated_at)
VALUES ('image_storage', '{"mode":"local","cdn":{}}', 'json', '图片存储配置（本地/图床）', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE config_key = config_key;
