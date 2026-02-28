-- =====================================================
-- Flyway Migration Script
-- Version: V1
-- Description: 初始化数据库表结构和基础数据
-- =====================================================

-- 分类表
CREATE TABLE IF NOT EXISTS tb_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    slug VARCHAR(50) NOT NULL UNIQUE COMMENT 'URL友好名称',
    description VARCHAR(200) COMMENT '分类描述',
    icon VARCHAR(100) COMMENT '分类图标',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    article_count INT DEFAULT 0 COMMENT '文章数量（冗余字段）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 文章表
CREATE TABLE IF NOT EXISTS tb_articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    summary VARCHAR(500) COMMENT '文章摘要',
    content_path VARCHAR(500) NOT NULL COMMENT 'Markdown文件相对路径',
    cover_image VARCHAR(500) COMMENT '封面图片路径',
    category_id BIGINT COMMENT '分类ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 0=已删除, 1=已发布, 2=草稿',
    views BIGINT DEFAULT 0 COMMENT '浏览量',
    likes BIGINT DEFAULT 0 COMMENT '点赞数',
    comments_count INT DEFAULT 0 COMMENT '评论数（冗余字段）',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶: 0=否, 1=是',
    published_at DATETIME COMMENT '发布时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_published (published_at),
    INDEX idx_is_top (is_top),
    FULLTEXT INDEX ft_title_summary (title, summary) COMMENT '全文索引',
    FOREIGN KEY (category_id) REFERENCES tb_categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 评论表
