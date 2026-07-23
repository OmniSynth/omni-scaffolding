-- =============================================================================
-- ${cfg.functionName} 菜单权限（请按环境调整菜单 ID，避免冲突）
-- 表：${cfg.tableName}
-- 执行后需重新登录以刷新权限缓存
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (${menuId}, ${cfg.menuParentId}, 'MENU', '${cfg.functionName}', '${cfg.businessName}', '${componentPath}', 'Document', '${perm}:list', ${cfg.menuSort}, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (${menuQueryId}, ${menuId}, 'BUTTON', '查询', NULL, NULL, NULL, '${perm}:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (${menuAddId}, ${menuId}, 'BUTTON', '新增', NULL, NULL, NULL, '${perm}:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (${menuEditId}, ${menuId}, 'BUTTON', '修改', NULL, NULL, NULL, '${perm}:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (${menuRemoveId}, ${menuId}, 'BUTTON', '删除', NULL, NULL, NULL, '${perm}:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, ${menuId}), (1, ${menuQueryId}), (1, ${menuAddId}), (1, ${menuEditId}), (1, ${menuRemoveId});
