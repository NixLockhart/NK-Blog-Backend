-- 添加 deleted_at 字段，用于追踪评论和留言的软删除时间（30天后自动永久删除）
ALTER TABLE tb_comments ADD COLUMN deleted_at DATETIME NULL COMMENT '删除时间（软删除时记录）' AFTER created_at;
ALTER TABLE tb_messages ADD COLUMN deleted_at DATETIME NULL COMMENT '删除时间（软删除时记录）' AFTER created_at;
