-- =============================================================================
-- IP 白名单表 + 菜单（替代 sys_config 参数维护）
-- =============================================================================

CREATE TABLE sys_ip_whitelist (
    id           BIGINT        NOT NULL PRIMARY KEY,
    ip_addr      VARCHAR(64)   NOT NULL COMMENT 'IP 地址',
    remark       VARCHAR(255)  NULL COMMENT '备注',
    status       TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    deleted      INT           NOT NULL DEFAULT 0,
    created_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version      BIGINT        NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_ip_whitelist_ip (ip_addr),
    KEY idx_sys_ip_whitelist_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='IP 白名单';

INSERT INTO sys_ip_whitelist (id, ip_addr, remark, status, deleted, created_at, updated_at, version)
VALUES
    (1, '127.0.0.1', '本机 IPv4', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (2, '0:0:0:0:0:0:0:1', '本机 IPv6', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

-- 停用旧的系统参数项（若 V20 已写入）
UPDATE sys_config
SET deleted = 1,
    status = 0,
    updated_at = CURRENT_TIMESTAMP(3)
WHERE config_key = 'sys.security.ipWhitelist'
  AND deleted = 0;

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (94, 1, 'MENU', 'IP白名单', 'ip-whitelist', 'system/ip-whitelist/index', 'Lock', 'system:ipWhitelist:list', 10, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (95, 94, 'BUTTON', '白名单查询', NULL, NULL, NULL, 'system:ipWhitelist:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (96, 94, 'BUTTON', '白名单新增', NULL, NULL, NULL, 'system:ipWhitelist:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (97, 94, 'BUTTON', '白名单修改', NULL, NULL, NULL, 'system:ipWhitelist:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (98, 94, 'BUTTON', '白名单删除', NULL, NULL, NULL, 'system:ipWhitelist:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 94), (1, 95), (1, 96), (1, 97), (1, 98);
