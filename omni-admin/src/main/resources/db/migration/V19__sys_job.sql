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
