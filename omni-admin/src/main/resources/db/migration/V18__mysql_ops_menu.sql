-- =============================================================================
-- MySQL 运维菜单权限
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (160, 100, 'MENU', 'MySQL', 'mysql', 'ops/mysql/index', 'Grid', 'ops:mysql:list', 6, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (161, 160, 'BUTTON', 'MySQL查询', NULL, NULL, NULL, 'ops:mysql:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (162, 160, 'BUTTON', 'MySQL修改', NULL, NULL, NULL, 'ops:mysql:edit', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (163, 160, 'BUTTON', 'MySQL删除', NULL, NULL, NULL, 'ops:mysql:remove', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 160), (1, 161), (1, 162), (1, 163);
