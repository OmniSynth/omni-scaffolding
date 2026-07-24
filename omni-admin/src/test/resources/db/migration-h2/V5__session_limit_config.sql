-- H2 mirror: concurrent session limit system configs
INSERT INTO sys_config (id, config_key, config_name, config_value, remark, sort, status, builtin, deleted, created_at, updated_at, version)
VALUES
    (6, 'sys.security.session-limit.enabled', '并发登录限制开关', 'true',
     '是否限制同账号同时在线设备数；true 开启，false 关闭。关闭或 max-devices≤0 时不限制',
     5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (7, 'sys.security.session-limit.max-devices', '最大同时在线设备数', '3',
     '每用户最大同时在线设备数；超限踢最旧会话。整数，≤0 表示不限制',
     6, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
