CREATE TABLE sys_ip_whitelist (
    id           BIGINT PRIMARY KEY,
    ip_addr      VARCHAR(64)   NOT NULL,
    remark       VARCHAR(255),
    status       BOOLEAN       NOT NULL DEFAULT TRUE,
    deleted      INT           NOT NULL DEFAULT 0,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version      BIGINT        NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_sys_ip_whitelist_ip ON sys_ip_whitelist (ip_addr);
CREATE INDEX idx_sys_ip_whitelist_status ON sys_ip_whitelist (status);

INSERT INTO sys_ip_whitelist (id, ip_addr, remark, status, deleted, created_at, updated_at, version)
VALUES
    (1, '127.0.0.1', '本机 IPv4', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, '0:0:0:0:0:0:0:1', '本机 IPv6', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

UPDATE sys_config
SET deleted = 1,
    status = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE config_key = 'sys.security.ipWhitelist'
  AND deleted = 0;

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (94, 1, 'MENU', 'IP白名单', 'ip-whitelist', 'system/ip-whitelist/index', 'Lock', 'system:ipWhitelist:list', 10, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (95, 94, 'BUTTON', '白名单查询', NULL, NULL, NULL, 'system:ipWhitelist:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (96, 94, 'BUTTON', '白名单新增', NULL, NULL, NULL, 'system:ipWhitelist:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (97, 94, 'BUTTON', '白名单修改', NULL, NULL, NULL, 'system:ipWhitelist:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (98, 94, 'BUTTON', '白名单删除', NULL, NULL, NULL, 'system:ipWhitelist:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 94), (1, 95), (1, 96), (1, 97), (1, 98);
