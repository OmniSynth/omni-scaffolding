-- 对齐 JPA Integer → MySQL INT（修复 validate：found smallint, expecting integer）
-- 若本地是开发库且可重建，也可直接删库重跑 V1；本脚本用于已执行旧版 V1 的环境。

ALTER TABLE sys_user      MODIFY COLUMN deleted INT NOT NULL DEFAULT 0;
ALTER TABLE sys_role      MODIFY COLUMN deleted INT NOT NULL DEFAULT 0;
ALTER TABLE sys_permission MODIFY COLUMN deleted INT NOT NULL DEFAULT 0;
ALTER TABLE demo_product  MODIFY COLUMN deleted INT NOT NULL DEFAULT 0;
