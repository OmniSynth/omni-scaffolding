CREATE TABLE sys_user (
    id              BIGINT PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    nickname        VARCHAR(64)  NOT NULL,
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
    deleted     INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE sys_permission (
    id          BIGINT PRIMARY KEY,
    code        VARCHAR(128) NOT NULL UNIQUE,
    name        VARCHAR(128) NOT NULL,
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

CREATE TABLE sys_role_permission (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
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

INSERT INTO sys_user (id, username, password_hash, nickname, enabled, deleted, created_at, updated_at, version)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Administrator', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_role (id, code, name, deleted, created_at, updated_at, version)
VALUES (1, 'ADMIN', 'Administrator', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_permission (id, code, name, deleted, created_at, updated_at, version)
VALUES
    (1, 'system:user:read', 'Read users', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'system:user:write', 'Write users', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'demo:product:read', 'Read products', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 'demo:product:write', 'Write products', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
INSERT INTO sys_role_permission (role_id, permission_id) VALUES (1, 1), (1, 2), (1, 3), (1, 4);

INSERT INTO demo_product (id, sku, name, category, price_cents, stock, status, deleted, created_at, updated_at, version)
VALUES
    (1001, 'SKU-BOOK-001', 'Java Concurrency in Practice', 'BOOK', 5999, 100, 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (1002, 'SKU-BOOK-002', 'Effective Java', 'BOOK', 4999, 80, 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
