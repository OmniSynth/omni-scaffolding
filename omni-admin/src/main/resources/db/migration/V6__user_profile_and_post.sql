-- =============================================================================
-- 岗位管理 + 用户档案字段（姓名/手机/邮箱/性别/头像）
-- =============================================================================

CREATE TABLE sys_post (
    id          BIGINT       NOT NULL PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL COMMENT '岗位编码',
    name        VARCHAR(64)  NOT NULL COMMENT '岗位名称',
    sort        INT          NOT NULL DEFAULT 0,
    status      TINYINT(1)   NOT NULL DEFAULT 1,
    deleted     INT          NOT NULL DEFAULT 0,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version     BIGINT       NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_post_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='岗位';

CREATE TABLE sys_user_post (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, post_id),
    CONSTRAINT fk_user_post_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_post_post FOREIGN KEY (post_id) REFERENCES sys_post (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE sys_user
    ADD COLUMN real_name VARCHAR(64)  NULL COMMENT '真实姓名' AFTER nickname,
    ADD COLUMN mobile    VARCHAR(32)  NULL COMMENT '手机号' AFTER real_name,
    ADD COLUMN email     VARCHAR(128) NULL COMMENT '邮箱' AFTER mobile,
    ADD COLUMN gender    VARCHAR(16)  NOT NULL DEFAULT 'UNKNOWN' COMMENT 'UNKNOWN/MALE/FEMALE' AFTER email,
    ADD COLUMN avatar    VARCHAR(512) NULL COMMENT '头像 URL 或相对路径' AFTER gender;

CREATE UNIQUE INDEX uk_sys_user_mobile ON sys_user (mobile);
CREATE UNIQUE INDEX uk_sys_user_email ON sys_user (email);

INSERT INTO sys_post (id, code, name, sort, status, deleted, created_at, updated_at, version)
VALUES
    (1, 'ENGINEER', '工程师', 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (2, 'MANAGER', '经理', 2, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (3, 'SALES', '销售专员', 3, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

UPDATE sys_user SET real_name = '系统管理员', mobile = '13800000001', email = 'admin@omni.local', gender = 'MALE' WHERE id = 1;
UPDATE sys_user SET real_name = '研发经理', mobile = '13800000002', email = 'rd_mgr@omni.local', gender = 'MALE' WHERE id = 2;
UPDATE sys_user SET real_name = '销售甲', mobile = '13800000003', email = 'sales1@omni.local', gender = 'FEMALE' WHERE id = 3;
UPDATE sys_user SET real_name = '研发开发', mobile = '13800000004', email = 'rd_dev@omni.local', gender = 'MALE' WHERE id = 4;

INSERT INTO sys_user_post (user_id, post_id) VALUES
    (1, 2),
    (2, 2),
    (3, 3),
    (4, 1);

-- 岗位管理菜单（挂在系统管理下）
INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (50, 1, 'MENU', '岗位管理', 'post', 'system/post/index', 'Postcard', 'system:post:list', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (51, 50, 'BUTTON', '岗位查询', NULL, NULL, NULL, 'system:post:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (52, 50, 'BUTTON', '岗位新增', NULL, NULL, NULL, 'system:post:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (53, 50, 'BUTTON', '岗位修改', NULL, NULL, NULL, 'system:post:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (54, 50, 'BUTTON', '岗位删除', NULL, NULL, NULL, 'system:post:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 50), (1, 51), (1, 52), (1, 53), (1, 54);