CREATE TABLE IF NOT EXISTS tb_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    parent_id BIGINT COMMENT '父评论ID（楼中楼）',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱（可选）',
    website VARCHAR(200) COMMENT '个人网站（可选）',
    avatar VARCHAR(500) COMMENT '头像路径',
    content TEXT NOT NULL COMMENT '评论内容（支持Markdown）',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器UA',
    status TINYINT DEFAULT 1 COMMENT '状态: 0=已删除, 1=正常, 2=待审核',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_article (article_id),
    INDEX idx_parent (parent_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at),
    FOREIGN KEY (article_id) REFERENCES tb_articles(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES tb_comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 留言表
CREATE TABLE IF NOT EXISTS tb_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱（可选）',
    website VARCHAR(200) COMMENT '个人博客地址（可选）',
    avatar VARCHAR(500) COMMENT '头像路径',
    content TEXT NOT NULL COMMENT '留言内容',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    is_friend_link TINYINT DEFAULT 0 COMMENT '是否显示为友链: 0=否, 1=是',
    status TINYINT DEFAULT 1 COMMENT '状态: 0=已删除, 1=正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_status (status),
    INDEX idx_friend_link (is_friend_link),
    INDEX idx_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='留言表';

-- 公告表
CREATE TABLE IF NOT EXISTS tb_announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(200) NOT NULL DEFAULT '公告' COMMENT '公告标题',
    content VARCHAR(500) NOT NULL COMMENT '公告内容',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用: 0=禁用, 1=启用',
    start_time DATETIME COMMENT '开始展示时间',
    end_time DATETIME COMMENT '结束展示时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_active_time (enabled, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 更新日志表
CREATE TABLE IF NOT EXISTS tb_update_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    version VARCHAR(20) NOT NULL UNIQUE COMMENT '版本号',
    title VARCHAR(200) NOT NULL COMMENT '更新标题',
    content_path VARCHAR(500) NOT NULL COMMENT 'Markdown文件路径',
    release_date DATETIME NOT NULL COMMENT '发布日期',
    is_major TINYINT DEFAULT 0 COMMENT '是否重大更新: 0=否, 1=是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_release_date (release_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='更新日志表';

-- 网站配置表
CREATE TABLE IF NOT EXISTS tb_site_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值（JSON格式）',
    config_type VARCHAR(50) DEFAULT 'string' COMMENT '配置类型: string, json, number, boolean',
    description VARCHAR(200) COMMENT '配置说明',
    is_public TINYINT DEFAULT 1 COMMENT '是否公开: 0=仅管理, 1=公开可访问',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网站配置表';

-- 主题表
CREATE TABLE IF NOT EXISTS tb_themes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '主题名称',
    slug VARCHAR(50) NOT NULL UNIQUE COMMENT '主题标识',
    description VARCHAR(200) COMMENT '主题描述',
    author VARCHAR(50) COMMENT '作者',
    version VARCHAR(20) COMMENT '版本号',
    preview_image VARCHAR(500) COMMENT '预览图',
    theme_path VARCHAR(500) NOT NULL COMMENT '主题文件路径',
    is_active TINYINT DEFAULT 0 COMMENT '是否激活: 0=否, 1=是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_default TINYINT DEFAULT 0 COMMENT '是否为默认主题: 0=否, 1=是',
    display_order INT DEFAULT 0 COMMENT '显示顺序',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    cover_path VARCHAR(255) COMMENT '封面图片路径',
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主题表';

-- 统计表
CREATE TABLE IF NOT EXISTS tb_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    stat_date DATE NOT NULL UNIQUE COMMENT '统计日期',
    daily_visits BIGINT DEFAULT 0 COMMENT '当日访问量',
    daily_unique_visitors INT DEFAULT 0 COMMENT '当日独立访客数',
    total_visits BIGINT DEFAULT 0 COMMENT '总访问量',
    total_unique_visitors BIGINT DEFAULT 0 COMMENT '总独立访客数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_date (stat_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统计表';

-- 访问记录表
CREATE TABLE IF NOT EXISTS tb_visit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    visitor_id VARCHAR(100) NOT NULL COMMENT '访客ID（Cookie或指纹）',
    article_id BIGINT COMMENT '文章ID（如果访问的是文章）',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器UA',
    referer VARCHAR(500) COMMENT '来源页面',
    page_url VARCHAR(500) COMMENT '访问页面',
    visit_date DATE NOT NULL COMMENT '访问日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_visitor_date (visitor_id, visit_date),
    INDEX idx_article (article_id),
    INDEX idx_date (visit_date DESC),
    UNIQUE KEY uk_visitor_article_date (visitor_id, article_id, visit_date) COMMENT '同一访客同一文章同一天只计一次'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访问记录表';

-- 管理员表
CREATE TABLE IF NOT EXISTS tb_admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    last_login_at DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(45) COMMENT '最后登录IP',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS tb_operation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    operator VARCHAR(50) DEFAULT 'system' COMMENT '操作人',
    module VARCHAR(50) NOT NULL COMMENT '操作模块: article, category, comment, etc.',
    operation_type VARCHAR(20) DEFAULT 'OTHER' COMMENT '操作类型(CREATE/UPDATE/DELETE等)',
    operation_object VARCHAR(100) COMMENT '操作对象',
    operation_detail TEXT COMMENT '操作详情',
    request_method VARCHAR(10) COMMENT '请求方法: GET, POST, etc.',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数（JSON）',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器UA',
    execution_time INT COMMENT '执行时长（毫秒）',
    result TINYINT DEFAULT 1 COMMENT '操作结果: 0=失败, 1=成功',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_module_action (module),
    INDEX idx_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 错误日志表
CREATE TABLE IF NOT EXISTS tb_error_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    level VARCHAR(20) NOT NULL COMMENT '日志级别: ERROR, WARN, FATAL',
    message TEXT NOT NULL COMMENT '错误信息',
    exception_class VARCHAR(255) COMMENT '异常类名',
    stack_trace TEXT COMMENT '堆栈跟踪',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_params TEXT COMMENT '请求参数',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器UA',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_level (level),
    INDEX idx_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='错误日志表';

-- 系统通知表
CREATE TABLE IF NOT EXISTS tb_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    type VARCHAR(50) NOT NULL COMMENT '通知类型: like, comment, message, visit_milestone, etc.',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    related_type VARCHAR(50) COMMENT '关联类型: article, comment, etc.',
    related_id BIGINT COMMENT '关联ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0=未读, 1=已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_read_created (is_read, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- 小工具表
CREATE TABLE IF NOT EXISTS tb_gadgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '小工具ID',
    name VARCHAR(100) NOT NULL COMMENT '小工具名称',
    code_path VARCHAR(255) NOT NULL COMMENT '代码文件路径',
    cover_path VARCHAR(255) COMMENT '封面图片路径',
    is_applied TINYINT(1) DEFAULT 0 COMMENT '是否应用到博客',
    is_system TINYINT(1) DEFAULT 0 COMMENT '是否系统自带（不可删除）',
    display_order INT DEFAULT 0 COMMENT '显示顺序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小工具表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入默认管理员账户 (密码: admin123, 部署后请立即修改)
INSERT INTO tb_admins (username, password, nickname, email, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EXc0v0gSYpbAlqT6UDG8u2', '管理员', 'admin@example.com', 1);

-- 插入默认分类
INSERT INTO tb_categories (name, slug, description, sort_order) VALUES
('技术', 'tech', '技术相关文章', 1),
('生活', 'life', '生活随笔', 2),
('随笔', 'essay', '随笔杂谈', 3),
('未分类', 'uncategorized', '未分类文章', 999);

-- 插入网站基础配置
INSERT INTO tb_site_configs (config_key, config_value, config_type, description, is_public) VALUES
('site_title', '我的博客', 'string', '网站标题', 1),
('site_subtitle', '分享技术与生活的点滴', 'string', '网站副标题', 1),
('site_keywords', '博客,技术,分享,编程,生活', 'string', 'SEO关键词', 1),
('site_description', '个人技术博客,记录技术成长与生活感悟', 'string', 'SEO描述', 1),
('site_author', '站长', 'string', '网站作者', 1),
('site_icp', '', 'string', '网站备案号', 1),
('site_favicon', '/favicon.ico', 'string', '网站图标', 1),
('site_tab_title_active', '我的博客', 'string', '当前窗口标签页标题', 1),
('site_tab_title_inactive', '欢迎回来', 'string', '非当前窗口标签页标题', 1),
('contact_email', '', 'string', '联系邮箱', 1),
('link_github', '', 'string', 'GitHub', 1),
('contact_gongzhonghao', '', 'string', '微信公众号', 1),
('link_bilibili', '', 'string', '哔哩哔哩', 1),
('contact_qq', '', 'string', 'QQ号', 1),
('theme_default_mode', 'light', 'string', '默认主题模式: light, dark', 1),
('widgets_enabled', '["calendar", "poem"]', 'json', '启用的小工具列表', 1),
('feature_comment_enabled', 'true', 'boolean', '是否启用评论功能', 1),
('feature_message_enabled', 'true', 'boolean', '是否启用留言功能', 1),
('feature_search_enabled', 'true', 'boolean', '是否启用搜索功能', 1),
('statistics_first_article_date', '', 'string', '第一篇文章发布日期', 0),
('admin_email', '', 'string', '管理员邮箱', 0),
('admin_notification_enabled', 'true', 'boolean', '是否启用管理员通知', 0),
('site_gongan', '', 'string', '公安备案号', 1),
('ai_assistant', 'false', 'boolean', 'AI助手', 1);

-- 插入默认主题
INSERT INTO tb_themes (name, slug, description, author, version, theme_path, is_active, is_default, display_order, cover_path) VALUES
('默认主题', 'default', '默认主题', '站长', '1.0.0', '/themes/default', 0, 1, 1, 'theme_default.jpg');

-- 插入系统小工具（不可删除）
INSERT INTO tb_gadgets (name, code_path, cover_path, is_applied, is_system, display_order) VALUES
('联系我卡片', 'system/contact.html', NULL, 1, 1, 1),
('关注我卡片', 'system/follow.html', NULL, 1, 1, 2);
