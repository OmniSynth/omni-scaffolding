-- H2 mirror: force password change + last password change time
ALTER TABLE sys_user ADD COLUMN must_change_pwd BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE sys_user ADD COLUMN pwd_changed_at TIMESTAMP NULL;
