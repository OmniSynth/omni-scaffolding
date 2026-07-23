CREATE TABLE sys_login_log (
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT,
    username    VARCHAR(64)  NOT NULL,
    ip          VARCHAR(64),
    user_agent  VARCHAR(512),
    status      VARCHAR(16)  NOT NULL,
    message     VARCHAR(255),
    trace_id    VARCHAR(64),
    login_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_oper_log (
    id              BIGINT PRIMARY KEY,
    user_id         BIGINT,
    username        VARCHAR(64),
    module          VARCHAR(64),
    action          VARCHAR(64),
    method          VARCHAR(200),
    request_uri     VARCHAR(255),
    request_method  VARCHAR(16),
    ip              VARCHAR(64),
    status          VARCHAR(16)   NOT NULL,
    error_msg       VARCHAR(1000),
    cost_ms         INT,
    params          VARCHAR(2000),
    trace_id        VARCHAR(64),
    oper_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (300, 0, 'DIR', '日志管理', '/log', NULL, 'Document', NULL, 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (310, 300, 'MENU', '登录日志', 'login-log', 'system/login-log/index', 'Key', 'system:loginLog:list', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (311, 310, 'BUTTON', '登录日志查询', NULL, NULL, NULL, 'system:loginLog:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (312, 310, 'BUTTON', '登录日志删除', NULL, NULL, NULL, 'system:loginLog:remove', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (320, 300, 'MENU', '操作日志', 'oper-log', 'system/oper-log/index', 'Tickets', 'system:operLog:list', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (321, 320, 'BUTTON', '操作日志查询', NULL, NULL, NULL, 'system:operLog:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (322, 320, 'BUTTON', '操作日志删除', NULL, NULL, NULL, 'system:operLog:remove', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 300), (1, 310), (1, 311), (1, 312), (1, 320), (1, 321), (1, 322);
