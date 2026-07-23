-- =============================================================================
-- IP 白名单全局配置（供 @IpWhitelist 使用，可在系统参数中修改）
-- =============================================================================

INSERT INTO sys_config (id, config_key, config_name, config_value, remark, sort, status, builtin, deleted, created_at, updated_at, version)
VALUES
    (4, 'sys.security.ipWhitelist', 'IP白名单', '127.0.0.1,0:0:0:0:0:0:0:1',
     '供 @IpWhitelist 注解使用，逗号分隔；空白则拒绝所有标注接口', 4, 1, 1, 0,
     CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);
