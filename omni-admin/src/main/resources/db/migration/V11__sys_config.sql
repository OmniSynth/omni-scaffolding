-- =============================================================================
-- 系统参数 + 菜单权限
-- =============================================================================

CREATE TABLE sys_config (
    id           BIGINT        NOT NULL PRIMARY KEY,
    config_key   VARCHAR(128)  NOT NULL COMMENT '参数键，业务引用',
    config_name  VARCHAR(128)  NOT NULL COMMENT '参数名称',
    config_value VARCHAR(2000) NULL COMMENT '参数值',
    remark       VARCHAR(255)  NULL COMMENT '备注',
    sort         INT           NOT NULL DEFAULT 0,
    status       TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    builtin      TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '内置参数不可删除',
    deleted      INT           NOT NULL DEFAULT 0,
    created_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version      BIGINT        NOT NULL DEFAULT 0,
    KEY idx_sys_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='系统参数';

INSERT INTO sys_config (id, config_key, config_name, config_value, remark, sort, status, builtin, deleted, created_at, updated_at, version)
VALUES
    (1, 'sys.account.initPassword', '用户初始密码', 'Admin@123', '新建用户或重置密码时的默认明文密码提示值', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (2, 'sys.user.defaultAvatar', '默认头像', '', '用户未上传头像时的默认地址', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (3, 'sys.ui.title', '系统标题', 'Omni Admin', '浏览器标题 / 登录页展示名', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (70, 1, 'MENU', '系统参数', 'config', 'system/config/index', 'Setting', 'system:config:list', 7, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (71, 70, 'BUTTON', '参数查询', NULL, NULL, NULL, 'system:config:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (72, 70, 'BUTTON', '参数新增', NULL, NULL, NULL, 'system:config:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (73, 70, 'BUTTON', '参数修改', NULL, NULL, NULL, 'system:config:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (74, 70, 'BUTTON', '参数删除', NULL, NULL, NULL, 'system:config:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (75, 70, 'BUTTON', '参数导出', NULL, NULL, NULL, 'system:config:export', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 70), (1, 71), (1, 72), (1, 73), (1, 74), (1, 75);
