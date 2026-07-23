INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (130, 100, 'MENU', 'Redis', 'redis', 'ops/redis/index', 'Coin', 'ops:redis:list', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (131, 130, 'BUTTON', 'Redis查询', NULL, NULL, NULL, 'ops:redis:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (132, 130, 'BUTTON', 'Redis修改', NULL, NULL, NULL, 'ops:redis:edit', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (133, 130, 'BUTTON', 'Redis删除', NULL, NULL, NULL, 'ops:redis:remove', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 130), (1, 131), (1, 132), (1, 133);
