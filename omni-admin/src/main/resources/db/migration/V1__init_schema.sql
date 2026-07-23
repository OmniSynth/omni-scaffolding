-- =============================================================================
-- Omni Scaffolding 初始 Schema（Flyway 为唯一真相；JPA ddl-auto=validate）
-- 数据库：MySQL 8+ / InnoDB / utf8mb4
-- 约定：业务写优先 JPA；复杂读走 MyBatis XML
-- =============================================================================

-- 系统用户（JPA 写路径主表）
CREATE TABLE sys_user (
    id              BIGINT       NOT NULL PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    nickname        VARCHAR(64)  NOT NULL,
    enabled         TINYINT(1)   NOT NULL DEFAULT 1,
    deleted         SMALLINT     NOT NULL DEFAULT 0,
    created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version         BIGINT       NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_role (
    id          BIGINT       NOT NULL PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(64)  NOT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version     BIGINT       NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_permission (
    id          BIGINT       NOT NULL PRIMARY KEY,
    code        VARCHAR(128) NOT NULL,
    name        VARCHAR(128) NOT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version     BIGINT       NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_role_permission (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 演示商品（JPA 写 / MyBatis 复杂读）
CREATE TABLE demo_product (
    id          BIGINT       NOT NULL PRIMARY KEY,
    sku         VARCHAR(64)  NOT NULL,
    name        VARCHAR(128) NOT NULL,
    category    VARCHAR(64)  NOT NULL,
    price_cents BIGINT       NOT NULL,
    stock       INT          NOT NULL DEFAULT 0,
    status      VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    version     BIGINT       NOT NULL DEFAULT 0,
    UNIQUE KEY uk_demo_product_sku (sku),
    KEY idx_demo_product_category (category),
    KEY idx_demo_product_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 种子数据：admin / admin123
INSERT INTO sys_user (id, username, password_hash, nickname, enabled, deleted, created_at, updated_at, version)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Administrator', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_role (id, code, name, deleted, created_at, updated_at, version)
VALUES (1, 'ADMIN', 'Administrator', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_permission (id, code, name, deleted, created_at, updated_at, version)
VALUES
    (1, 'system:user:read', 'Read users', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (2, 'system:user:write', 'Write users', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (3, 'demo:product:read', 'Read products', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (4, 'demo:product:write', 'Write products', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES (1, 1), (1, 2), (1, 3), (1, 4);

INSERT INTO demo_product (id, sku, name, category, price_cents, stock, status, deleted, created_at, updated_at, version)
VALUES
    (1001, 'SKU-BOOK-001', 'Java Concurrency in Practice', 'BOOK', 5999, 100, 'ACTIVE', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (1002, 'SKU-BOOK-002', 'Effective Java', 'BOOK', 4999, 80, 'ACTIVE', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (1003, 'SKU-GEAR-001', 'Mechanical Keyboard', 'GEAR', 12999, 40, 'ACTIVE', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
    (1004, 'SKU-GEAR-002', 'USB-C Hub', 'GEAR', 3999, 0, 'INACTIVE', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);
