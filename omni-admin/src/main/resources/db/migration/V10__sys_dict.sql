-- =============================================================================
-- 数据字典：类型 + 数据项 + 菜单权限
-- =============================================================================

CREATE TABLE sys_dict_type (
    id          BIGINT       NOT NULL PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL COMMENT '字典类型编码，业务引用键',
    name        VARCHAR(64)  NOT NULL COMMENT '字典类型名称',
    remark      VARCHAR(255) NULL COMMENT '备注',
    sort        INT          NOT NULL DEFAULT 0,
    status      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    deleted     INT          NOT NULL DEFAULT 0,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version     BIGINT       NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_dict_type_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='字典类型';

CREATE TABLE sys_dict_data (
    id           BIGINT       NOT NULL PRIMARY KEY,
    type_code    VARCHAR(64)  NOT NULL COMMENT '所属字典类型编码',
    label        VARCHAR(128) NOT NULL COMMENT '显示标签',
    value        VARCHAR(128) NOT NULL COMMENT '存储值',
    sort         INT          NOT NULL DEFAULT 0,
    css_class    VARCHAR(64)  NULL COMMENT '前端样式类',
    default_flag TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否默认项',
    status       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    remark       VARCHAR(255) NULL COMMENT '备注',
    deleted      INT          NOT NULL DEFAULT 0,
    created_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version      BIGINT       NOT NULL DEFAULT 0,
    KEY idx_dict_data_type (type_code),
    KEY idx_dict_data_type_value (type_code, value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='字典数据';

-- 种子字典类型
INSERT INTO sys_dict_type (id, code, name, remark, sort, status, deleted, created_at, updated_at, version)
VALUES
    (1, 'sys_gender', '用户性别', '对齐用户 gender 字段', 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (2, 'sys_yes_no', '是否', '通用是否', 2, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (3, 'sys_normal_disable', '系统状态', '正常/停用', 3, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

-- 种子字典数据
INSERT INTO sys_dict_data (id, type_code, label, value, sort, css_class, default_flag, status, remark, deleted, created_at, updated_at, version)
VALUES
    (1, 'sys_gender', '未知', 'UNKNOWN', 1, 'info', 1, 1, NULL, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (2, 'sys_gender', '男', 'MALE', 2, '', 0, 1, NULL, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (3, 'sys_gender', '女', 'FEMALE', 3, '', 0, 1, NULL, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (4, 'sys_yes_no', '是', 'Y', 1, 'success', 0, 1, NULL, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (5, 'sys_yes_no', '否', 'N', 2, 'info', 1, 1, NULL, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (6, 'sys_normal_disable', '正常', '0', 1, 'success', 1, 1, NULL, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (7, 'sys_normal_disable', '停用', '1', 2, 'danger', 0, 1, NULL, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (60, 1, 'MENU', '数据字典', 'dict', 'system/dict/index', 'Collection', 'system:dict:list', 6, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (61, 60, 'BUTTON', '字典查询', NULL, NULL, NULL, 'system:dict:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (62, 60, 'BUTTON', '字典新增', NULL, NULL, NULL, 'system:dict:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (63, 60, 'BUTTON', '字典修改', NULL, NULL, NULL, 'system:dict:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (64, 60, 'BUTTON', '字典删除', NULL, NULL, NULL, 'system:dict:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (65, 60, 'BUTTON', '字典导出', NULL, NULL, NULL, 'system:dict:export', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 60), (1, 61), (1, 62), (1, 63), (1, 64), (1, 65);
