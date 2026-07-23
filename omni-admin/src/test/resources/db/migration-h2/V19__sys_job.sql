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
