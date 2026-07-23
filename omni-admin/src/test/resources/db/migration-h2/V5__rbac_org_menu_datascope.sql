CREATE TABLE sys_dept (
    id          BIGINT PRIMARY KEY,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    name        VARCHAR(64)  NOT NULL,
    sort        INT          NOT NULL DEFAULT 0,
    ancestors   VARCHAR(512) NOT NULL DEFAULT '0',
    status      BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted     INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE sys_menu (
    id          BIGINT PRIMARY KEY,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    type        VARCHAR(16)  NOT NULL,
    name        VARCHAR(64)  NOT NULL,
    path        VARCHAR(128) NULL,
    component   VARCHAR(128) NULL,
    icon        VARCHAR(64)  NULL,
    perms       VARCHAR(128) NULL,
    sort        INT          NOT NULL DEFAULT 0,
    visible     BOOLEAN      NOT NULL DEFAULT TRUE,
    status      BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted     INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

INSERT INTO sys_dept (id, parent_id, name, sort, ancestors, status, deleted, created_at, updated_at, version)
VALUES
    (1, 0, '总部', 0, '0', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 1, '研发部', 1, '0,1', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 1, '销售部', 2, '0,1', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

ALTER TABLE sys_role ADD COLUMN data_scope VARCHAR(32) NOT NULL DEFAULT 'SELF';
UPDATE sys_role SET data_scope = 'ALL' WHERE id = 1;

ALTER TABLE sys_user ADD COLUMN dept_id BIGINT;
UPDATE sys_user SET dept_id = 1 WHERE id = 1;
ALTER TABLE sys_user ALTER COLUMN dept_id SET NOT NULL;

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (1, 0, 'DIR', '系统管理', '/system', NULL, 'Setting', NULL, 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (10, 1, 'MENU', '用户管理', 'user', 'system/user/index', 'User', 'system:user:list', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (11, 10, 'BUTTON', '用户查询', NULL, NULL, NULL, 'system:user:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (12, 10, 'BUTTON', '用户新增', NULL, NULL, NULL, 'system:user:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (13, 10, 'BUTTON', '用户修改', NULL, NULL, NULL, 'system:user:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (14, 10, 'BUTTON', '用户删除', NULL, NULL, NULL, 'system:user:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (15, 10, 'BUTTON', '重置密码', NULL, NULL, NULL, 'system:user:resetPwd', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (20, 1, 'MENU', '角色管理', 'role', 'system/role/index', 'UserFilled', 'system:role:list', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (21, 20, 'BUTTON', '角色查询', NULL, NULL, NULL, 'system:role:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (22, 20, 'BUTTON', '角色新增', NULL, NULL, NULL, 'system:role:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (23, 20, 'BUTTON', '角色修改', NULL, NULL, NULL, 'system:role:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (24, 20, 'BUTTON', '角色删除', NULL, NULL, NULL, 'system:role:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (30, 1, 'MENU', '部门管理', 'dept', 'system/dept/index', 'OfficeBuilding', 'system:dept:list', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (31, 30, 'BUTTON', '部门查询', NULL, NULL, NULL, 'system:dept:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (32, 30, 'BUTTON', '部门新增', NULL, NULL, NULL, 'system:dept:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (33, 30, 'BUTTON', '部门修改', NULL, NULL, NULL, 'system:dept:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (34, 30, 'BUTTON', '部门删除', NULL, NULL, NULL, 'system:dept:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (40, 1, 'MENU', '菜单管理', 'menu', 'system/menu/index', 'Menu', 'system:menu:list', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (41, 40, 'BUTTON', '菜单查询', NULL, NULL, NULL, 'system:menu:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (42, 40, 'BUTTON', '菜单新增', NULL, NULL, NULL, 'system:menu:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (43, 40, 'BUTTON', '菜单修改', NULL, NULL, NULL, 'system:menu:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (44, 40, 'BUTTON', '菜单删除', NULL, NULL, NULL, 'system:menu:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (100, 0, 'DIR', '运维', '/ops', NULL, 'Monitor', NULL, 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (110, 100, 'MENU', 'Kafka', 'kafka', 'ops/kafka/index', 'Connection', 'ops:kafka:list', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (120, 100, 'MENU', 'Elasticsearch', 'elasticsearch', 'ops/elasticsearch/index', 'Search', 'ops:es:list', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (200, 0, 'DIR', '演示', '/demo', NULL, 'Goods', NULL, 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (210, 200, 'MENU', '商品演示', 'product', NULL, 'Box', 'demo:product:list', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (211, 210, 'BUTTON', '商品查询', NULL, NULL, NULL, 'demo:product:read', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (212, 210, 'BUTTON', '商品写入', NULL, NULL, NULL, 'demo:product:write', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE deleted = 0;

INSERT INTO sys_role (id, code, name, data_scope, deleted, created_at, updated_at, version)
VALUES
    (2, 'RD_MANAGER', '研发经理', 'DEPT_AND_CHILD', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'SALES', '销售员', 'SELF', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
    (2, 1), (2, 10), (2, 11), (2, 12), (2, 13),
    (3, 1), (3, 10), (3, 11);

INSERT INTO sys_user (id, username, password_hash, nickname, dept_id, enabled, deleted, created_at, updated_at, version)
VALUES
    (2, 'rd_mgr', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '研发经理', 2, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'sales1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '销售甲', 3, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 'rd_dev', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '研发开发', 2, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 2), (3, 3), (4, 3);

DROP TABLE sys_role_permission;
DROP TABLE sys_permission;
