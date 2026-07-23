-- =============================================================================
-- 登录日志 / 操作日志 + 日志管理菜单
-- =============================================================================

CREATE TABLE sys_login_log (
    id          BIGINT       NOT NULL PRIMARY KEY,
    user_id     BIGINT       NULL COMMENT '用户 ID，未知账号可为空',
    username    VARCHAR(64)  NOT NULL COMMENT '登录账号',
    ip          VARCHAR(64)  NULL COMMENT '客户端 IP',
    user_agent  VARCHAR(512) NULL COMMENT 'UA',
    status      VARCHAR(16)  NOT NULL COMMENT 'SUCCESS / FAIL',
    message     VARCHAR(255) NULL COMMENT '结果说明',
    trace_id    VARCHAR(64)  NULL COMMENT '链路 ID',
    login_time  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_login_log_user (user_id),
    KEY idx_login_log_username (username),
    KEY idx_login_log_status (status),
    KEY idx_login_log_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='登录日志';

CREATE TABLE sys_oper_log (
    id              BIGINT        NOT NULL PRIMARY KEY,
    user_id         BIGINT        NULL COMMENT '操作人 ID',
    username        VARCHAR(64)   NULL COMMENT '操作人账号',
    module          VARCHAR(64)   NULL COMMENT '业务模块',
    action          VARCHAR(64)   NULL COMMENT '操作动作',
    method          VARCHAR(200)  NULL COMMENT 'Java 方法',
    request_uri     VARCHAR(255)  NULL COMMENT '请求 URI',
    request_method  VARCHAR(16)   NULL COMMENT 'HTTP 方法',
    ip              VARCHAR(64)   NULL COMMENT '客户端 IP',
    status          VARCHAR(16)   NOT NULL COMMENT 'SUCCESS / FAIL',
    error_msg       VARCHAR(1000) NULL COMMENT '失败信息',
    cost_ms         INT           NULL COMMENT '耗时毫秒',
    params          VARCHAR(2000) NULL COMMENT '请求参数摘要',
    trace_id        VARCHAR(64)   NULL COMMENT '链路 ID',
    oper_time       DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_oper_log_user (user_id),
    KEY idx_oper_log_module (module),
    KEY idx_oper_log_status (status),
    KEY idx_oper_log_time (oper_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='操作日志';

-- 日志管理目录（与系统管理、运维并列）
INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (300, 0, 'DIR', '日志管理', '/log', NULL, 'Document', NULL, 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (310, 300, 'MENU', '登录日志', 'login-log', 'system/login-log/index', 'Key', 'system:loginLog:list', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (311, 310, 'BUTTON', '登录日志查询', NULL, NULL, NULL, 'system:loginLog:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (312, 310, 'BUTTON', '登录日志删除', NULL, NULL, NULL, 'system:loginLog:remove', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (320, 300, 'MENU', '操作日志', 'oper-log', 'system/oper-log/index', 'Tickets', 'system:operLog:list', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (321, 320, 'BUTTON', '操作日志查询', NULL, NULL, NULL, 'system:operLog:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (322, 320, 'BUTTON', '操作日志删除', NULL, NULL, NULL, 'system:operLog:remove', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 300), (1, 310), (1, 311), (1, 312), (1, 320), (1, 321), (1, 322);
