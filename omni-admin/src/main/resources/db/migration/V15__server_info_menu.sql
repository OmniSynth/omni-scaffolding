-- =============================================================================
-- 系统详情（运行环境）菜单权限
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (140, 100, 'MENU', '系统详情', 'server', 'ops/server/index', 'Monitor', 'ops:server:list', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (141, 140, 'BUTTON', '系统详情查询', NULL, NULL, NULL, 'ops:server:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 140), (1, 141);
