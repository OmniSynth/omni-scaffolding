-- =============================================================================
-- 在线用户菜单权限
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (80, 1, 'MENU', '在线用户', 'online', 'system/online/index', 'Connection', 'system:online:list', 8, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (81, 80, 'BUTTON', '在线查询', NULL, NULL, NULL, 'system:online:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (82, 80, 'BUTTON', '强制下线', NULL, NULL, NULL, 'system:online:kick', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 80), (1, 81), (1, 82);
