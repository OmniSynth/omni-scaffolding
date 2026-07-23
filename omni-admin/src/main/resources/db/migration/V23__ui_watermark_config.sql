-- 前端水印开关（系统参数）
INSERT INTO sys_config (id, config_key, config_name, config_value, remark, sort, status, builtin, deleted, created_at, updated_at, version)
VALUES (5, 'sys.ui.watermark', '前端水印', 'true', '是否在管理端展示水印；true 展示，false 关闭', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);
