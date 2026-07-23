CREATE TABLE sys_user (
    id              BIGINT PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    nickname        VARCHAR(64)  NOT NULL,
    real_name       VARCHAR(64),
    mobile          VARCHAR(32),
    email           VARCHAR(128),
    gender          VARCHAR(16)  NOT NULL DEFAULT 'UNKNOWN',
    avatar          VARCHAR(512),
    dept_id         BIGINT       NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted         INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE sys_role (
    id          BIGINT PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(64)  NOT NULL,
    data_scope  VARCHAR(32)  NOT NULL DEFAULT 'SELF',
    status      BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted     INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE demo_product (
    id          BIGINT PRIMARY KEY,
    sku         VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(128) NOT NULL,
    category    VARCHAR(64)  NOT NULL,
    price_cents BIGINT       NOT NULL,
    stock       INT          NOT NULL DEFAULT 0,
    status      VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    deleted     INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT       NOT NULL DEFAULT 0
);

INSERT INTO sys_user (id, username, password_hash, nickname, real_name, mobile, email, gender, avatar, dept_id, enabled, deleted, created_at, updated_at, version)
VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'Administrator', '系统管理员', '13800000001', 'admin@omni.local', 'MALE', NULL, 1, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role (id, code, name, data_scope, status, deleted, created_at, updated_at, version)
VALUES (1, 'ADMIN', 'Administrator', 'ALL', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
INSERT INTO demo_product (id, sku, name, category, price_cents, stock, status, deleted, created_at, updated_at, version)
VALUES
    (1001, 'SKU-BOOK-001', 'Java Concurrency in Practice', 'BOOK', 5999, 100, 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (1002, 'SKU-BOOK-002', 'Effective Java', 'BOOK', 4999, 80, 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

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

INSERT INTO sys_user (id, username, password_hash, nickname, real_name, mobile, email, gender, avatar, dept_id, enabled, deleted, created_at, updated_at, version)
VALUES
    (2, 'rd_mgr', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '研发经理', '研发经理', '13800000002', 'rd_mgr@omni.local', 'MALE', NULL, 2, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'sales1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '销售甲', '销售甲', '13800000003', 'sales1@omni.local', 'FEMALE', NULL, 3, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 'rd_dev', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '研发开发', '研发开发', '13800000004', 'rd_dev@omni.local', 'MALE', NULL, 2, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 2), (3, 3), (4, 3);

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

CREATE UNIQUE INDEX uk_sys_user_mobile ON sys_user (mobile);
CREATE UNIQUE INDEX uk_sys_user_email ON sys_user (email);

INSERT INTO sys_post (id, code, name, sort, status, deleted, created_at, updated_at, version)
VALUES
    (1, 'ENGINEER', '工程师', 1, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'MANAGER', '经理', 2, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'SALES', '销售专员', 3, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

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

CREATE TABLE sys_login_log (
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT,
    username    VARCHAR(64)  NOT NULL,
    ip          VARCHAR(64),
    user_agent  VARCHAR(512),
    status      VARCHAR(16)  NOT NULL,
    message     VARCHAR(255),
    trace_id    VARCHAR(64),
    login_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_oper_log (
    id              BIGINT PRIMARY KEY,
    user_id         BIGINT,
    username        VARCHAR(64),
    module          VARCHAR(64),
    action          VARCHAR(64),
    method          VARCHAR(200),
    request_uri     VARCHAR(255),
    request_method  VARCHAR(16),
    ip              VARCHAR(64),
    status          VARCHAR(16)   NOT NULL,
    error_msg       VARCHAR(1000),
    cost_ms         INT,
    params          VARCHAR(2000),
    trace_id        VARCHAR(64),
    oper_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (300, 0, 'DIR', '日志管理', '/log', NULL, 'Document', NULL, 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (310, 300, 'MENU', '登录日志', 'login-log', 'system/login-log/index', 'Key', 'system:loginLog:list', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (311, 310, 'BUTTON', '登录日志查询', NULL, NULL, NULL, 'system:loginLog:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (312, 310, 'BUTTON', '登录日志删除', NULL, NULL, NULL, 'system:loginLog:remove', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (320, 300, 'MENU', '操作日志', 'oper-log', 'system/oper-log/index', 'Tickets', 'system:operLog:list', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (321, 320, 'BUTTON', '操作日志查询', NULL, NULL, NULL, 'system:operLog:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (322, 320, 'BUTTON', '操作日志删除', NULL, NULL, NULL, 'system:operLog:remove', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 300), (1, 310), (1, 311), (1, 312), (1, 320), (1, 321), (1, 322);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (16, 10, 'BUTTON', '用户导出', NULL, NULL, NULL, 'system:user:export', 6, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (25, 20, 'BUTTON', '角色导出', NULL, NULL, NULL, 'system:role:export', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (35, 30, 'BUTTON', '部门导出', NULL, NULL, NULL, 'system:dept:export', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (55, 50, 'BUTTON', '岗位导出', NULL, NULL, NULL, 'system:post:export', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 16), (1, 25), (1, 35), (1, 55);

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

CREATE TABLE sys_config (
    id           BIGINT PRIMARY KEY,
    config_key   VARCHAR(128)  NOT NULL,
    config_name  VARCHAR(128)  NOT NULL,
    config_value VARCHAR(2000),
    remark       VARCHAR(255),
    sort         INT           NOT NULL DEFAULT 0,
    status       BOOLEAN       NOT NULL DEFAULT TRUE,
    builtin      BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted      INT           NOT NULL DEFAULT 0,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version      BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_sys_config_key ON sys_config (config_key);

INSERT INTO sys_config (id, config_key, config_name, config_value, remark, sort, status, builtin, deleted, created_at, updated_at, version)
VALUES
    (1, 'sys.account.initPassword', '用户初始密码', 'Admin@123', '新建用户或重置密码时的默认明文密码提示值', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'sys.user.defaultAvatar', '默认头像', '', '用户未上传头像时的默认地址', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'sys.ui.title', '系统标题', 'Omni Admin', '浏览器标题 / 登录页展示名', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (70, 1, 'MENU', '系统参数', 'config', 'system/config/index', 'Setting', 'system:config:list', 7, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (71, 70, 'BUTTON', '参数查询', NULL, NULL, NULL, 'system:config:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (72, 70, 'BUTTON', '参数新增', NULL, NULL, NULL, 'system:config:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (73, 70, 'BUTTON', '参数修改', NULL, NULL, NULL, 'system:config:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (74, 70, 'BUTTON', '参数删除', NULL, NULL, NULL, 'system:config:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (75, 70, 'BUTTON', '参数导出', NULL, NULL, NULL, 'system:config:export', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 70), (1, 71), (1, 72), (1, 73), (1, 74), (1, 75);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (80, 1, 'MENU', '在线用户', 'online', 'system/online/index', 'Connection', 'system:online:list', 8, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (81, 80, 'BUTTON', '在线查询', NULL, NULL, NULL, 'system:online:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (82, 80, 'BUTTON', '强制下线', NULL, NULL, NULL, 'system:online:kick', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 80), (1, 81), (1, 82);

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

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (130, 100, 'MENU', 'Redis', 'redis', 'ops/redis/index', 'Coin', 'ops:redis:list', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (131, 130, 'BUTTON', 'Redis查询', NULL, NULL, NULL, 'ops:redis:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (132, 130, 'BUTTON', 'Redis修改', NULL, NULL, NULL, 'ops:redis:edit', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (133, 130, 'BUTTON', 'Redis删除', NULL, NULL, NULL, 'ops:redis:remove', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 130), (1, 131), (1, 132), (1, 133);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (140, 100, 'MENU', '系统详情', 'server', 'ops/server/index', 'Monitor', 'ops:server:list', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (141, 140, 'BUTTON', '系统详情查询', NULL, NULL, NULL, 'ops:server:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 140), (1, 141);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (150, 100, 'MENU', 'Druid监控', 'druid', 'ops/druid/index', 'DataAnalysis', 'ops:druid:list', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (151, 150, 'BUTTON', 'Druid查看', NULL, NULL, NULL, 'ops:druid:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 150), (1, 151);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (160, 100, 'MENU', 'MySQL', 'mysql', 'ops/mysql/index', 'Grid', 'ops:mysql:list', 6, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (161, 160, 'BUTTON', 'MySQL查询', NULL, NULL, NULL, 'ops:mysql:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (162, 160, 'BUTTON', 'MySQL修改', NULL, NULL, NULL, 'ops:mysql:edit', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (163, 160, 'BUTTON', 'MySQL删除', NULL, NULL, NULL, 'ops:mysql:remove', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 160), (1, 161), (1, 162), (1, 163);

CREATE TABLE sys_job (
    id               BIGINT PRIMARY KEY,
    job_name         VARCHAR(128)  NOT NULL,
    job_group        VARCHAR(64)   NOT NULL DEFAULT 'omni-job',
    invoke_target    VARCHAR(255)  NOT NULL,
    job_params       VARCHAR(2000),
    cron_expression  VARCHAR(64)   NOT NULL,
    misfire_policy   INT           NOT NULL DEFAULT 0,
    concurrent       BOOLEAN       NOT NULL DEFAULT FALSE,
    status           BOOLEAN       NOT NULL DEFAULT TRUE,
    remark           VARCHAR(255),
    deleted          INT           NOT NULL DEFAULT 0,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version          BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_sys_job_name ON sys_job (job_name);
CREATE INDEX idx_sys_job_status ON sys_job (status);

CREATE TABLE sys_job_log (
    id               BIGINT PRIMARY KEY,
    job_id           BIGINT        NOT NULL,
    job_name         VARCHAR(128)  NOT NULL,
    invoke_target    VARCHAR(255)  NOT NULL,
    job_params       VARCHAR(2000),
    status           BOOLEAN       NOT NULL,
    message          VARCHAR(2000),
    start_time       TIMESTAMP     NOT NULL,
    end_time         TIMESTAMP,
    cost_ms          BIGINT
);

CREATE INDEX idx_sys_job_log_job ON sys_job_log (job_id);
CREATE INDEX idx_sys_job_log_start ON sys_job_log (start_time);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (88, 1, 'MENU', '定时任务', 'job', 'system/job/index', 'Timer', 'system:job:list', 9, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (89, 88, 'BUTTON', '任务查询', NULL, NULL, NULL, 'system:job:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (90, 88, 'BUTTON', '任务新增', NULL, NULL, NULL, 'system:job:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (91, 88, 'BUTTON', '任务修改', NULL, NULL, NULL, 'system:job:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (92, 88, 'BUTTON', '任务删除', NULL, NULL, NULL, 'system:job:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (93, 88, 'BUTTON', '任务执行', NULL, NULL, NULL, 'system:job:run', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 88), (1, 89), (1, 90), (1, 91), (1, 92), (1, 93);

CREATE TABLE sys_ip_whitelist (
    id           BIGINT PRIMARY KEY,
    ip_addr      VARCHAR(64)   NOT NULL,
    remark       VARCHAR(255),
    status       BOOLEAN       NOT NULL DEFAULT TRUE,
    deleted      INT           NOT NULL DEFAULT 0,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version      BIGINT        NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_sys_ip_whitelist_ip ON sys_ip_whitelist (ip_addr);
CREATE INDEX idx_sys_ip_whitelist_status ON sys_ip_whitelist (status);

INSERT INTO sys_ip_whitelist (id, ip_addr, remark, status, deleted, created_at, updated_at, version)
VALUES
    (1, '127.0.0.1', '本机 IPv4', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, '0:0:0:0:0:0:0:1', '本机 IPv6', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (94, 1, 'MENU', 'IP白名单', 'ip-whitelist', 'system/ip-whitelist/index', 'Lock', 'system:ipWhitelist:list', 10, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (95, 94, 'BUTTON', '白名单查询', NULL, NULL, NULL, 'system:ipWhitelist:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (96, 94, 'BUTTON', '白名单新增', NULL, NULL, NULL, 'system:ipWhitelist:add', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (97, 94, 'BUTTON', '白名单修改', NULL, NULL, NULL, 'system:ipWhitelist:edit', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (98, 94, 'BUTTON', '白名单删除', NULL, NULL, NULL, 'system:ipWhitelist:remove', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 94), (1, 95), (1, 96), (1, 97), (1, 98);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (76, 70, 'BUTTON', '参数刷新缓存', NULL, NULL, NULL, 'system:config:refresh', 6, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (99, 94, 'BUTTON', '白名单刷新缓存', NULL, NULL, NULL, 'system:ipWhitelist:refresh', 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 76), (1, 99);

INSERT INTO sys_config (id, config_key, config_name, config_value, remark, sort, status, builtin, deleted, created_at, updated_at, version)
VALUES (5, 'sys.ui.watermark', '前端水印', 'true', '是否在管理端展示水印；true 展示，false 关闭', 4, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (400, 0, 'DIR', '工具', '/tool', NULL, 'Tools', NULL, 5, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (410, 400, 'MENU', '代码生成', 'gen', 'tool/gen/index', 'Document', 'tool:gen:list', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (411, 410, 'BUTTON', '生成查询', NULL, NULL, NULL, 'tool:gen:query', 1, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (412, 410, 'BUTTON', '生成预览', NULL, NULL, NULL, 'tool:gen:preview', 2, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (413, 410, 'BUTTON', '生成代码', NULL, NULL, NULL, 'tool:gen:code', 3, TRUE, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 400), (1, 410), (1, 411), (1, 412), (1, 413);
