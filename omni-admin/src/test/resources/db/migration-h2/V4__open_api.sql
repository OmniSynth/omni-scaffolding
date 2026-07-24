-- H2 mirror: Open API endpoint catalog, clients, IP whitelist, bindings, menus
CREATE TABLE open_api_endpoint (
    id              BIGINT       NOT NULL PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL,
    name            VARCHAR(128) NOT NULL,
    http_method     VARCHAR(16)  NOT NULL,
    path_pattern    VARCHAR(255) NOT NULL,
    remark          VARCHAR(255),
    status          BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted         INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT       NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_open_api_endpoint_code ON open_api_endpoint (code);

CREATE TABLE open_api_client (
    id              BIGINT       NOT NULL PRIMARY KEY,
    name            VARCHAR(128) NOT NULL,
    api_key_hash    VARCHAR(64)  NOT NULL,
    access_key      VARCHAR(64)  NOT NULL,
    secret_hash     VARCHAR(64),
    daily_limit     INT,
    qps_limit       INT,
    expire_at       TIMESTAMP,
    remark          VARCHAR(255),
    status          BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted         INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT       NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_open_api_client_key_hash ON open_api_client (api_key_hash);
CREATE UNIQUE INDEX uk_open_api_client_access_key ON open_api_client (access_key);

CREATE TABLE open_api_client_ip (
    client_id       BIGINT       NOT NULL,
    ip_addr         VARCHAR(64)  NOT NULL,
    PRIMARY KEY (client_id, ip_addr)
);

CREATE TABLE open_api_client_endpoint (
    client_id       BIGINT       NOT NULL,
    endpoint_id     BIGINT       NOT NULL,
    PRIMARY KEY (client_id, endpoint_id)
);

INSERT INTO open_api_endpoint (id, code, name, http_method, path_pattern, remark, status, deleted, created_at, updated_at, version)
VALUES (1, 'open.demo.ping', '开放演示 Ping', 'GET', '/api/open/demo/ping', '脚手架演示接口', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (500, 0, 'DIR', '开放管理', '/open', NULL, 'Key', NULL, 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (510, 500, 'MENU', '接口目录', 'endpoint', 'open/endpoint/index', 'Link', 'open:endpoint:list', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (511, 510, 'BUTTON', '接口查询', NULL, NULL, NULL, 'open:endpoint:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (512, 510, 'BUTTON', '接口新增', NULL, NULL, NULL, 'open:endpoint:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (513, 510, 'BUTTON', '接口修改', NULL, NULL, NULL, 'open:endpoint:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (514, 510, 'BUTTON', '接口删除', NULL, NULL, NULL, 'open:endpoint:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (520, 500, 'MENU', '客户端管理', 'client', 'open/client/index', 'Avatar', 'open:client:list', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (521, 520, 'BUTTON', '客户端查询', NULL, NULL, NULL, 'open:client:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (522, 520, 'BUTTON', '客户端新增', NULL, NULL, NULL, 'open:client:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (523, 520, 'BUTTON', '客户端修改', NULL, NULL, NULL, 'open:client:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (524, 520, 'BUTTON', '客户端删除', NULL, NULL, NULL, 'open:client:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (525, 520, 'BUTTON', '重置密钥', NULL, NULL, NULL, 'open:client:resetKey', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES
    (1, 500), (1, 510), (1, 511), (1, 512), (1, 513), (1, 514),
    (1, 520), (1, 521), (1, 522), (1, 523), (1, 524), (1, 525);
