CREATE TABLE sys_dict_type (
    id          BIGINT PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(64)  NOT NULL,
    remark      VARCHAR(255),
    sort        INT          NOT NULL DEFAULT 0,
    status      BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted     INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE sys_dict_data (
    id           BIGINT PRIMARY KEY,
    type_code    VARCHAR(64)  NOT NULL,
    label        VARCHAR(128) NOT NULL,
    `value`      VARCHAR(128) NOT NULL,
    sort         INT          NOT NULL DEFAULT 0,
    css_class    VARCHAR(64),
    default_flag BOOLEAN      NOT NULL DEFAULT FALSE,
    status       BOOLEAN      NOT NULL DEFAULT TRUE,
    remark       VARCHAR(255),
    deleted      INT          NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version      BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_dict_data_type ON sys_dict_data (type_code);
CREATE INDEX idx_dict_data_type_value ON sys_dict_data (type_code, `value`);

INSERT INTO sys_dict_type (id, code, name, remark, sort, status, deleted, created_at, updated_at, version)
VALUES
    (1, 'sys_gender', '用户性别', '对齐用户 gender 字段', 1, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'sys_yes_no', '是否', '通用是否', 2, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'sys_normal_disable', '系统状态', '正常/停用', 3, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_dict_data (id, type_code, label, `value`, sort, css_class, default_flag, status, remark, deleted, created_at, updated_at, version)
VALUES
    (1, 'sys_gender', '未知', 'UNKNOWN', 1, 'info', TRUE, TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'sys_gender', '男', 'MALE', 2, '', FALSE, TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'sys_gender', '女', 'FEMALE', 3, '', FALSE, TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 'sys_yes_no', '是', 'Y', 1, 'success', FALSE, TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5, 'sys_yes_no', '否', 'N', 2, 'info', TRUE, TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (6, 'sys_normal_disable', '正常', '0', 1, 'success', TRUE, TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (7, 'sys_normal_disable', '停用', '1', 2, 'danger', FALSE, TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (60, 1, 'MENU', '数据字典', 'dict', 'system/dict/index', 'Collection', 'system:dict:list', 6, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (61, 60, 'BUTTON', '字典查询', NULL, NULL, NULL, 'system:dict:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (62, 60, 'BUTTON', '字典新增', NULL, NULL, NULL, 'system:dict:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (63, 60, 'BUTTON', '字典修改', NULL, NULL, NULL, 'system:dict:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (64, 60, 'BUTTON', '字典删除', NULL, NULL, NULL, 'system:dict:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (65, 60, 'BUTTON', '字典导出', NULL, NULL, NULL, 'system:dict:export', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 60), (1, 61), (1, 62), (1, 63), (1, 64), (1, 65);
