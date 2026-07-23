-- Unified file metadata + avatar file_id + file management menus
CREATE TABLE sys_file (
    id              BIGINT       NOT NULL PRIMARY KEY COMMENT '主键',
    original_name   VARCHAR(255) NOT NULL COMMENT '原始文件名',
    content_type    VARCHAR(128) NULL COMMENT 'MIME 类型',
    size_bytes      BIGINT       NOT NULL DEFAULT 0 COMMENT '字节大小',
    storage_type    VARCHAR(16)  NOT NULL COMMENT 'LOCAL / MINIO / OSS',
    oss_provider    VARCHAR(32)  NULL COMMENT 'OSS 插件 id，如 aliyun',
    object_key      VARCHAR(512) NOT NULL COMMENT '存储对象键/相对路径',
    biz_type        VARCHAR(64)  NOT NULL DEFAULT 'common' COMMENT '业务类型，如 avatar/common',
    md5             VARCHAR(64)  NULL COMMENT '可选 MD5',
    created_by      BIGINT       NULL COMMENT '上传人用户 ID',
    deleted         INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 1 已删',
    created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version         BIGINT       NOT NULL DEFAULT 0,
    KEY idx_sys_file_biz (biz_type),
    KEY idx_sys_file_created (created_at),
    KEY idx_sys_file_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一文件元数据';

ALTER TABLE sys_user
    DROP COLUMN avatar,
    ADD COLUMN avatar_file_id BIGINT NULL COMMENT '头像文件 ID，关联 sys_file.id' AFTER gender;

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (110, 1, 'MENU', '文件管理', 'file', 'system/file/index', 'FolderOpened', 'system:file:list', 11, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (111, 110, 'BUTTON', '文件查询', NULL, NULL, NULL, 'system:file:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (112, 110, 'BUTTON', '文件上传', NULL, NULL, NULL, 'system:file:upload', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (113, 110, 'BUTTON', '文件删除', NULL, NULL, NULL, 'system:file:remove', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 110), (1, 111), (1, 112), (1, 113);
