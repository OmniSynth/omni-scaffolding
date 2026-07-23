-- =============================================================================
-- 角色启用/停用
-- =============================================================================

ALTER TABLE sys_role
    ADD COLUMN status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0停用' AFTER data_scope;

UPDATE sys_role SET status = 1 WHERE deleted = 0;
