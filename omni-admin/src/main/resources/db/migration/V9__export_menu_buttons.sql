-- =============================================================================
-- 用户 / 角色 / 部门 / 岗位 导出按钮权限
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (16, 10, 'BUTTON', '用户导出', NULL, NULL, NULL, 'system:user:export', 6, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (25, 20, 'BUTTON', '角色导出', NULL, NULL, NULL, 'system:role:export', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (35, 30, 'BUTTON', '部门导出', NULL, NULL, NULL, 'system:dept:export', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (55, 50, 'BUTTON', '岗位导出', NULL, NULL, NULL, 'system:post:export', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 16), (1, 25), (1, 35), (1, 55);
