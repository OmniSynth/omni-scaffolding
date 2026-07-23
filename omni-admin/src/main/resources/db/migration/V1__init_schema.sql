-- =============================================================================
-- Omni Scaffolding 初始 Schema（Flyway 为唯一真相；JPA ddl-auto=validate）
-- 数据库：MySQL 8+ / InnoDB / utf8mb4
-- 约定：业务写优先 JPA；复杂读走 MyBatis XML
-- =============================================================================

-- 核心组织与 RBAC 表
CREATE TABLE sys_dept (
    id          BIGINT       NOT NULL PRIMARY KEY,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    name        VARCHAR(64)  NOT NULL,
    sort        INT          NOT NULL DEFAULT 0,
    ancestors   VARCHAR(512) NOT NULL DEFAULT '0',
    status      TINYINT(1)   NOT NULL DEFAULT 1,
    deleted     INT          NOT NULL DEFAULT 0,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version     BIGINT       NOT NULL DEFAULT 0,
    KEY idx_sys_dept_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织部门树';

CREATE TABLE sys_user (
    id              BIGINT       NOT NULL COMMENT '主键ID',
    username        VARCHAR(64)  NOT NULL COMMENT '登录用户名（唯一）',
    password_hash   VARCHAR(255) NOT NULL COMMENT '密码哈希（BCrypt）',
    nickname        VARCHAR(64)  NOT NULL COMMENT '显示昵称',
    real_name       VARCHAR(64)  NULL COMMENT '真实姓名',
    mobile          VARCHAR(32)  NULL COMMENT '手机号',
    email           VARCHAR(128) NULL COMMENT '邮箱',
    gender          VARCHAR(16)  NOT NULL DEFAULT 'UNKNOWN' COMMENT 'UNKNOWN/MALE/FEMALE',
    avatar          VARCHAR(512) NULL COMMENT '头像 URL 或相对路径',
    dept_id         BIGINT       NOT NULL COMMENT '所属部门',
    enabled         TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用：1是 0否',
    deleted         INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 非0已删',
    created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    version         BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_mobile (mobile),
    UNIQUE KEY uk_sys_user_email (email),
    CONSTRAINT fk_sys_user_dept FOREIGN KEY (dept_id) REFERENCES sys_dept (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表（JPA 写路径主表）';

CREATE TABLE sys_role (
    id          BIGINT       NOT NULL COMMENT '主键ID',
    code        VARCHAR(64)  NOT NULL COMMENT '角色编码（唯一，如 ADMIN）',
    name        VARCHAR(64)  NOT NULL COMMENT '角色名称',
    data_scope  VARCHAR(32)  NOT NULL DEFAULT 'SELF' COMMENT 'ALL/SELF/DEPT/DEPT_AND_CHILD',
    status      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0停用',
    deleted     INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 非0已删',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    version     BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

CREATE TABLE demo_product (
    id          BIGINT       NOT NULL COMMENT '主键ID',
    sku         VARCHAR(64)  NOT NULL COMMENT '商品SKU（唯一）',
    name        VARCHAR(128) NOT NULL COMMENT '商品名称',
    category    VARCHAR(64)  NOT NULL COMMENT '商品分类',
    price_cents BIGINT       NOT NULL COMMENT '价格（分）',
    stock       INT          NOT NULL DEFAULT 0 COMMENT '库存数量',
    status      VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE 等',
    deleted     INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 非0已删',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    version     BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_demo_product_sku (sku),
    KEY idx_demo_product_category (category),
    KEY idx_demo_product_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='演示商品表（JPA 写 / MyBatis 复杂读）';

-- 初始数据
INSERT INTO sys_dept (id, parent_id, name, sort, ancestors, status, deleted, created_at, updated_at, version)
VALUES
    (1, 0, '总部', 0, '0', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (2, 1, '研发部', 1, '0,1', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (3, 1, '销售部', 2, '0,1', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_user (id, username, password_hash, nickname, real_name, mobile, email, gender, avatar, dept_id, enabled, deleted, created_at, updated_at, version)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Administrator', '系统管理员', '13800000001', 'admin@omni.local', 'MALE', NULL, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role (id, code, name, data_scope, status, deleted, created_at, updated_at, version)
VALUES (1, 'ADMIN', 'Administrator', 'ALL', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

INSERT INTO demo_product (id, sku, name, category, price_cents, stock, status, deleted, created_at, updated_at, version)
VALUES
    (1001, 'SKU-BOOK-001', 'Java Concurrency in Practice', 'BOOK', 5999, 100, 'ACTIVE', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (1002, 'SKU-BOOK-002', 'Effective Java', 'BOOK', 4999, 80, 'ACTIVE', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (1003, 'SKU-GEAR-001', 'Mechanical Keyboard', 'GEAR', 12999, 40, 'ACTIVE', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (1004, 'SKU-GEAR-002', 'USB-C Hub', 'GEAR', 3999, 0, 'INACTIVE', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

-- =============================================================================
-- Quartz JDBC JobStore 表（MySQL InnoDB）
-- 来源：Quartz 官方 tables_mysql_innodb.sql（去掉 DROP，改由 Flyway 管理）
-- 集群：org.quartz.jobStore.isClustered=true + 多实例共享本库
-- =============================================================================

CREATE TABLE QRTZ_JOB_DETAILS (
    SCHED_NAME        VARCHAR(120) NOT NULL COMMENT '调度器名称',
    JOB_NAME          VARCHAR(190) NOT NULL COMMENT 'Job 名称',
    JOB_GROUP         VARCHAR(190) NOT NULL COMMENT 'Job 分组',
    DESCRIPTION       VARCHAR(250) NULL COMMENT 'Job 描述',
    JOB_CLASS_NAME    VARCHAR(250) NOT NULL COMMENT 'Job 实现类全限定名',
    IS_DURABLE        VARCHAR(1)   NOT NULL COMMENT '是否持久化',
    IS_NONCONCURRENT  VARCHAR(1)   NOT NULL COMMENT '是否禁止并发执行',
    IS_UPDATE_DATA   VARCHAR(1)   NOT NULL COMMENT '是否更新 JobData',
    REQUESTS_RECOVERY VARCHAR(1)   NOT NULL COMMENT '故障后是否请求恢复',
    JOB_DATA          BLOB         NULL COMMENT 'Job 数据（序列化）',
    PRIMARY KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quartz：Job 明细';

CREATE TABLE QRTZ_TRIGGERS (
    SCHED_NAME     VARCHAR(120) NOT NULL COMMENT '调度器名称',
    TRIGGER_NAME   VARCHAR(190) NOT NULL COMMENT 'Trigger 名称',
    TRIGGER_GROUP  VARCHAR(190) NOT NULL COMMENT 'Trigger 分组',
    JOB_NAME       VARCHAR(190) NOT NULL COMMENT '关联 Job 名称',
    JOB_GROUP      VARCHAR(190) NOT NULL COMMENT '关联 Job 分组',
    DESCRIPTION    VARCHAR(250) NULL COMMENT 'Trigger 描述',
    NEXT_FIRE_TIME BIGINT       NULL COMMENT '下次触发时间（毫秒时间戳）',
    PREV_FIRE_TIME BIGINT       NULL COMMENT '上次触发时间（毫秒时间戳）',
    PRIORITY       INTEGER      NULL COMMENT '优先级',
    TRIGGER_STATE  VARCHAR(16)  NOT NULL COMMENT '状态：WAITING/ACQUIRED/PAUSED 等',
    TRIGGER_TYPE   VARCHAR(8)   NOT NULL COMMENT '类型：CRON/SIMPLE/BLOB 等',
    START_TIME     BIGINT       NOT NULL COMMENT '开始时间（毫秒时间戳）',
    END_TIME       BIGINT       NULL COMMENT '结束时间（毫秒时间戳）',
    CALENDAR_NAME  VARCHAR(190) NULL COMMENT '关联日历名称',
    MISFIRE_INSTR  SMALLINT     NULL COMMENT '错过触发处理策略',
    JOB_DATA       BLOB         NULL COMMENT 'Trigger 附带 Job 数据',
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT FK_QRTZ_TRIGGERS_JOB
        FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
            REFERENCES QRTZ_JOB_DETAILS (SCHED_NAME, JOB_NAME, JOB_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quartz：Trigger 主表';

CREATE TABLE QRTZ_SIMPLE_TRIGGERS (
    SCHED_NAME      VARCHAR(120) NOT NULL COMMENT '调度器名称',
    TRIGGER_NAME    VARCHAR(190) NOT NULL COMMENT 'Trigger 名称',
    TRIGGER_GROUP   VARCHAR(190) NOT NULL COMMENT 'Trigger 分组',
    REPEAT_COUNT    BIGINT       NOT NULL COMMENT '重复次数（-1 表示无限）',
    REPEAT_INTERVAL BIGINT       NOT NULL COMMENT '重复间隔（毫秒）',
    TIMES_TRIGGERED BIGINT       NOT NULL COMMENT '已触发次数',
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT FK_QRTZ_SIMPLE_TRIGGERS
        FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
            REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quartz：Simple Trigger 扩展';

CREATE TABLE QRTZ_CRON_TRIGGERS (
    SCHED_NAME      VARCHAR(120) NOT NULL COMMENT '调度器名称',
    TRIGGER_NAME    VARCHAR(190) NOT NULL COMMENT 'Trigger 名称',
    TRIGGER_GROUP   VARCHAR(190) NOT NULL COMMENT 'Trigger 分组',
    CRON_EXPRESSION VARCHAR(120) NOT NULL COMMENT 'Cron 表达式',
    TIME_ZONE_ID    VARCHAR(80)  NULL COMMENT '时区 ID',
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT FK_QRTZ_CRON_TRIGGERS
        FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
            REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quartz：Cron Trigger 扩展';

CREATE TABLE QRTZ_SIMPROP_TRIGGERS (
    SCHED_NAME    VARCHAR(120)   NOT NULL COMMENT '调度器名称',
    TRIGGER_NAME  VARCHAR(190)   NOT NULL COMMENT 'Trigger 名称',
    TRIGGER_GROUP VARCHAR(190)   NOT NULL COMMENT 'Trigger 分组',
    STR_PROP_1    VARCHAR(512)   NULL COMMENT '字符串属性1',
    STR_PROP_2    VARCHAR(512)   NULL COMMENT '字符串属性2',
    STR_PROP_3    VARCHAR(512)   NULL COMMENT '字符串属性3',
    INT_PROP_1    INT            NULL COMMENT '整型属性1',
    INT_PROP_2    INT            NULL COMMENT '整型属性2',
    LONG_PROP_1   BIGINT         NULL COMMENT '长整型属性1',
    LONG_PROP_2   BIGINT         NULL COMMENT '长整型属性2',
    DEC_PROP_1    NUMERIC(13, 4) NULL COMMENT '小数属性1',
    DEC_PROP_2    NUMERIC(13, 4) NULL COMMENT '小数属性2',
    BOOL_PROP_1   VARCHAR(1)     NULL COMMENT '布尔属性1',
    BOOL_PROP_2   VARCHAR(1)     NULL COMMENT '布尔属性2',
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT FK_QRTZ_SIMPROP_TRIGGERS
        FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
            REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quartz：CalendarInterval/DailyTimeInterval 等属性 Trigger';

CREATE TABLE QRTZ_BLOB_TRIGGERS (
    SCHED_NAME    VARCHAR(120) NOT NULL COMMENT '调度器名称',
    TRIGGER_NAME  VARCHAR(190) NOT NULL COMMENT 'Trigger 名称',
    TRIGGER_GROUP VARCHAR(190) NOT NULL COMMENT 'Trigger 分组',
    BLOB_DATA     BLOB         NULL COMMENT '序列化 Trigger 数据',
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT FK_QRTZ_BLOB_TRIGGERS
        FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
            REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quartz：Blob Trigger 扩展';

CREATE TABLE QRTZ_CALENDARS (
    SCHED_NAME    VARCHAR(120) NOT NULL COMMENT '调度器名称',
    CALENDAR_NAME VARCHAR(190) NOT NULL COMMENT '日历名称',
    CALENDAR      BLOB         NOT NULL COMMENT '序列化日历对象',
    PRIMARY KEY (SCHED_NAME, CALENDAR_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quartz：日历（节假日等排除规则）';

CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS (
    SCHED_NAME    VARCHAR(120) NOT NULL COMMENT '调度器名称',
    TRIGGER_GROUP VARCHAR(190) NOT NULL COMMENT '已暂停的 Trigger 分组',
    PRIMARY KEY (SCHED_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quartz：已暂停的 Trigger 分组';

CREATE TABLE QRTZ_FIRED_TRIGGERS (
    SCHED_NAME        VARCHAR(120) NOT NULL COMMENT '调度器名称',
    ENTRY_ID          VARCHAR(95)  NOT NULL COMMENT '条目ID',
    TRIGGER_NAME      VARCHAR(190) NOT NULL COMMENT 'Trigger 名称',
    TRIGGER_GROUP     VARCHAR(190) NOT NULL COMMENT 'Trigger 分组',
    INSTANCE_NAME     VARCHAR(190) NOT NULL COMMENT '执行实例名',
    FIRED_TIME        BIGINT       NOT NULL COMMENT '实际触发时间（毫秒）',
    SCHED_TIME        BIGINT       NOT NULL COMMENT '计划触发时间（毫秒）',
    PRIORITY          INTEGER      NOT NULL COMMENT '优先级',
    STATE             VARCHAR(16)  NOT NULL COMMENT '状态',
    JOB_NAME          VARCHAR(190) NULL COMMENT 'Job 名称',
    JOB_GROUP         VARCHAR(190) NULL COMMENT 'Job 分组',
    IS_NONCONCURRENT  VARCHAR(1)   NULL COMMENT '是否禁止并发',
    REQUESTS_RECOVERY VARCHAR(1)   NULL COMMENT '是否请求恢复',
    PRIMARY KEY (SCHED_NAME, ENTRY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quartz：正在执行/已获取的 Trigger';

CREATE TABLE QRTZ_SCHEDULER_STATE (
    SCHED_NAME        VARCHAR(120) NOT NULL COMMENT '调度器名称',
    INSTANCE_NAME     VARCHAR(190) NOT NULL COMMENT '实例名称',
    LAST_CHECKIN_TIME BIGINT       NOT NULL COMMENT '最后签到时间（毫秒）',
    CHECKIN_INTERVAL  BIGINT       NOT NULL COMMENT '签到间隔（毫秒）',
    PRIMARY KEY (SCHED_NAME, INSTANCE_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quartz：集群实例心跳状态';

CREATE TABLE QRTZ_LOCKS (
    SCHED_NAME VARCHAR(120) NOT NULL COMMENT '调度器名称',
    LOCK_NAME  VARCHAR(40)  NOT NULL COMMENT '锁名称',
    PRIMARY KEY (SCHED_NAME, LOCK_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quartz：集群悲观锁';

CREATE INDEX IDX_QRTZ_J_REQ_RECOVERY ON QRTZ_JOB_DETAILS (SCHED_NAME, REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_J_GRP ON QRTZ_JOB_DETAILS (SCHED_NAME, JOB_GROUP);

CREATE INDEX IDX_QRTZ_T_J ON QRTZ_TRIGGERS (SCHED_NAME, JOB_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_JG ON QRTZ_TRIGGERS (SCHED_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_C ON QRTZ_TRIGGERS (SCHED_NAME, CALENDAR_NAME);
CREATE INDEX IDX_QRTZ_T_G ON QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_T_STATE ON QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_STATE ON QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_G_STATE ON QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_GROUP, TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON QRTZ_TRIGGERS (SCHED_NAME, NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST ON QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_STATE, NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_MISFIRE ON QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE ON QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE_GRP ON QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_GROUP, TRIGGER_STATE);

CREATE INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON QRTZ_FIRED_TRIGGERS (SCHED_NAME, INSTANCE_NAME);
CREATE INDEX IDX_QRTZ_FT_INST_JOB_REQ_RCVRY ON QRTZ_FIRED_TRIGGERS (SCHED_NAME, INSTANCE_NAME, REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_FT_J_G ON QRTZ_FIRED_TRIGGERS (SCHED_NAME, JOB_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_JG ON QRTZ_FIRED_TRIGGERS (SCHED_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_T_G ON QRTZ_FIRED_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_FT_TG ON QRTZ_FIRED_TRIGGERS (SCHED_NAME, TRIGGER_GROUP);

-- =============================================================================
-- 为业务表与 Quartz 表补充 MySQL 表/字段注释（不改动已执行的 V1/V3，避免 checksum 冲突）
-- 查看：SHOW FULL COLUMNS FROM sys_user; 或 information_schema.COLUMNS.COLUMN_COMMENT
CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL PRIMARY KEY,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    type        VARCHAR(16)  NOT NULL COMMENT 'DIR / MENU / BUTTON',
    name        VARCHAR(64)  NOT NULL,
    path        VARCHAR(128) NULL,
    component   VARCHAR(128) NULL,
    icon        VARCHAR(64)  NULL,
    perms       VARCHAR(128) NULL COMMENT '权限码，按钮/菜单可见性',
    sort        INT          NOT NULL DEFAULT 0,
    visible     TINYINT(1)   NOT NULL DEFAULT 1,
    status      TINYINT(1)   NOT NULL DEFAULT 1,
    deleted     INT          NOT NULL DEFAULT 0,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version     BIGINT       NOT NULL DEFAULT 0,
    KEY idx_sys_menu_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='菜单与按钮权限';

CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 菜单种子：系统管理
INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (1, 0, 'DIR', '系统管理', '/system', NULL, 'Setting', NULL, 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (10, 1, 'MENU', '用户管理', 'user', 'system/user/index', 'User', 'system:user:list', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (11, 10, 'BUTTON', '用户查询', NULL, NULL, NULL, 'system:user:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (12, 10, 'BUTTON', '用户新增', NULL, NULL, NULL, 'system:user:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (13, 10, 'BUTTON', '用户修改', NULL, NULL, NULL, 'system:user:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (14, 10, 'BUTTON', '用户删除', NULL, NULL, NULL, 'system:user:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (15, 10, 'BUTTON', '重置密码', NULL, NULL, NULL, 'system:user:resetPwd', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (20, 1, 'MENU', '角色管理', 'role', 'system/role/index', 'UserFilled', 'system:role:list', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (21, 20, 'BUTTON', '角色查询', NULL, NULL, NULL, 'system:role:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (22, 20, 'BUTTON', '角色新增', NULL, NULL, NULL, 'system:role:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (23, 20, 'BUTTON', '角色修改', NULL, NULL, NULL, 'system:role:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (24, 20, 'BUTTON', '角色删除', NULL, NULL, NULL, 'system:role:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (30, 1, 'MENU', '部门管理', 'dept', 'system/dept/index', 'OfficeBuilding', 'system:dept:list', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (31, 30, 'BUTTON', '部门查询', NULL, NULL, NULL, 'system:dept:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (32, 30, 'BUTTON', '部门新增', NULL, NULL, NULL, 'system:dept:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (33, 30, 'BUTTON', '部门修改', NULL, NULL, NULL, 'system:dept:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (34, 30, 'BUTTON', '部门删除', NULL, NULL, NULL, 'system:dept:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (40, 1, 'MENU', '菜单管理', 'menu', 'system/menu/index', 'Menu', 'system:menu:list', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (41, 40, 'BUTTON', '菜单查询', NULL, NULL, NULL, 'system:menu:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (42, 40, 'BUTTON', '菜单新增', NULL, NULL, NULL, 'system:menu:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (43, 40, 'BUTTON', '菜单修改', NULL, NULL, NULL, 'system:menu:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (44, 40, 'BUTTON', '菜单删除', NULL, NULL, NULL, 'system:menu:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (100, 0, 'DIR', '运维', '/ops', NULL, 'Monitor', NULL, 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (200, 0, 'DIR', '演示', '/demo', NULL, 'Goods', NULL, 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (210, 200, 'MENU', '商品演示', 'product', NULL, 'Box', 'demo:product:list', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (211, 210, 'BUTTON', '商品查询', NULL, NULL, NULL, 'demo:product:read', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (212, 210, 'BUTTON', '商品写入', NULL, NULL, NULL, 'demo:product:write', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE deleted = 0;

-- 演示数据范围：研发经理（本部门及以下）、销售员（仅本人）
INSERT INTO sys_role (id, code, name, data_scope, deleted, created_at, updated_at, version)
VALUES
    (2, 'RD_MANAGER', '研发经理', 'DEPT_AND_CHILD', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (3, 'SALES', '销售员', 'SELF', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

-- 研发经理：系统管理目录 + 用户管理（查/增/改）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
    (2, 1), (2, 10), (2, 11), (2, 12), (2, 13);

-- 销售员：仅用户查询
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
    (3, 1), (3, 10), (3, 11);

-- 演示用户密码同 admin123
INSERT INTO sys_user (id, username, password_hash, nickname, real_name, mobile, email, gender, avatar, dept_id, enabled, deleted, created_at, updated_at, version)
VALUES
    (2, 'rd_mgr', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '????', '????', '13800000002', 'rd_mgr@omni.local', 'MALE', NULL, 2, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (3, 'sales1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '???', '???', '13800000003', 'sales1@omni.local', 'FEMALE', NULL, 3, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (4, 'rd_dev', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '????', '????', '13800000004', 'rd_dev@omni.local', 'MALE', NULL, 2, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 2), (3, 3), (4, 3);

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

INSERT INTO sys_post (id, code, name, sort, status, deleted, created_at, updated_at, version)
VALUES
    (1, 'ENGINEER', '工程师', 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (2, 'MANAGER', '经理', 2, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (3, 'SALES', '销售专员', 3, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

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

-- =============================================================================
-- 角色启用/停用
-- =============================================================================

-- =============================================================================
-- 登录日志 / 操作日志 + 日志管理菜单
-- =============================================================================

CREATE TABLE sys_login_log (
    id          BIGINT       NOT NULL PRIMARY KEY,
    user_id     BIGINT       NULL COMMENT '用户 ID，未知账号可为空',
    username    VARCHAR(64)  NOT NULL COMMENT '登录账号',
    ip          VARCHAR(64)  NULL COMMENT '客户端 IP',
    user_agent  VARCHAR(512) NULL COMMENT 'UA',
    status      VARCHAR(16)  NOT NULL COMMENT 'SUCCESS / FAIL',
    message     VARCHAR(255) NULL COMMENT '结果说明',
    trace_id    VARCHAR(64)  NULL COMMENT '链路 ID',
    login_time  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_login_log_user (user_id),
    KEY idx_login_log_username (username),
    KEY idx_login_log_status (status),
    KEY idx_login_log_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='登录日志';

CREATE TABLE sys_oper_log (
    id              BIGINT        NOT NULL PRIMARY KEY,
    user_id         BIGINT        NULL COMMENT '操作人 ID',
    username        VARCHAR(64)   NULL COMMENT '操作人账号',
    module          VARCHAR(64)   NULL COMMENT '业务模块',
    action          VARCHAR(64)   NULL COMMENT '操作动作',
    method          VARCHAR(200)  NULL COMMENT 'Java 方法',
    request_uri     VARCHAR(255)  NULL COMMENT '请求 URI',
    request_method  VARCHAR(16)   NULL COMMENT 'HTTP 方法',
    ip              VARCHAR(64)   NULL COMMENT '客户端 IP',
    status          VARCHAR(16)   NOT NULL COMMENT 'SUCCESS / FAIL',
    error_msg       VARCHAR(1000) NULL COMMENT '失败信息',
    cost_ms         INT           NULL COMMENT '耗时毫秒',
    params          VARCHAR(2000) NULL COMMENT '请求参数摘要',
    trace_id        VARCHAR(64)   NULL COMMENT '链路 ID',
    oper_time       DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_oper_log_user (user_id),
    KEY idx_oper_log_module (module),
    KEY idx_oper_log_status (status),
    KEY idx_oper_log_time (oper_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='操作日志';

-- 日志管理目录（与系统管理、运维并列）
INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (300, 0, 'DIR', '日志管理', '/log', NULL, 'Document', NULL, 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (310, 300, 'MENU', '登录日志', 'login-log', 'system/login-log/index', 'Key', 'system:loginLog:list', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (311, 310, 'BUTTON', '登录日志查询', NULL, NULL, NULL, 'system:loginLog:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (312, 310, 'BUTTON', '登录日志删除', NULL, NULL, NULL, 'system:loginLog:remove', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (320, 300, 'MENU', '操作日志', 'oper-log', 'system/oper-log/index', 'Tickets', 'system:operLog:list', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (321, 320, 'BUTTON', '操作日志查询', NULL, NULL, NULL, 'system:operLog:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (322, 320, 'BUTTON', '操作日志删除', NULL, NULL, NULL, 'system:operLog:remove', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 300), (1, 310), (1, 311), (1, 312), (1, 320), (1, 321), (1, 322);

-- =============================================================================
-- 用户 / 角色 / 部门 / 岗位 导出按钮权限
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (16, 10, 'BUTTON', '用户导出', NULL, NULL, NULL, 'system:user:export', 6, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (25, 20, 'BUTTON', '角色导出', NULL, NULL, NULL, 'system:role:export', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (35, 30, 'BUTTON', '部门导出', NULL, NULL, NULL, 'system:dept:export', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (55, 50, 'BUTTON', '岗位导出', NULL, NULL, NULL, 'system:post:export', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 16), (1, 25), (1, 35), (1, 55);

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

-- =============================================================================
-- 系统参数 + 菜单权限
-- =============================================================================

CREATE TABLE sys_config (
    id           BIGINT        NOT NULL PRIMARY KEY,
    config_key   VARCHAR(128)  NOT NULL COMMENT '参数键，业务引用',
    config_name  VARCHAR(128)  NOT NULL COMMENT '参数名称',
    config_value VARCHAR(2000) NULL COMMENT '参数值',
    remark       VARCHAR(255)  NULL COMMENT '备注',
    sort         INT           NOT NULL DEFAULT 0,
    status       TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    builtin      TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '内置参数不可删除',
    deleted      INT           NOT NULL DEFAULT 0,
    created_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version      BIGINT        NOT NULL DEFAULT 0,
    KEY idx_sys_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='系统参数';

INSERT INTO sys_config (id, config_key, config_name, config_value, remark, sort, status, builtin, deleted, created_at, updated_at, version)
VALUES
    (1, 'sys.account.initPassword', '用户初始密码', 'Admin@123', '新建用户或重置密码时的默认明文密码提示值', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (2, 'sys.user.defaultAvatar', '默认头像', '', '用户未上传头像时的默认地址', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (3, 'sys.ui.title', '系统标题', 'Omni Admin', '浏览器标题 / 登录页展示名', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (70, 1, 'MENU', '系统参数', 'config', 'system/config/index', 'Setting', 'system:config:list', 7, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (71, 70, 'BUTTON', '参数查询', NULL, NULL, NULL, 'system:config:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (72, 70, 'BUTTON', '参数新增', NULL, NULL, NULL, 'system:config:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (73, 70, 'BUTTON', '参数修改', NULL, NULL, NULL, 'system:config:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (74, 70, 'BUTTON', '参数删除', NULL, NULL, NULL, 'system:config:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (75, 70, 'BUTTON', '参数导出', NULL, NULL, NULL, 'system:config:export', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 70), (1, 71), (1, 72), (1, 73), (1, 74), (1, 75);

-- =============================================================================
-- 在线用户菜单权限
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (80, 1, 'MENU', '在线用户', 'online', 'system/online/index', 'Connection', 'system:online:list', 8, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (81, 80, 'BUTTON', '在线查询', NULL, NULL, NULL, 'system:online:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (82, 80, 'BUTTON', '强制下线', NULL, NULL, NULL, 'system:online:kick', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 80), (1, 81), (1, 82);

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

-- =============================================================================
-- Redis 运维菜单权限
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (130, 100, 'MENU', 'Redis', 'redis', 'ops/redis/index', 'Coin', 'ops:redis:list', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (131, 130, 'BUTTON', 'Redis查询', NULL, NULL, NULL, 'ops:redis:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (132, 130, 'BUTTON', 'Redis修改', NULL, NULL, NULL, 'ops:redis:edit', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (133, 130, 'BUTTON', 'Redis删除', NULL, NULL, NULL, 'ops:redis:remove', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 130), (1, 131), (1, 132), (1, 133);

-- =============================================================================
-- 系统详情（运行环境）菜单权限
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (140, 100, 'MENU', '系统详情', 'server', 'ops/server/index', 'Monitor', 'ops:server:list', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (141, 140, 'BUTTON', '系统详情查询', NULL, NULL, NULL, 'ops:server:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 140), (1, 141);

-- =============================================================================
-- Druid 监控菜单（iframe 内嵌 /druid）
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (150, 100, 'MENU', 'Druid监控', 'druid', 'ops/druid/index', 'DataAnalysis', 'ops:druid:list', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (151, 150, 'BUTTON', 'Druid查看', NULL, NULL, NULL, 'ops:druid:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 150), (1, 151);

-- =============================================================================
-- MySQL 运维菜单权限
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (160, 100, 'MENU', 'MySQL', 'mysql', 'ops/mysql/index', 'Grid', 'ops:mysql:list', 6, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (161, 160, 'BUTTON', 'MySQL查询', NULL, NULL, NULL, 'ops:mysql:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (162, 160, 'BUTTON', 'MySQL修改', NULL, NULL, NULL, 'ops:mysql:edit', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (163, 160, 'BUTTON', 'MySQL删除', NULL, NULL, NULL, 'ops:mysql:remove', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 160), (1, 161), (1, 162), (1, 163);

-- =============================================================================
-- 定时任务 + 执行日志 + 菜单权限
-- =============================================================================

CREATE TABLE sys_job (
    id               BIGINT        NOT NULL PRIMARY KEY,
    job_name         VARCHAR(128)  NOT NULL COMMENT '任务名称',
    job_group        VARCHAR(64)   NOT NULL DEFAULT 'omni-job' COMMENT '任务组',
    invoke_target    VARCHAR(255)  NOT NULL COMMENT '调用目标 beanName.methodName',
    job_params       VARCHAR(2000) NULL COMMENT '任务参数（可空=无参）',
    cron_expression  VARCHAR(64)   NOT NULL COMMENT 'Cron 表达式',
    misfire_policy   TINYINT       NOT NULL DEFAULT 0 COMMENT '0忽略 1立即触发一次 2触发所有错过',
    concurrent       TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '1允许并发 0禁止',
    status           TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    remark           VARCHAR(255)  NULL COMMENT '备注',
    deleted          INT           NOT NULL DEFAULT 0,
    created_at       DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at       DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version          BIGINT        NOT NULL DEFAULT 0,
    KEY idx_sys_job_name (job_name),
    KEY idx_sys_job_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='定时任务';

CREATE TABLE sys_job_log (
    id               BIGINT        NOT NULL PRIMARY KEY,
    job_id           BIGINT        NOT NULL COMMENT '任务 ID',
    job_name         VARCHAR(128)  NOT NULL COMMENT '任务名称',
    invoke_target    VARCHAR(255)  NOT NULL COMMENT '调用目标',
    job_params       VARCHAR(2000) NULL COMMENT '任务参数',
    status           TINYINT(1)    NOT NULL COMMENT '1成功 0失败',
    message          VARCHAR(2000) NULL COMMENT '结果/异常摘要',
    start_time       DATETIME(3)   NOT NULL COMMENT '开始时间',
    end_time         DATETIME(3)   NULL COMMENT '结束时间',
    cost_ms          BIGINT        NULL COMMENT '耗时毫秒',
    KEY idx_sys_job_log_job (job_id),
    KEY idx_sys_job_log_start (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='定时任务执行日志';

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (88, 1, 'MENU', '定时任务', 'job', 'system/job/index', 'Timer', 'system:job:list', 9, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (89, 88, 'BUTTON', '任务查询', NULL, NULL, NULL, 'system:job:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (90, 88, 'BUTTON', '任务新增', NULL, NULL, NULL, 'system:job:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (91, 88, 'BUTTON', '任务修改', NULL, NULL, NULL, 'system:job:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (92, 88, 'BUTTON', '任务删除', NULL, NULL, NULL, 'system:job:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (93, 88, 'BUTTON', '任务执行', NULL, NULL, NULL, 'system:job:run', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 88), (1, 89), (1, 90), (1, 91), (1, 92), (1, 93);

-- =============================================================================
-- IP 白名单表 + 菜单（替代 sys_config 参数维护）
-- =============================================================================

CREATE TABLE sys_ip_whitelist (
    id           BIGINT        NOT NULL PRIMARY KEY,
    ip_addr      VARCHAR(64)   NOT NULL COMMENT 'IP 地址',
    remark       VARCHAR(255)  NULL COMMENT '备注',
    status       TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    deleted      INT           NOT NULL DEFAULT 0,
    created_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version      BIGINT        NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_ip_whitelist_ip (ip_addr),
    KEY idx_sys_ip_whitelist_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='IP 白名单';

INSERT INTO sys_ip_whitelist (id, ip_addr, remark, status, deleted, created_at, updated_at, version)
VALUES
    (1, '127.0.0.1', '本机 IPv4', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (2, '0:0:0:0:0:0:0:1', '本机 IPv6', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (94, 1, 'MENU', 'IP白名单', 'ip-whitelist', 'system/ip-whitelist/index', 'Lock', 'system:ipWhitelist:list', 10, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (95, 94, 'BUTTON', '白名单查询', NULL, NULL, NULL, 'system:ipWhitelist:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (96, 94, 'BUTTON', '白名单新增', NULL, NULL, NULL, 'system:ipWhitelist:add', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (97, 94, 'BUTTON', '白名单修改', NULL, NULL, NULL, 'system:ipWhitelist:edit', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (98, 94, 'BUTTON', '白名单删除', NULL, NULL, NULL, 'system:ipWhitelist:remove', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 94), (1, 95), (1, 96), (1, 97), (1, 98);

-- 系统参数 / IP 白名单：刷新缓存按钮权限
INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (76, 70, 'BUTTON', '参数刷新缓存', NULL, NULL, NULL, 'system:config:refresh', 6, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (99, 94, 'BUTTON', '白名单刷新缓存', NULL, NULL, NULL, 'system:ipWhitelist:refresh', 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 76), (1, 99);

-- 前端水印开关（系统参数）
INSERT INTO sys_config (id, config_key, config_name, config_value, remark, sort, status, builtin, deleted, created_at, updated_at, version)
VALUES (5, 'sys.ui.watermark', '前端水印', 'true', '是否在管理端展示水印；true 展示，false 关闭', 4, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

-- 工具目录 + 代码生成菜单
INSERT INTO sys_menu (id, parent_id, type, name, path, component, icon, perms, sort, visible, status, deleted, created_at, updated_at, version)
VALUES
    (400, 0, 'DIR', '工具', '/tool', NULL, 'Tools', NULL, 5, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (410, 400, 'MENU', '代码生成', 'gen', 'tool/gen/index', 'Document', 'tool:gen:list', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (411, 410, 'BUTTON', '生成查询', NULL, NULL, NULL, 'tool:gen:query', 1, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (412, 410, 'BUTTON', '生成预览', NULL, NULL, NULL, 'tool:gen:preview', 2, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (413, 410, 'BUTTON', '生成代码', NULL, NULL, NULL, 'tool:gen:code', 3, 1, 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 400), (1, 410), (1, 411), (1, 412), (1, 413);
