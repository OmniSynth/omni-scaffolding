-- 系统参数 / IP 白名单：刷新缓存按钮权限
INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (76, 70, 'BUTTON', '参数刷新缓存', NULL, NULL, NULL, 'system:config:refresh', 6, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (99, 94, 'BUTTON', '白名单刷新缓存', NULL, NULL, NULL, 'system:ipWhitelist:refresh', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 76), (1, 99);
