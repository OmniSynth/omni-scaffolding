-- =============================================================================
-- 通知公告 + 已读记录 + 菜单权限
-- =============================================================================

CREATE TABLE sys_notice (
    id            BIGINT        NOT NULL PRIMARY KEY,
    title         VARCHAR(200)  NOT NULL COMMENT '标题',
    content       VARCHAR(4000) NOT NULL COMMENT '内容',
    type          VARCHAR(16)   NOT NULL DEFAULT 'NOTICE' COMMENT 'NOTICE通知 / ANNOUNCE公告',
    status        TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    publisher_id  BIGINT        NULL COMMENT '发布人用户 ID',
    publish_time  DATETIME(3)   NULL COMMENT '首次启用发布时间',
    deleted       INT           NOT NULL DEFAULT 0,
    created_at    DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version       BIGINT        NOT NULL DEFAULT 0,
    KEY idx_sys_notice_status (status),
    KEY idx_sys_notice_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='通知公告';

CREATE TABLE sys_notice_read (
    id          BIGINT      NOT NULL PRIMARY KEY,
    notice_id   BIGINT      NOT NULL COMMENT '公告 ID',
    user_id     BIGINT      NOT NULL COMMENT '用户 ID',
    read_time   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_notice_user (notice_id, user_id),
    KEY idx_notice_read_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='公告已读记录';

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (83, 1, 'MENU', '通知公告', 'notice', 'system/notice/index', 'Bell', 'system:notice:list', 9, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (84, 83, 'BUTTON', '公告查询', NULL, NULL, NULL, 'system:notice:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (85, 83, 'BUTTON', '公告新增', NULL, NULL, NULL, 'system:notice:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (86, 83, 'BUTTON', '公告修改', NULL, NULL, NULL, 'system:notice:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (87, 83, 'BUTTON', '公告删除', NULL, NULL, NULL, 'system:notice:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 83), (1, 84), (1, 85), (1, 86), (1, 87);
