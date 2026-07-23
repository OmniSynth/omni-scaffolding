CREATE TABLE sys_config (
    id           BIGINT PRIMARY KEY,
    config_key   VARCHAR(128)  NOT NULL,
    config_name  VARCHAR(128)  NOT NULL,
    config_value VARCHAR(2000),
    remark       VARCHAR(255),
    sort         INT           NOT NULL DEFAULT 0,
    status       BOOLEAN       NOT NULL DEFAULT TRUE,
    builtin      BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted      INT           NOT NULL DEFAULT 0,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version      BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_sys_config_key ON sys_config (config_key);

INSERT INTO sys_config (id, config_key, config_name, config_value, remark, sort, status, builtin, deleted, created_at, updated_at, version)
VALUES
    (1, 'sys.account.initPassword', '用户初始密码', 'Admin@123', '新建用户或重置密码时的默认明文密码提示值', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'sys.user.defaultAvatar', '默认头像', '', '用户未上传头像时的默认地址', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'sys.ui.title', '系统标题', 'Omni Admin', '浏览器标题 / 登录页展示名', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (70, 1, 'MENU', '系统参数', 'config', 'system/config/index', 'Setting', 'system:config:list', 7, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (71, 70, 'BUTTON', '参数查询', NULL, NULL, NULL, 'system:config:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (72, 70, 'BUTTON', '参数新增', NULL, NULL, NULL, 'system:config:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (73, 70, 'BUTTON', '参数修改', NULL, NULL, NULL, 'system:config:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (74, 70, 'BUTTON', '参数删除', NULL, NULL, NULL, 'system:config:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (75, 70, 'BUTTON', '参数导出', NULL, NULL, NULL, 'system:config:export', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 70), (1, 71), (1, 72), (1, 73), (1, 74), (1, 75);
