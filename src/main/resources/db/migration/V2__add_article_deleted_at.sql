-- 添加 deleted_at 字段，用于追踪文章软删除时间（30天后自动永久删除）
ALTER TABLE tb_articles ADD COLUMN deleted_at DATETIME NULL COMMENT '删除时间（软删除时记录）' AFTER published_at;

-- 移除 updated_at 的 ON UPDATE CURRENT_TIMESTAMP，改为应用层手动控制
-- 创建时 updated_at 为 NULL，更新时由后端代码设置
ALTER TABLE tb_articles MODIFY COLUMN updated_at DATETIME NULL COMMENT '更新时间（手动控制）';
