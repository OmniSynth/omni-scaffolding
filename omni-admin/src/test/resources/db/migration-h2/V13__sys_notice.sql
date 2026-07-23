CREATE TABLE sys_notice (
    id            BIGINT PRIMARY KEY,
    title         VARCHAR(200)  NOT NULL,
    content       VARCHAR(4000) NOT NULL,
    type          VARCHAR(16)   NOT NULL DEFAULT 'NOTICE',
    status        BOOLEAN       NOT NULL DEFAULT TRUE,
    publisher_id  BIGINT,
    publish_time  TIMESTAMP,
    deleted       INT           NOT NULL DEFAULT 0,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version       BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_sys_notice_status ON sys_notice (status);
CREATE INDEX idx_sys_notice_publish_time ON sys_notice (publish_time);

CREATE TABLE sys_notice_read (
    id          BIGINT PRIMARY KEY,
    notice_id   BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    read_time   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_notice_user ON sys_notice_read (notice_id, user_id);
CREATE INDEX idx_notice_read_user ON sys_notice_read (user_id);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (83, 1, 'MENU', '通知公告', 'notice', 'system/notice/index', 'Bell', 'system:notice:list', 9, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (84, 83, 'BUTTON', '公告查询', NULL, NULL, NULL, 'system:notice:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (85, 83, 'BUTTON', '公告新增', NULL, NULL, NULL, 'system:notice:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (86, 83, 'BUTTON', '公告修改', NULL, NULL, NULL, 'system:notice:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (87, 83, 'BUTTON', '公告删除', NULL, NULL, NULL, 'system:notice:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 83), (1, 84), (1, 85), (1, 86), (1, 87);
