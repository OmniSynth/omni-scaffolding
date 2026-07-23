CREATE TABLE sys_post (
    id          BIGINT PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(64)  NOT NULL,
    sort        INT          NOT NULL DEFAULT 0,
    status      BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted     INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE sys_user_post (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, post_id)
);

ALTER TABLE sys_user ADD COLUMN real_name VARCHAR(64);
ALTER TABLE sys_user ADD COLUMN mobile VARCHAR(32);
ALTER TABLE sys_user ADD COLUMN email VARCHAR(128);
ALTER TABLE sys_user ADD COLUMN gender VARCHAR(16) DEFAULT 'UNKNOWN' NOT NULL;
ALTER TABLE sys_user ADD COLUMN avatar VARCHAR(512);

CREATE UNIQUE INDEX uk_sys_user_mobile ON sys_user (mobile);
CREATE UNIQUE INDEX uk_sys_user_email ON sys_user (email);

INSERT INTO sys_post (id, code, name, sort, status, deleted, created_at, updated_at, version)
VALUES
    (1, 'ENGINEER', '工程师', 1, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'MANAGER', '经理', 2, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'SALES', '销售专员', 3, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

UPDATE sys_user SET real_name = '系统管理员', mobile = '13800000001', email = 'admin@omni.local', gender = 'MALE' WHERE id = 1;
UPDATE sys_user SET real_name = '研发经理', mobile = '13800000002', email = 'rd_mgr@omni.local', gender = 'MALE' WHERE id = 2;
UPDATE sys_user SET real_name = '销售甲', mobile = '13800000003', email = 'sales1@omni.local', gender = 'FEMALE' WHERE id = 3;
UPDATE sys_user SET real_name = '研发开发', mobile = '13800000004', email = 'rd_dev@omni.local', gender = 'MALE' WHERE id = 4;

INSERT INTO sys_user_post (user_id, post_id) VALUES (1, 2), (2, 2), (3, 3), (4, 1);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (50, 1, 'MENU', '岗位管理', 'post', 'system/post/index', 'Postcard', 'system:post:list', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (51, 50, 'BUTTON', '岗位查询', NULL, NULL, NULL, 'system:post:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (52, 50, 'BUTTON', '岗位新增', NULL, NULL, NULL, 'system:post:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (53, 50, 'BUTTON', '岗位修改', NULL, NULL, NULL, 'system:post:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (54, 50, 'BUTTON', '岗位删除', NULL, NULL, NULL, 'system:post:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 50), (1, 51), (1, 52), (1, 53), (1, 54);
