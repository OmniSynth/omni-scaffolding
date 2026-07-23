-- H2 mirror: unified file metadata + avatar file_id + file management menus
CREATE TABLE sys_file (
    id              BIGINT       NOT NULL PRIMARY KEY,
    original_name   VARCHAR(255) NOT NULL,
    content_type    VARCHAR(128),
    size_bytes      BIGINT       NOT NULL DEFAULT 0,
    storage_type    VARCHAR(16)  NOT NULL,
    oss_provider    VARCHAR(32),
    object_key      VARCHAR(512) NOT NULL,
    biz_type        VARCHAR(64)  NOT NULL DEFAULT 'common',
    md5             VARCHAR(64),
    created_by      BIGINT,
    deleted         INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT       NOT NULL DEFAULT 0
);

ALTER TABLE sys_user DROP COLUMN avatar;
ALTER TABLE sys_user ADD COLUMN avatar_file_id BIGINT;

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (110, 1, 'MENU', '文件管理', 'file', 'system/file/index', 'FolderOpened', 'system:file:list', 11, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (111, 110, 'BUTTON', '文件查询', NULL, NULL, NULL, 'system:file:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (112, 110, 'BUTTON', '文件上传', NULL, NULL, NULL, 'system:file:upload', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (113, 110, 'BUTTON', '文件删除', NULL, NULL, NULL, 'system:file:remove', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 110), (1, 111), (1, 112), (1, 113);
