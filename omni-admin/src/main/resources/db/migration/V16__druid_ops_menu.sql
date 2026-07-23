-- =============================================================================
-- Druid 监控菜单（iframe 内嵌 /druid）
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (150, 100, 'MENU', 'Druid监控', 'druid', 'ops/druid/index', 'DataAnalysis', 'ops:druid:list', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (151, 150, 'BUTTON', 'Druid查看', NULL, NULL, NULL, 'ops:druid:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 150), (1, 151);
