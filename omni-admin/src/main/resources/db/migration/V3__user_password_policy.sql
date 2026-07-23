-- Login security: force password change + last password change time
ALTER TABLE sys_user
    ADD COLUMN must_change_pwd TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否强制改密：1是 0否' AFTER enabled,
    ADD COLUMN pwd_changed_at DATETIME(3) NULL COMMENT '最近修改密码时间' AFTER must_change_pwd;
