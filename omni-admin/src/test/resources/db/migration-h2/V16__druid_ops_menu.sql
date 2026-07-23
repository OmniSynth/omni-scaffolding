INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (150, 100, 'MENU', 'Druid监控', 'druid', 'ops/druid/index', 'DataAnalysis', 'ops:druid:list', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (151, 150, 'BUTTON', 'Druid查看', NULL, NULL, NULL, 'ops:druid:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 150), (1, 151);
