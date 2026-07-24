/*
 Navicat Premium Data Transfer

 Source Server         : 大数据平台
 Source Server Type    : MySQL
 Source Server Version : 80027 (8.0.27)
 Source Host           : 192.168.3.10:40785
 Source Schema         : omni-scaffolding

 Target Server Type    : MySQL
 Target Server Version : 80027 (8.0.27)
 File Encoding         : 65001

 Date: 24/07/2026 14:20:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for demo_product
-- ----------------------------
DROP TABLE IF EXISTS `demo_product`;
CREATE TABLE `demo_product`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `sku` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品SKU（唯一）',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品分类',
  `price_cents` bigint NOT NULL COMMENT '价格（分）',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE 等',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 非0已删',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_demo_product_sku`(`sku` ASC) USING BTREE,
  INDEX `idx_demo_product_category`(`category` ASC) USING BTREE,
  INDEX `idx_demo_product_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '演示商品表（JPA 写 / MyBatis 复杂读）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of demo_product
-- ----------------------------
INSERT INTO `demo_product` VALUES (1001, 'SKU-BOOK-001', 'Java Concurrency in Practice', 'BOOK', 5999, 100, 'ACTIVE', 0, '2026-07-23 09:05:08.009', '2026-07-23 09:05:08.009', 0);
INSERT INTO `demo_product` VALUES (1002, 'SKU-BOOK-002', 'Effective Java', 'BOOK', 4999, 80, 'ACTIVE', 0, '2026-07-23 09:05:08.009', '2026-07-23 09:05:08.009', 0);
INSERT INTO `demo_product` VALUES (1003, 'SKU-GEAR-001', 'Mechanical Keyboard', 'GEAR', 12999, 40, 'ACTIVE', 0, '2026-07-23 09:05:08.009', '2026-07-23 09:05:08.009', 0);
INSERT INTO `demo_product` VALUES (1004, 'SKU-GEAR-002', 'USB-C Hub', 'GEAR', 3999, 0, 'INACTIVE', 0, '2026-07-23 09:05:08.009', '2026-07-23 09:05:08.009', 0);

-- ----------------------------
-- Table structure for flyway_schema_history
-- ----------------------------
DROP TABLE IF EXISTS `flyway_schema_history`;
CREATE TABLE `flyway_schema_history`  (
  `installed_rank` int NOT NULL,
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int NULL DEFAULT NULL,
  `installed_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`) USING BTREE,
  INDEX `flyway_schema_history_s_idx`(`success` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flyway_schema_history
-- ----------------------------
INSERT INTO `flyway_schema_history` VALUES (1, '1', 'init schema', 'SQL', 'V1__init_schema.sql', 1656416959, 'root', '2026-07-23 09:05:09', 2002, 1);
INSERT INTO `flyway_schema_history` VALUES (2, '2', 'sys file', 'SQL', 'V2__sys_file.sql', 313294148, 'root', '2026-07-23 11:41:16', 154, 1);
INSERT INTO `flyway_schema_history` VALUES (3, '3', 'user password policy', 'SQL', 'V3__user_password_policy.sql', -484160314, 'root', '2026-07-23 15:29:26', 453, 1);

-- ----------------------------
-- Table structure for open_api_client
-- ----------------------------
DROP TABLE IF EXISTS `open_api_client`;
CREATE TABLE `open_api_client`  (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户端名称',
  `api_key_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API Key SHA-256',
  `access_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公开 AccessKey（二期签名用）',
  `secret_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Secret SHA-256（二期签名用）',
  `daily_limit` int NULL DEFAULT NULL COMMENT '日调用上限，NULL/0 不限',
  `qps_limit` int NULL DEFAULT NULL COMMENT '每秒上限，NULL/0 不限',
  `expire_at` datetime(3) NULL DEFAULT NULL COMMENT '过期时间，空表示不过期',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_open_api_client_key_hash`(`api_key_hash` ASC) USING BTREE,
  UNIQUE INDEX `uk_open_api_client_access_key`(`access_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '开放 API 客户端' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of open_api_client
-- ----------------------------
INSERT INTO `open_api_client` VALUES (595458373448572, '测试', 'eaf9c03539e2cf249c7d6f283d6f12ccf3026239f59bd90afa946da9930bc4e3', 'ak_ef5afaf843ca4469', 'c6e945be7451050ec2a2876e7982f3ab5230a3ac7286cdf45bbfb019cd094161', 10, 5, NULL, NULL, 1, 0, '2026-07-24 05:37:41.725', '2026-07-24 06:04:54.917', 2);

-- ----------------------------
-- Table structure for open_api_client_endpoint
-- ----------------------------
DROP TABLE IF EXISTS `open_api_client_endpoint`;
CREATE TABLE `open_api_client_endpoint`  (
  `client_id` bigint NOT NULL COMMENT '客户端 ID',
  `endpoint_id` bigint NOT NULL COMMENT '接口目录 ID',
  PRIMARY KEY (`client_id`, `endpoint_id`) USING BTREE,
  INDEX `idx_open_api_client_ep_client`(`client_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户端可访问接口' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of open_api_client_endpoint
-- ----------------------------
INSERT INTO `open_api_client_endpoint` VALUES (595458373448572, 1);

-- ----------------------------
-- Table structure for open_api_client_ip
-- ----------------------------
DROP TABLE IF EXISTS `open_api_client_ip`;
CREATE TABLE `open_api_client_ip`  (
  `client_id` bigint NOT NULL COMMENT '客户端 ID',
  `ip_addr` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '允许的 IP',
  PRIMARY KEY (`client_id`, `ip_addr`) USING BTREE,
  INDEX `idx_open_api_client_ip_client`(`client_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户端 IP 白名单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of open_api_client_ip
-- ----------------------------

-- ----------------------------
-- Table structure for open_api_endpoint
-- ----------------------------
DROP TABLE IF EXISTS `open_api_endpoint`;
CREATE TABLE `open_api_endpoint`  (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口编码',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口名称',
  `http_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'HTTP 方法，如 GET/POST/*',
  `path_pattern` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Ant 路径，如 /api/open/demo/**',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_open_api_endpoint_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '开放接口目录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of open_api_endpoint
-- ----------------------------
INSERT INTO `open_api_endpoint` VALUES (1, 'open.demo.ping', '开放演示 Ping', 'GET', '/api/open/demo/ping', '脚手架演示接口', 1, 0, '2026-07-24 13:33:18.463', '2026-07-24 06:17:18.246', 2);

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器名称',
  `TRIGGER_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger 名称',
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger 分组',
  `BLOB_DATA` blob NULL COMMENT '序列化 Trigger 数据',
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `FK_QRTZ_BLOB_TRIGGERS` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Quartz：Blob Trigger 扩展' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_blob_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器名称',
  `CALENDAR_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日历名称',
  `CALENDAR` blob NOT NULL COMMENT '序列化日历对象',
  PRIMARY KEY (`SCHED_NAME`, `CALENDAR_NAME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Quartz：日历（节假日等排除规则）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器名称',
  `TRIGGER_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger 名称',
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger 分组',
  `CRON_EXPRESSION` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Cron 表达式',
  `TIME_ZONE_ID` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '时区 ID',
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `FK_QRTZ_CRON_TRIGGERS` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Quartz：Cron Trigger 扩展' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_cron_triggers
-- ----------------------------
INSERT INTO `qrtz_cron_triggers` VALUES ('omni-cluster', 'trigger-1325085100703352', 'omni-job', '0 * * * * ?', 'Asia/Shanghai');
INSERT INTO `qrtz_cron_triggers` VALUES ('omni-cluster', 'trigger-1718173365857657', 'omni-job', '0 * * * * ?', 'Asia/Shanghai');

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器名称',
  `ENTRY_ID` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '条目ID',
  `TRIGGER_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger 名称',
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger 分组',
  `INSTANCE_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行实例名',
  `FIRED_TIME` bigint NOT NULL COMMENT '实际触发时间（毫秒）',
  `SCHED_TIME` bigint NOT NULL COMMENT '计划触发时间（毫秒）',
  `PRIORITY` int NOT NULL COMMENT '优先级',
  `STATE` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态',
  `JOB_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Job 名称',
  `JOB_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Job 分组',
  `IS_NONCONCURRENT` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否禁止并发',
  `REQUESTS_RECOVERY` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否请求恢复',
  PRIMARY KEY (`SCHED_NAME`, `ENTRY_ID`) USING BTREE,
  INDEX `IDX_QRTZ_FT_TRIG_INST_NAME`(`SCHED_NAME` ASC, `INSTANCE_NAME` ASC) USING BTREE,
  INDEX `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY`(`SCHED_NAME` ASC, `INSTANCE_NAME` ASC, `REQUESTS_RECOVERY` ASC) USING BTREE,
  INDEX `IDX_QRTZ_FT_J_G`(`SCHED_NAME` ASC, `JOB_NAME` ASC, `JOB_GROUP` ASC) USING BTREE,
  INDEX `IDX_QRTZ_FT_JG`(`SCHED_NAME` ASC, `JOB_GROUP` ASC) USING BTREE,
  INDEX `IDX_QRTZ_FT_T_G`(`SCHED_NAME` ASC, `TRIGGER_NAME` ASC, `TRIGGER_GROUP` ASC) USING BTREE,
  INDEX `IDX_QRTZ_FT_TG`(`SCHED_NAME` ASC, `TRIGGER_GROUP` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Quartz：正在执行/已获取的 Trigger' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器名称',
  `JOB_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Job 名称',
  `JOB_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Job 分组',
  `DESCRIPTION` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Job 描述',
  `JOB_CLASS_NAME` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Job 实现类全限定名',
  `IS_DURABLE` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否持久化',
  `IS_NONCONCURRENT` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否禁止并发执行',
  `IS_UPDATE_DATA` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否更新 JobData',
  `REQUESTS_RECOVERY` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '故障后是否请求恢复',
  `JOB_DATA` blob NULL COMMENT 'Job 数据（序列化）',
  PRIMARY KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) USING BTREE,
  INDEX `IDX_QRTZ_J_REQ_RECOVERY`(`SCHED_NAME` ASC, `REQUESTS_RECOVERY` ASC) USING BTREE,
  INDEX `IDX_QRTZ_J_GRP`(`SCHED_NAME` ASC, `JOB_GROUP` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Quartz：Job 明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_job_details
-- ----------------------------
INSERT INTO `qrtz_job_details` VALUES ('omni-cluster', '1325085100703352', 'omni-job', '测试无参调用', 'com.omni.scaffolding.quartz.job.DisallowConcurrentBeanInvokeJob', '1', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C770800000010000000047400076A6F624E616D65740012E6B58BE8AF95E697A0E58F82E8B083E794A87400056A6F6249647372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700004B5286AB97E7874000C696E766F6B6554617267657474001973616D706C655363686564756C65645461736B732E70696E677400096A6F62506172616D737400007800);
INSERT INTO `qrtz_job_details` VALUES ('omni-cluster', '1718173365857657', 'omni-job', '测试有参调用', 'com.omni.scaffolding.quartz.job.DisallowConcurrentBeanInvokeJob', '1', '1', '0', '0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C770800000010000000047400076A6F624E616D65740012E6B58BE8AF95E69C89E58F82E8B083E794A87400056A6F6249647372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B020000787000061AAB69226D7974000C696E766F6B6554617267657474001973616D706C655363686564756C65645461736B732E6563686F7400096A6F62506172616D73740012E6B58BE8AF95E69C89E58F82E8B083E794A87800);

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器名称',
  `LOCK_NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '锁名称',
  PRIMARY KEY (`SCHED_NAME`, `LOCK_NAME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Quartz：集群悲观锁' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------
INSERT INTO `qrtz_locks` VALUES ('omni-cluster', 'STATE_ACCESS');
INSERT INTO `qrtz_locks` VALUES ('omni-cluster', 'TRIGGER_ACCESS');

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器名称',
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '已暂停的 Trigger 分组',
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_GROUP`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Quartz：已暂停的 Trigger 分组' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器名称',
  `INSTANCE_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实例名称',
  `LAST_CHECKIN_TIME` bigint NOT NULL COMMENT '最后签到时间（毫秒）',
  `CHECKIN_INTERVAL` bigint NOT NULL COMMENT '签到间隔（毫秒）',
  PRIMARY KEY (`SCHED_NAME`, `INSTANCE_NAME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Quartz：集群实例心跳状态' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_scheduler_state
-- ----------------------------
INSERT INTO `qrtz_scheduler_state` VALUES ('omni-cluster', 'DESKTOP-N2OD8271784873796651', 1784874004319, 10000);

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器名称',
  `TRIGGER_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger 名称',
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger 分组',
  `REPEAT_COUNT` bigint NOT NULL COMMENT '重复次数（-1 表示无限）',
  `REPEAT_INTERVAL` bigint NOT NULL COMMENT '重复间隔（毫秒）',
  `TIMES_TRIGGERED` bigint NOT NULL COMMENT '已触发次数',
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `FK_QRTZ_SIMPLE_TRIGGERS` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Quartz：Simple Trigger 扩展' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_simple_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器名称',
  `TRIGGER_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger 名称',
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger 分组',
  `STR_PROP_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '字符串属性1',
  `STR_PROP_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '字符串属性2',
  `STR_PROP_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '字符串属性3',
  `INT_PROP_1` int NULL DEFAULT NULL COMMENT '整型属性1',
  `INT_PROP_2` int NULL DEFAULT NULL COMMENT '整型属性2',
  `LONG_PROP_1` bigint NULL DEFAULT NULL COMMENT '长整型属性1',
  `LONG_PROP_2` bigint NULL DEFAULT NULL COMMENT '长整型属性2',
  `DEC_PROP_1` decimal(13, 4) NULL DEFAULT NULL COMMENT '小数属性1',
  `DEC_PROP_2` decimal(13, 4) NULL DEFAULT NULL COMMENT '小数属性2',
  `BOOL_PROP_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '布尔属性1',
  `BOOL_PROP_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '布尔属性2',
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `FK_QRTZ_SIMPROP_TRIGGERS` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Quartz：CalendarInterval/DailyTimeInterval 等属性 Trigger' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_simprop_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器名称',
  `TRIGGER_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger 名称',
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger 分组',
  `JOB_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联 Job 名称',
  `JOB_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联 Job 分组',
  `DESCRIPTION` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Trigger 描述',
  `NEXT_FIRE_TIME` bigint NULL DEFAULT NULL COMMENT '下次触发时间（毫秒时间戳）',
  `PREV_FIRE_TIME` bigint NULL DEFAULT NULL COMMENT '上次触发时间（毫秒时间戳）',
  `PRIORITY` int NULL DEFAULT NULL COMMENT '优先级',
  `TRIGGER_STATE` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态：WAITING/ACQUIRED/PAUSED 等',
  `TRIGGER_TYPE` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：CRON/SIMPLE/BLOB 等',
  `START_TIME` bigint NOT NULL COMMENT '开始时间（毫秒时间戳）',
  `END_TIME` bigint NULL DEFAULT NULL COMMENT '结束时间（毫秒时间戳）',
  `CALENDAR_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联日历名称',
  `MISFIRE_INSTR` smallint NULL DEFAULT NULL COMMENT '错过触发处理策略',
  `JOB_DATA` blob NULL COMMENT 'Trigger 附带 Job 数据',
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  INDEX `IDX_QRTZ_T_J`(`SCHED_NAME` ASC, `JOB_NAME` ASC, `JOB_GROUP` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_JG`(`SCHED_NAME` ASC, `JOB_GROUP` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_C`(`SCHED_NAME` ASC, `CALENDAR_NAME` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_G`(`SCHED_NAME` ASC, `TRIGGER_GROUP` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_STATE`(`SCHED_NAME` ASC, `TRIGGER_STATE` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_N_STATE`(`SCHED_NAME` ASC, `TRIGGER_NAME` ASC, `TRIGGER_GROUP` ASC, `TRIGGER_STATE` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_N_G_STATE`(`SCHED_NAME` ASC, `TRIGGER_GROUP` ASC, `TRIGGER_STATE` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_NEXT_FIRE_TIME`(`SCHED_NAME` ASC, `NEXT_FIRE_TIME` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_NFT_ST`(`SCHED_NAME` ASC, `TRIGGER_STATE` ASC, `NEXT_FIRE_TIME` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_NFT_MISFIRE`(`SCHED_NAME` ASC, `MISFIRE_INSTR` ASC, `NEXT_FIRE_TIME` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_NFT_ST_MISFIRE`(`SCHED_NAME` ASC, `MISFIRE_INSTR` ASC, `NEXT_FIRE_TIME` ASC, `TRIGGER_STATE` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP`(`SCHED_NAME` ASC, `MISFIRE_INSTR` ASC, `NEXT_FIRE_TIME` ASC, `TRIGGER_GROUP` ASC, `TRIGGER_STATE` ASC) USING BTREE,
  CONSTRAINT `FK_QRTZ_TRIGGERS_JOB` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `qrtz_job_details` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Quartz：Trigger 主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_triggers
-- ----------------------------
INSERT INTO `qrtz_triggers` VALUES ('omni-cluster', 'trigger-1325085100703352', 'omni-job', '1325085100703352', 'omni-job', 'Cron: 0 * * * * ?', 1784775360000, -1, 5, 'PAUSED', 'CRON', 1784775314000, 0, NULL, 2, '');
INSERT INTO `qrtz_triggers` VALUES ('omni-cluster', 'trigger-1718173365857657', 'omni-job', '1718173365857657', 'omni-job', 'Cron: 0 * * * * ?', 1784775360000, -1, 5, 'PAUSED', 'CRON', 1784775315000, 0, NULL, 2, '');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL,
  `config_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数键，业务引用',
  `config_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数名称',
  `config_value` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '参数值',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `sort` int NOT NULL DEFAULT 0,
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `builtin` tinyint(1) NOT NULL DEFAULT 0 COMMENT '内置参数不可删除',
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统参数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'sys.account.initPassword', '用户初始密码', 'Admin@123', '新建用户或重置密码时的默认明文密码提示值', 1, 1, 1, 0, '2026-07-23 09:05:09.286', '2026-07-23 09:05:09.286', 0);
INSERT INTO `sys_config` VALUES (2, 'sys.user.defaultAvatar', '默认头像', '', '用户未上传头像时的默认地址', 2, 1, 1, 0, '2026-07-23 09:05:09.286', '2026-07-23 09:05:09.286', 0);
INSERT INTO `sys_config` VALUES (3, 'sys.ui.title', '系统标题', 'Omni Admin', '浏览器标题 / 登录页展示名', 3, 1, 1, 0, '2026-07-23 09:05:09.286', '2026-07-23 09:05:09.286', 0);
INSERT INTO `sys_config` VALUES (5, 'sys.ui.watermark', '前端水印', 'true', '是否在管理端展示水印；true 展示，false 关闭', 4, 1, 1, 0, '2026-07-23 09:05:09.554', '2026-07-23 09:05:09.554', 0);

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint NOT NULL,
  `parent_id` bigint NOT NULL DEFAULT 0,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `ancestors` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0',
  `status` tinyint(1) NOT NULL DEFAULT 1,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_dept_parent`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '组织部门树' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (1, 0, '总部', 0, '0', 1, 0, '2026-07-23 09:05:07.999', '2026-07-23 09:05:07.999', 0);
INSERT INTO `sys_dept` VALUES (2, 1, '研发部', 1, '0,1', 1, 0, '2026-07-23 09:05:07.999', '2026-07-23 09:05:07.999', 0);
INSERT INTO `sys_dept` VALUES (3, 1, '销售部', 2, '0,1', 1, 0, '2026-07-23 09:05:07.999', '2026-07-23 09:05:07.999', 0);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `id` bigint NOT NULL,
  `type_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属字典类型编码',
  `label` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示标签',
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储值',
  `sort` int NOT NULL DEFAULT 0,
  `css_class` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '前端样式类',
  `default_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认项',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dict_data_type`(`type_code` ASC) USING BTREE,
  INDEX `idx_dict_data_type_value`(`type_code` ASC, `value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '字典数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 'sys_gender', '未知', 'UNKNOWN', 1, 'info', 1, 1, NULL, 0, '2026-07-23 09:05:09.244', '2026-07-23 09:05:09.244', 0);
INSERT INTO `sys_dict_data` VALUES (2, 'sys_gender', '男', 'MALE', 2, '', 0, 1, NULL, 0, '2026-07-23 09:05:09.244', '2026-07-23 09:05:09.244', 0);
INSERT INTO `sys_dict_data` VALUES (3, 'sys_gender', '女', 'FEMALE', 3, '', 0, 1, NULL, 0, '2026-07-23 09:05:09.244', '2026-07-23 09:05:09.244', 0);
INSERT INTO `sys_dict_data` VALUES (4, 'sys_yes_no', '是', 'Y', 1, 'success', 0, 1, NULL, 0, '2026-07-23 09:05:09.244', '2026-07-23 09:05:09.244', 0);
INSERT INTO `sys_dict_data` VALUES (5, 'sys_yes_no', '否', 'N', 2, 'info', 1, 1, NULL, 0, '2026-07-23 09:05:09.244', '2026-07-23 09:05:09.244', 0);
INSERT INTO `sys_dict_data` VALUES (6, 'sys_normal_disable', '正常', '0', 1, 'success', 1, 1, NULL, 0, '2026-07-23 09:05:09.244', '2026-07-23 09:05:09.244', 0);
INSERT INTO `sys_dict_data` VALUES (7, 'sys_normal_disable', '停用', '1', 2, 'danger', 0, 1, NULL, 0, '2026-07-23 09:05:09.244', '2026-07-23 09:05:09.244', 0);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint NOT NULL,
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典类型编码，业务引用键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典类型名称',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `sort` int NOT NULL DEFAULT 0,
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_dict_type_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '字典类型' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, 'sys_gender', '用户性别', '对齐用户 gender 字段', 1, 1, 0, '2026-07-23 09:05:09.240', '2026-07-23 09:05:09.240', 0);
INSERT INTO `sys_dict_type` VALUES (2, 'sys_yes_no', '是否', '通用是否', 2, 1, 0, '2026-07-23 09:05:09.240', '2026-07-23 09:05:09.240', 0);
INSERT INTO `sys_dict_type` VALUES (3, 'sys_normal_disable', '系统状态', '正常/停用', 3, 1, 0, '2026-07-23 09:05:09.240', '2026-07-23 09:05:09.240', 0);

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`  (
  `id` bigint NOT NULL COMMENT '主键',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
  `content_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'MIME 类型',
  `size_bytes` bigint NOT NULL DEFAULT 0 COMMENT '字节大小',
  `storage_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'LOCAL / MINIO / OSS',
  `oss_provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'OSS 插件 id，如 aliyun',
  `object_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储对象键/相对路径',
  `biz_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'common' COMMENT '业务类型，如 avatar/common',
  `md5` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '可选 MD5',
  `created_by` bigint NULL DEFAULT NULL COMMENT '上传人用户 ID',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 1 已删',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_file_biz`(`biz_type` ASC) USING BTREE,
  INDEX `idx_sys_file_created`(`created_at` ASC) USING BTREE,
  INDEX `idx_sys_file_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一文件元数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_file
-- ----------------------------
INSERT INTO `sys_file` VALUES (459102358155861, '微信图片_2026-07-08_135601_219.jpg', 'image/jpeg', 92762, 'LOCAL', NULL, 'avatar/004a737c8c5c4f3f89808810ef08af74.jpg', 'avatar', NULL, 1, 0, '2026-07-23 06:21:59.299', '2026-07-23 06:21:59.299', 0);
INSERT INTO `sys_file` VALUES (511419486362917, '微信图片_20260713083418_298_6.jpg', 'image/jpeg', 1277396, 'LOCAL', NULL, 'common/5da6ce88fb304dcdb50c90896e49370c.jpg', 'common', NULL, 1, 0, '2026-07-23 03:43:16.118', '2026-07-23 03:43:16.118', 0);
INSERT INTO `sys_file` VALUES (3316986718942062, '2025-12-23.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 92681, 'LOCAL', NULL, 'common/435dbc20659546619078d3b0438d0dfe.xlsx', 'common', NULL, 1, 0, '2026-07-23 03:45:06.603', '2026-07-23 03:45:06.603', 0);
INSERT INTO `sys_file` VALUES (6401387236930554, 'ryd_app_logo.png', 'image/png', 47430, 'LOCAL', NULL, 'avatar/6bad4a6981474241a96a66c7d9f9c86f.png', 'avatar', NULL, 1, 0, '2026-07-23 05:57:30.977', '2026-07-23 05:57:30.977', 0);

-- ----------------------------
-- Table structure for sys_ip_whitelist
-- ----------------------------
DROP TABLE IF EXISTS `sys_ip_whitelist`;
CREATE TABLE `sys_ip_whitelist`  (
  `id` bigint NOT NULL,
  `ip_addr` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'IP 地址',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_ip_whitelist_ip`(`ip_addr` ASC) USING BTREE,
  INDEX `idx_sys_ip_whitelist_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'IP 白名单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_ip_whitelist
-- ----------------------------
INSERT INTO `sys_ip_whitelist` VALUES (1, '127.0.0.1', '本机 IPv4', 1, 0, '2026-07-23 09:05:09.543', '2026-07-23 09:05:09.543', 0);
INSERT INTO `sys_ip_whitelist` VALUES (2, '0:0:0:0:0:0:0:1', '本机 IPv6', 1, 0, '2026-07-23 09:05:09.543', '2026-07-23 09:05:09.543', 0);

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
  `id` bigint NOT NULL,
  `job_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'omni-job' COMMENT '任务组',
  `invoke_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用目标 beanName.methodName',
  `job_params` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务参数（可空=无参）',
  `cron_expression` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Cron 表达式',
  `misfire_policy` tinyint NOT NULL DEFAULT 0 COMMENT '0忽略 1立即触发一次 2触发所有错过',
  `concurrent` tinyint(1) NOT NULL DEFAULT 0 COMMENT '1允许并发 0禁止',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_job_name`(`job_name` ASC) USING BTREE,
  INDEX `idx_sys_job_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '定时任务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job
-- ----------------------------
INSERT INTO `sys_job` VALUES (1325085100703352, '测试无参调用', 'omni-job', 'sampleScheduledTasks.ping', NULL, '0 * * * * ?', 0, 0, 0, NULL, 0, '2026-07-23 02:49:29.590', '2026-07-23 02:55:14.939', 1);
INSERT INTO `sys_job` VALUES (1718173365857657, '测试有参调用', 'omni-job', 'sampleScheduledTasks.echo', '测试有参调用', '0 * * * * ?', 0, 0, 0, NULL, 0, '2026-07-23 02:50:08.072', '2026-07-23 02:55:15.515', 1);

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`  (
  `id` bigint NOT NULL,
  `job_id` bigint NOT NULL COMMENT '任务 ID',
  `job_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `invoke_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用目标',
  `job_params` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务参数',
  `status` tinyint(1) NOT NULL COMMENT '1成功 0失败',
  `message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结果/异常摘要',
  `start_time` datetime(3) NOT NULL COMMENT '开始时间',
  `end_time` datetime(3) NULL DEFAULT NULL COMMENT '结束时间',
  `cost_ms` bigint NULL DEFAULT NULL COMMENT '耗时毫秒',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_job_log_job`(`job_id` ASC) USING BTREE,
  INDEX `idx_sys_job_log_start`(`start_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '定时任务执行日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------
INSERT INTO `sys_job_log` VALUES (319596217088246, 1325085100703352, '测试无参调用', 'sampleScheduledTasks.ping', '', 1, 'OK', '2026-07-23 02:53:00.022', '2026-07-23 02:53:00.022', 0);
INSERT INTO `sys_job_log` VALUES (628559195192419, 1325085100703352, '测试无参调用', 'sampleScheduledTasks.ping', '', 1, 'OK', '2026-07-23 02:52:00.021', '2026-07-23 02:52:00.021', 0);
INSERT INTO `sys_job_log` VALUES (1181463327545910, 1718173365857657, '测试有参调用', 'sampleScheduledTasks.echo', '测试有参调用', 1, 'OK', '2026-07-23 02:51:00.049', '2026-07-23 02:51:00.049', 0);
INSERT INTO `sys_job_log` VALUES (2913156501680084, 1718173365857657, '测试有参调用', 'sampleScheduledTasks.echo', '测试有参调用', 1, 'OK', '2026-07-23 02:53:00.066', '2026-07-23 02:53:00.066', 0);
INSERT INTO `sys_job_log` VALUES (3693804621011564, 1325085100703352, '测试无参调用', 'sampleScheduledTasks.ping', '', 1, 'OK', '2026-07-23 02:54:00.018', '2026-07-23 02:54:00.018', 0);
INSERT INTO `sys_job_log` VALUES (5131984042068293, 1325085100703352, '测试无参调用', 'sampleScheduledTasks.ping', '', 1, 'OK', '2026-07-23 02:50:00.020', '2026-07-23 02:50:00.021', 1);
INSERT INTO `sys_job_log` VALUES (5178702931799123, 1718173365857657, '测试有参调用', 'sampleScheduledTasks.echo', '测试有参调用', 1, 'OK', '2026-07-23 02:52:00.051', '2026-07-23 02:52:00.051', 0);
INSERT INTO `sys_job_log` VALUES (7253483307304080, 1325085100703352, '测试无参调用', 'sampleScheduledTasks.ping', '', 1, 'OK', '2026-07-23 02:51:00.022', '2026-07-23 02:51:00.022', 0);
INSERT INTO `sys_job_log` VALUES (7295748923535601, 1718173365857657, '测试有参调用', 'sampleScheduledTasks.echo', '测试有参调用', 1, 'OK', '2026-07-23 02:54:00.050', '2026-07-23 02:54:00.050', 0);
INSERT INTO `sys_job_log` VALUES (7776689393096826, 1325085100703352, '测试无参调用', 'sampleScheduledTasks.ping', '', 1, 'OK', '2026-07-23 02:55:00.056', '2026-07-23 02:55:00.056', 0);
INSERT INTO `sys_job_log` VALUES (7970279519379106, 1325085100703352, '测试无参调用', 'sampleScheduledTasks.ping', '', 1, 'OK', '2026-07-23 02:49:36.917', '2026-07-23 02:49:36.918', 1);
INSERT INTO `sys_job_log` VALUES (8094113293252170, 1718173365857657, '测试有参调用', 'sampleScheduledTasks.echo', '测试有参调用', 1, 'OK', '2026-07-23 02:55:00.101', '2026-07-23 02:55:00.101', 0);

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL,
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户 ID，未知账号可为空',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端 IP',
  `user_agent` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'UA',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SUCCESS / FAIL',
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结果说明',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链路 ID',
  `login_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_login_log_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_login_log_username`(`username` ASC) USING BTREE,
  INDEX `idx_login_log_status`(`status` ASC) USING BTREE,
  INDEX `idx_login_log_time`(`login_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '登录日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------
INSERT INTO `sys_login_log` VALUES (1007210125766924, 1, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '登录成功', '92360fc90ad04646b72c50030ca84259', '2026-07-23 09:09:04.322');
INSERT INTO `sys_login_log` VALUES (1284246678677475, NULL, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'FAIL', '验证码错误', '0f4a63dfc3f64d2f857be3b3826c0b86', '2026-07-24 00:28:39.657');
INSERT INTO `sys_login_log` VALUES (1310026957542900, 1, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '登录成功', 'a7b78414ab04460f9731696a3f3b7263', '2026-07-23 03:16:57.924');
INSERT INTO `sys_login_log` VALUES (1314644188122388, 1, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '登录成功', '344e27041ceb43d5beb15f6b5d49fa14', '2026-07-23 05:48:13.085');
INSERT INTO `sys_login_log` VALUES (4223018688810666, 1, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '登录成功', '3934f5edf13c415ea45d35dc3dc8594b', '2026-07-23 07:38:58.605');
INSERT INTO `sys_login_log` VALUES (4366271268674718, 1, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '登录成功', 'b76faa303c8f4539b8bff79822ed0f6a', '2026-07-23 04:57:52.304');
INSERT INTO `sys_login_log` VALUES (4707551005179593, 1, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '登录成功', '1125fedfbe5c45f7ad988cdb7509e5aa', '2026-07-24 03:02:02.459');
INSERT INTO `sys_login_log` VALUES (5445604984271740, 1, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '登录成功', '8b6dfb616b4c4dad90421a7c39d2b560', '2026-07-23 05:52:11.822');
INSERT INTO `sys_login_log` VALUES (6646036533756785, 1, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '登录成功', '58333c1e0b954892822763d76bea28fd', '2026-07-23 07:30:14.151');
INSERT INTO `sys_login_log` VALUES (6689299597474104, 1, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '登录成功', '95d1454b6616475f933b611a2294a99e', '2026-07-23 09:18:00.350');
INSERT INTO `sys_login_log` VALUES (7111253921452047, 1, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '登录成功', 'f4c125c7092c4090b72fc5f4fef37bf9', '2026-07-23 09:10:43.959');
INSERT INTO `sys_login_log` VALUES (7311352341317075, 1, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '登录成功', 'c88e513ff9f745d3bbb1da9da3fe1ad8', '2026-07-24 00:28:58.738');
INSERT INTO `sys_login_log` VALUES (7514432794357866, NULL, 'admin', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'FAIL', '验证码已过期，请刷新', '0b2a1d55757e49e98c998fcdc3a06032', '2026-07-23 09:17:55.952');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL,
  `parent_id` bigint NOT NULL DEFAULT 0,
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'DIR / MENU / BUTTON',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `component` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `perms` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限码，按钮/菜单可见性',
  `sort` int NOT NULL DEFAULT 0,
  `visible` tinyint(1) NOT NULL DEFAULT 1,
  `status` tinyint(1) NOT NULL DEFAULT 1,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_menu_parent`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单与按钮权限' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, 'DIR', '系统管理', '/system', NULL, 'Setting', NULL, 1, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (10, 1, 'MENU', '用户管理', 'user', 'system/user/index', 'User', 'system:user:list', 1, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (11, 10, 'BUTTON', '用户查询', NULL, NULL, NULL, 'system:user:query', 1, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (12, 10, 'BUTTON', '用户新增', NULL, NULL, NULL, 'system:user:add', 2, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (13, 10, 'BUTTON', '用户修改', NULL, NULL, NULL, 'system:user:edit', 3, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (14, 10, 'BUTTON', '用户删除', NULL, NULL, NULL, 'system:user:remove', 4, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (15, 10, 'BUTTON', '重置密码', NULL, NULL, NULL, 'system:user:resetPwd', 5, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (16, 10, 'BUTTON', '用户导出', NULL, NULL, NULL, 'system:user:export', 6, 1, 1, 0, '2026-07-23 09:05:09.143', '2026-07-23 09:05:09.143', 0);
INSERT INTO `sys_menu` VALUES (20, 1, 'MENU', '角色管理', 'role', 'system/role/index', 'UserFilled', 'system:role:list', 2, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (21, 20, 'BUTTON', '角色查询', NULL, NULL, NULL, 'system:role:query', 1, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (22, 20, 'BUTTON', '角色新增', NULL, NULL, NULL, 'system:role:add', 2, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (23, 20, 'BUTTON', '角色修改', NULL, NULL, NULL, 'system:role:edit', 3, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (24, 20, 'BUTTON', '角色删除', NULL, NULL, NULL, 'system:role:remove', 4, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (25, 20, 'BUTTON', '角色导出', NULL, NULL, NULL, 'system:role:export', 5, 1, 1, 0, '2026-07-23 09:05:09.143', '2026-07-23 09:05:09.143', 0);
INSERT INTO `sys_menu` VALUES (30, 1, 'MENU', '部门管理', 'dept', 'system/dept/index', 'OfficeBuilding', 'system:dept:list', 3, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (31, 30, 'BUTTON', '部门查询', NULL, NULL, NULL, 'system:dept:query', 1, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (32, 30, 'BUTTON', '部门新增', NULL, NULL, NULL, 'system:dept:add', 2, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (33, 30, 'BUTTON', '部门修改', NULL, NULL, NULL, 'system:dept:edit', 3, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (34, 30, 'BUTTON', '部门删除', NULL, NULL, NULL, 'system:dept:remove', 4, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (35, 30, 'BUTTON', '部门导出', NULL, NULL, NULL, 'system:dept:export', 5, 1, 1, 0, '2026-07-23 09:05:09.143', '2026-07-23 09:05:09.143', 0);
INSERT INTO `sys_menu` VALUES (40, 1, 'MENU', '菜单管理', 'menu', 'system/menu/index', 'Menu', 'system:menu:list', 4, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (41, 40, 'BUTTON', '菜单查询', NULL, NULL, NULL, 'system:menu:query', 1, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (42, 40, 'BUTTON', '菜单新增', NULL, NULL, NULL, 'system:menu:add', 2, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (43, 40, 'BUTTON', '菜单修改', NULL, NULL, NULL, 'system:menu:edit', 3, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (44, 40, 'BUTTON', '菜单删除', NULL, NULL, NULL, 'system:menu:remove', 4, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (50, 1, 'MENU', '岗位管理', 'post', 'system/post/index', 'Postcard', 'system:post:list', 5, 1, 1, 0, '2026-07-23 09:05:09.054', '2026-07-23 09:05:09.054', 0);
INSERT INTO `sys_menu` VALUES (51, 50, 'BUTTON', '岗位查询', NULL, NULL, NULL, 'system:post:query', 1, 1, 1, 0, '2026-07-23 09:05:09.054', '2026-07-23 09:05:09.054', 0);
INSERT INTO `sys_menu` VALUES (52, 50, 'BUTTON', '岗位新增', NULL, NULL, NULL, 'system:post:add', 2, 1, 1, 0, '2026-07-23 09:05:09.054', '2026-07-23 09:05:09.054', 0);
INSERT INTO `sys_menu` VALUES (53, 50, 'BUTTON', '岗位修改', NULL, NULL, NULL, 'system:post:edit', 3, 1, 1, 0, '2026-07-23 09:05:09.054', '2026-07-23 09:05:09.054', 0);
INSERT INTO `sys_menu` VALUES (54, 50, 'BUTTON', '岗位删除', NULL, NULL, NULL, 'system:post:remove', 4, 1, 1, 0, '2026-07-23 09:05:09.054', '2026-07-23 09:05:09.054', 0);
INSERT INTO `sys_menu` VALUES (55, 50, 'BUTTON', '岗位导出', NULL, NULL, NULL, 'system:post:export', 5, 1, 1, 0, '2026-07-23 09:05:09.143', '2026-07-23 09:05:09.143', 0);
INSERT INTO `sys_menu` VALUES (60, 1, 'MENU', '数据字典', 'dict', 'system/dict/index', 'Collection', 'system:dict:list', 6, 1, 1, 0, '2026-07-23 09:05:09.247', '2026-07-23 09:05:09.247', 0);
INSERT INTO `sys_menu` VALUES (61, 60, 'BUTTON', '字典查询', NULL, NULL, NULL, 'system:dict:query', 1, 1, 1, 0, '2026-07-23 09:05:09.247', '2026-07-23 09:05:09.247', 0);
INSERT INTO `sys_menu` VALUES (62, 60, 'BUTTON', '字典新增', NULL, NULL, NULL, 'system:dict:add', 2, 1, 1, 0, '2026-07-23 09:05:09.247', '2026-07-23 09:05:09.247', 0);
INSERT INTO `sys_menu` VALUES (63, 60, 'BUTTON', '字典修改', NULL, NULL, NULL, 'system:dict:edit', 3, 1, 1, 0, '2026-07-23 09:05:09.247', '2026-07-23 09:05:09.247', 0);
INSERT INTO `sys_menu` VALUES (64, 60, 'BUTTON', '字典删除', NULL, NULL, NULL, 'system:dict:remove', 4, 1, 1, 0, '2026-07-23 09:05:09.247', '2026-07-23 09:05:09.247', 0);
INSERT INTO `sys_menu` VALUES (65, 60, 'BUTTON', '字典导出', NULL, NULL, NULL, 'system:dict:export', 5, 1, 1, 0, '2026-07-23 09:05:09.247', '2026-07-23 09:05:09.247', 0);
INSERT INTO `sys_menu` VALUES (70, 1, 'MENU', '系统参数', 'config', 'system/config/index', 'Setting', 'system:config:list', 7, 1, 1, 0, '2026-07-23 09:05:09.290', '2026-07-23 09:05:09.290', 0);
INSERT INTO `sys_menu` VALUES (71, 70, 'BUTTON', '参数查询', NULL, NULL, NULL, 'system:config:query', 1, 1, 1, 0, '2026-07-23 09:05:09.290', '2026-07-23 09:05:09.290', 0);
INSERT INTO `sys_menu` VALUES (72, 70, 'BUTTON', '参数新增', NULL, NULL, NULL, 'system:config:add', 2, 1, 1, 0, '2026-07-23 09:05:09.290', '2026-07-23 09:05:09.290', 0);
INSERT INTO `sys_menu` VALUES (73, 70, 'BUTTON', '参数修改', NULL, NULL, NULL, 'system:config:edit', 3, 1, 1, 0, '2026-07-23 09:05:09.290', '2026-07-23 09:05:09.290', 0);
INSERT INTO `sys_menu` VALUES (74, 70, 'BUTTON', '参数删除', NULL, NULL, NULL, 'system:config:remove', 4, 1, 1, 0, '2026-07-23 09:05:09.290', '2026-07-23 09:05:09.290', 0);
INSERT INTO `sys_menu` VALUES (75, 70, 'BUTTON', '参数导出', NULL, NULL, NULL, 'system:config:export', 5, 1, 1, 0, '2026-07-23 09:05:09.290', '2026-07-23 09:05:09.290', 0);
INSERT INTO `sys_menu` VALUES (76, 70, 'BUTTON', '参数刷新缓存', NULL, NULL, NULL, 'system:config:refresh', 6, 1, 1, 0, '2026-07-23 09:05:09.551', '2026-07-23 09:05:09.551', 0);
INSERT INTO `sys_menu` VALUES (80, 1, 'MENU', '在线用户', 'online', 'system/online/index', 'Connection', 'system:online:list', 8, 1, 1, 0, '2026-07-23 09:05:09.294', '2026-07-23 09:05:09.294', 0);
INSERT INTO `sys_menu` VALUES (81, 80, 'BUTTON', '在线查询', NULL, NULL, NULL, 'system:online:query', 1, 1, 1, 0, '2026-07-23 09:05:09.294', '2026-07-23 09:05:09.294', 0);
INSERT INTO `sys_menu` VALUES (82, 80, 'BUTTON', '强制下线', NULL, NULL, NULL, 'system:online:kick', 2, 1, 1, 0, '2026-07-23 09:05:09.294', '2026-07-23 09:05:09.294', 0);
INSERT INTO `sys_menu` VALUES (83, 1, 'MENU', '通知公告', 'notice', 'system/notice/index', 'Bell', 'system:notice:list', 9, 1, 1, 0, '2026-07-23 09:05:09.396', '2026-07-23 09:05:09.396', 0);
INSERT INTO `sys_menu` VALUES (84, 83, 'BUTTON', '公告查询', NULL, NULL, NULL, 'system:notice:query', 1, 1, 1, 0, '2026-07-23 09:05:09.396', '2026-07-23 09:05:09.396', 0);
INSERT INTO `sys_menu` VALUES (85, 83, 'BUTTON', '公告新增', NULL, NULL, NULL, 'system:notice:add', 2, 1, 1, 0, '2026-07-23 09:05:09.396', '2026-07-23 09:05:09.396', 0);
INSERT INTO `sys_menu` VALUES (86, 83, 'BUTTON', '公告修改', NULL, NULL, NULL, 'system:notice:edit', 3, 1, 1, 0, '2026-07-23 09:05:09.396', '2026-07-23 09:05:09.396', 0);
INSERT INTO `sys_menu` VALUES (87, 83, 'BUTTON', '公告删除', NULL, NULL, NULL, 'system:notice:remove', 4, 1, 1, 0, '2026-07-23 09:05:09.396', '2026-07-23 09:05:09.396', 0);
INSERT INTO `sys_menu` VALUES (88, 1, 'MENU', '定时任务', 'job', 'system/job/index', 'Timer', 'system:job:list', 9, 1, 1, 0, '2026-07-23 09:05:09.505', '2026-07-23 09:05:09.505', 0);
INSERT INTO `sys_menu` VALUES (89, 88, 'BUTTON', '任务查询', NULL, NULL, NULL, 'system:job:query', 1, 1, 1, 0, '2026-07-23 09:05:09.505', '2026-07-23 09:05:09.505', 0);
INSERT INTO `sys_menu` VALUES (90, 88, 'BUTTON', '任务新增', NULL, NULL, NULL, 'system:job:add', 2, 1, 1, 0, '2026-07-23 09:05:09.505', '2026-07-23 09:05:09.505', 0);
INSERT INTO `sys_menu` VALUES (91, 88, 'BUTTON', '任务修改', NULL, NULL, NULL, 'system:job:edit', 3, 1, 1, 0, '2026-07-23 09:05:09.505', '2026-07-23 09:05:09.505', 0);
INSERT INTO `sys_menu` VALUES (92, 88, 'BUTTON', '任务删除', NULL, NULL, NULL, 'system:job:remove', 4, 1, 1, 0, '2026-07-23 09:05:09.505', '2026-07-23 09:05:09.505', 0);
INSERT INTO `sys_menu` VALUES (93, 88, 'BUTTON', '任务执行', NULL, NULL, NULL, 'system:job:run', 5, 1, 1, 0, '2026-07-23 09:05:09.505', '2026-07-23 09:05:09.505', 0);
INSERT INTO `sys_menu` VALUES (94, 1, 'MENU', 'IP白名单', 'ip-whitelist', 'system/ip-whitelist/index', 'Lock', 'system:ipWhitelist:list', 10, 1, 1, 0, '2026-07-23 09:05:09.546', '2026-07-23 09:05:09.546', 0);
INSERT INTO `sys_menu` VALUES (95, 94, 'BUTTON', '白名单查询', NULL, NULL, NULL, 'system:ipWhitelist:query', 1, 1, 1, 0, '2026-07-23 09:05:09.546', '2026-07-23 09:05:09.546', 0);
INSERT INTO `sys_menu` VALUES (96, 94, 'BUTTON', '白名单新增', NULL, NULL, NULL, 'system:ipWhitelist:add', 2, 1, 1, 0, '2026-07-23 09:05:09.546', '2026-07-23 09:05:09.546', 0);
INSERT INTO `sys_menu` VALUES (97, 94, 'BUTTON', '白名单修改', NULL, NULL, NULL, 'system:ipWhitelist:edit', 3, 1, 1, 0, '2026-07-23 09:05:09.546', '2026-07-23 09:05:09.546', 0);
INSERT INTO `sys_menu` VALUES (98, 94, 'BUTTON', '白名单删除', NULL, NULL, NULL, 'system:ipWhitelist:remove', 4, 1, 1, 0, '2026-07-23 09:05:09.546', '2026-07-23 09:05:09.546', 0);
INSERT INTO `sys_menu` VALUES (99, 94, 'BUTTON', '白名单刷新缓存', NULL, NULL, NULL, 'system:ipWhitelist:refresh', 5, 1, 1, 0, '2026-07-23 09:05:09.551', '2026-07-23 09:05:09.551', 0);
INSERT INTO `sys_menu` VALUES (100, 0, 'DIR', '运维', '/ops', NULL, 'Monitor', NULL, 2, 1, 1, 0, '2026-07-23 09:05:08.968', '2026-07-23 09:05:08.968', 0);
INSERT INTO `sys_menu` VALUES (110, 1, 'MENU', '文件管理', 'file', 'system/file/index', 'FolderOpened', 'system:file:list', 11, 1, 1, 0, '2026-07-23 11:41:16.514', '2026-07-23 11:41:16.514', 0);
INSERT INTO `sys_menu` VALUES (111, 110, 'BUTTON', '文件查询', NULL, NULL, NULL, 'system:file:query', 1, 1, 1, 0, '2026-07-23 11:41:16.514', '2026-07-23 11:41:16.514', 0);
INSERT INTO `sys_menu` VALUES (112, 110, 'BUTTON', '文件上传', NULL, NULL, NULL, 'system:file:upload', 2, 1, 1, 0, '2026-07-23 11:41:16.514', '2026-07-23 11:41:16.514', 0);
INSERT INTO `sys_menu` VALUES (113, 110, 'BUTTON', '文件删除', NULL, NULL, NULL, 'system:file:remove', 3, 1, 1, 0, '2026-07-23 11:41:16.514', '2026-07-23 11:41:16.514', 0);
INSERT INTO `sys_menu` VALUES (130, 100, 'MENU', 'Redis', 'redis', 'ops/redis/index', 'Coin', 'ops:redis:list', 3, 1, 1, 0, '2026-07-23 09:05:09.400', '2026-07-23 09:05:09.400', 0);
INSERT INTO `sys_menu` VALUES (131, 130, 'BUTTON', 'Redis查询', NULL, NULL, NULL, 'ops:redis:query', 1, 1, 1, 0, '2026-07-23 09:05:09.400', '2026-07-23 09:05:09.400', 0);
INSERT INTO `sys_menu` VALUES (132, 130, 'BUTTON', 'Redis修改', NULL, NULL, NULL, 'ops:redis:edit', 2, 1, 1, 0, '2026-07-23 09:05:09.400', '2026-07-23 09:05:09.400', 0);
INSERT INTO `sys_menu` VALUES (133, 130, 'BUTTON', 'Redis删除', NULL, NULL, NULL, 'ops:redis:remove', 3, 1, 1, 0, '2026-07-23 09:05:09.400', '2026-07-23 09:05:09.400', 0);
INSERT INTO `sys_menu` VALUES (140, 100, 'MENU', '系统详情', 'server', 'ops/server/index', 'Monitor', 'ops:server:list', 4, 1, 1, 0, '2026-07-23 09:05:09.403', '2026-07-23 09:05:09.403', 0);
INSERT INTO `sys_menu` VALUES (141, 140, 'BUTTON', '系统详情查询', NULL, NULL, NULL, 'ops:server:query', 1, 1, 1, 0, '2026-07-23 09:05:09.403', '2026-07-23 09:05:09.403', 0);
INSERT INTO `sys_menu` VALUES (150, 100, 'MENU', 'Druid监控', 'druid', 'ops/druid/index', 'DataAnalysis', 'ops:druid:list', 5, 1, 1, 0, '2026-07-23 09:05:09.406', '2026-07-23 09:05:09.406', 0);
INSERT INTO `sys_menu` VALUES (151, 150, 'BUTTON', 'Druid查看', NULL, NULL, NULL, 'ops:druid:query', 1, 1, 1, 0, '2026-07-23 09:05:09.406', '2026-07-23 09:05:09.406', 0);
INSERT INTO `sys_menu` VALUES (160, 100, 'MENU', 'MySQL', 'mysql', 'ops/mysql/index', 'Grid', 'ops:mysql:list', 6, 1, 1, 0, '2026-07-23 09:05:09.409', '2026-07-23 09:05:09.409', 0);
INSERT INTO `sys_menu` VALUES (161, 160, 'BUTTON', 'MySQL查询', NULL, NULL, NULL, 'ops:mysql:query', 1, 1, 1, 0, '2026-07-23 09:05:09.409', '2026-07-23 09:05:09.409', 0);
INSERT INTO `sys_menu` VALUES (162, 160, 'BUTTON', 'MySQL修改', NULL, NULL, NULL, 'ops:mysql:edit', 2, 1, 1, 0, '2026-07-23 09:05:09.409', '2026-07-23 09:05:09.409', 0);
INSERT INTO `sys_menu` VALUES (163, 160, 'BUTTON', 'MySQL删除', NULL, NULL, NULL, 'ops:mysql:remove', 3, 1, 1, 0, '2026-07-23 09:05:09.409', '2026-07-23 09:05:09.409', 0);
INSERT INTO `sys_menu` VALUES (200, 0, 'DIR', '演示', '/demo', NULL, 'Goods', NULL, 3, 1, 1, 1, '2026-07-23 09:05:08.968', '2026-07-23 04:59:04.317', 1);
INSERT INTO `sys_menu` VALUES (210, 200, 'MENU', '商品演示', 'product', NULL, 'Box', 'demo:product:list', 1, 1, 1, 1, '2026-07-23 09:05:08.968', '2026-07-23 04:59:02.165', 1);
INSERT INTO `sys_menu` VALUES (211, 210, 'BUTTON', '商品查询', NULL, NULL, NULL, 'demo:product:read', 1, 1, 1, 1, '2026-07-23 09:05:08.968', '2026-07-23 04:58:59.581', 1);
INSERT INTO `sys_menu` VALUES (212, 210, 'BUTTON', '商品写入', NULL, NULL, NULL, 'demo:product:write', 2, 1, 1, 1, '2026-07-23 09:05:08.968', '2026-07-23 04:58:57.510', 1);
INSERT INTO `sys_menu` VALUES (300, 0, 'DIR', '日志管理', '/log', NULL, 'Document', NULL, 4, 1, 1, 0, '2026-07-23 09:05:09.139', '2026-07-23 09:05:09.139', 0);
INSERT INTO `sys_menu` VALUES (310, 300, 'MENU', '登录日志', 'login-log', 'system/login-log/index', 'Key', 'system:loginLog:list', 1, 1, 1, 0, '2026-07-23 09:05:09.139', '2026-07-23 09:05:09.139', 0);
INSERT INTO `sys_menu` VALUES (311, 310, 'BUTTON', '登录日志查询', NULL, NULL, NULL, 'system:loginLog:query', 1, 1, 1, 0, '2026-07-23 09:05:09.139', '2026-07-23 09:05:09.139', 0);
INSERT INTO `sys_menu` VALUES (312, 310, 'BUTTON', '登录日志删除', NULL, NULL, NULL, 'system:loginLog:remove', 2, 1, 1, 0, '2026-07-23 09:05:09.139', '2026-07-23 09:05:09.139', 0);
INSERT INTO `sys_menu` VALUES (320, 300, 'MENU', '操作日志', 'oper-log', 'system/oper-log/index', 'Tickets', 'system:operLog:list', 2, 1, 1, 0, '2026-07-23 09:05:09.139', '2026-07-23 09:05:09.139', 0);
INSERT INTO `sys_menu` VALUES (321, 320, 'BUTTON', '操作日志查询', NULL, NULL, NULL, 'system:operLog:query', 1, 1, 1, 0, '2026-07-23 09:05:09.139', '2026-07-23 09:05:09.139', 0);
INSERT INTO `sys_menu` VALUES (322, 320, 'BUTTON', '操作日志删除', NULL, NULL, NULL, 'system:operLog:remove', 2, 1, 1, 0, '2026-07-23 09:05:09.139', '2026-07-23 09:05:09.139', 0);
INSERT INTO `sys_menu` VALUES (400, 0, 'DIR', '工具', '/tool', NULL, 'Tools', NULL, 5, 1, 1, 0, '2026-07-23 09:05:09.555', '2026-07-23 09:05:09.555', 0);
INSERT INTO `sys_menu` VALUES (410, 400, 'MENU', '代码生成', 'gen', 'tool/gen/index', 'Document', 'tool:gen:list', 1, 1, 1, 0, '2026-07-23 09:05:09.555', '2026-07-23 09:05:09.555', 0);
INSERT INTO `sys_menu` VALUES (411, 410, 'BUTTON', '生成查询', NULL, NULL, NULL, 'tool:gen:query', 1, 1, 1, 0, '2026-07-23 09:05:09.555', '2026-07-23 09:05:09.555', 0);
INSERT INTO `sys_menu` VALUES (412, 410, 'BUTTON', '生成预览', NULL, NULL, NULL, 'tool:gen:preview', 2, 1, 1, 0, '2026-07-23 09:05:09.555', '2026-07-23 09:05:09.555', 0);
INSERT INTO `sys_menu` VALUES (413, 410, 'BUTTON', '生成代码', NULL, NULL, NULL, 'tool:gen:code', 3, 1, 1, 0, '2026-07-23 09:05:09.555', '2026-07-23 09:05:09.555', 0);
INSERT INTO `sys_menu` VALUES (500, 0, 'DIR', '开放管理', '/open', NULL, 'Key', NULL, 4, 1, 1, 0, '2026-07-24 13:33:18.469', '2026-07-24 13:33:18.469', 0);
INSERT INTO `sys_menu` VALUES (510, 500, 'MENU', '接口目录', 'endpoint', 'open/endpoint/index', 'Link', 'open:endpoint:list', 1, 1, 1, 0, '2026-07-24 13:33:18.469', '2026-07-24 13:33:18.469', 0);
INSERT INTO `sys_menu` VALUES (511, 510, 'BUTTON', '接口查询', NULL, NULL, NULL, 'open:endpoint:query', 1, 1, 1, 0, '2026-07-24 13:33:18.469', '2026-07-24 13:33:18.469', 0);
INSERT INTO `sys_menu` VALUES (512, 510, 'BUTTON', '接口新增', NULL, NULL, NULL, 'open:endpoint:add', 2, 1, 1, 0, '2026-07-24 13:33:18.469', '2026-07-24 13:33:18.469', 0);
INSERT INTO `sys_menu` VALUES (513, 510, 'BUTTON', '接口修改', NULL, NULL, NULL, 'open:endpoint:edit', 3, 1, 1, 0, '2026-07-24 13:33:18.469', '2026-07-24 13:33:18.469', 0);
INSERT INTO `sys_menu` VALUES (514, 510, 'BUTTON', '接口删除', NULL, NULL, NULL, 'open:endpoint:remove', 4, 1, 1, 0, '2026-07-24 13:33:18.469', '2026-07-24 13:33:18.469', 0);
INSERT INTO `sys_menu` VALUES (520, 500, 'MENU', '客户端管理', 'client', 'open/client/index', 'Avatar', 'open:client:list', 2, 1, 1, 0, '2026-07-24 13:33:18.469', '2026-07-24 13:33:18.469', 0);
INSERT INTO `sys_menu` VALUES (521, 520, 'BUTTON', '客户端查询', NULL, NULL, NULL, 'open:client:query', 1, 1, 1, 0, '2026-07-24 13:33:18.469', '2026-07-24 13:33:18.469', 0);
INSERT INTO `sys_menu` VALUES (522, 520, 'BUTTON', '客户端新增', NULL, NULL, NULL, 'open:client:add', 2, 1, 1, 0, '2026-07-24 13:33:18.469', '2026-07-24 13:33:18.469', 0);
INSERT INTO `sys_menu` VALUES (523, 520, 'BUTTON', '客户端修改', NULL, NULL, NULL, 'open:client:edit', 3, 1, 1, 0, '2026-07-24 13:33:18.469', '2026-07-24 13:33:18.469', 0);
INSERT INTO `sys_menu` VALUES (524, 520, 'BUTTON', '客户端删除', NULL, NULL, NULL, 'open:client:remove', 4, 1, 1, 0, '2026-07-24 13:33:18.469', '2026-07-24 13:33:18.469', 0);
INSERT INTO `sys_menu` VALUES (525, 520, 'BUTTON', '重置密钥', NULL, NULL, NULL, 'open:client:resetKey', 5, 1, 1, 0, '2026-07-24 13:33:18.469', '2026-07-24 13:33:18.469', 0);

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` bigint NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容',
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOTICE' COMMENT 'NOTICE通知 / ANNOUNCE公告',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `publisher_id` bigint NULL DEFAULT NULL COMMENT '发布人用户 ID',
  `publish_time` datetime(3) NULL DEFAULT NULL COMMENT '首次启用发布时间',
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_notice_status`(`status` ASC) USING BTREE,
  INDEX `idx_sys_notice_publish_time`(`publish_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知公告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------

-- ----------------------------
-- Table structure for sys_notice_read
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice_read`;
CREATE TABLE `sys_notice_read`  (
  `id` bigint NOT NULL,
  `notice_id` bigint NOT NULL COMMENT '公告 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `read_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_notice_user`(`notice_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_notice_read_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告已读记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice_read
-- ----------------------------

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `id` bigint NOT NULL,
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作人 ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人账号',
  `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务模块',
  `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作动作',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Java 方法',
  `request_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求 URI',
  `request_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'HTTP 方法',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端 IP',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SUCCESS / FAIL',
  `error_msg` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '失败信息',
  `cost_ms` int NULL DEFAULT NULL COMMENT '耗时毫秒',
  `params` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求参数摘要',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链路 ID',
  `oper_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_oper_log_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_oper_log_module`(`module` ASC) USING BTREE,
  INDEX `idx_oper_log_status`(`status` ASC) USING BTREE,
  INDEX `idx_oper_log_time`(`oper_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (196963838079124, 1, 'admin', '用户管理', '修改', 'UserController#update', '/api/system/users/5780727731628461', 'PUT', '127.0.0.1', 'SUCCESS', NULL, 31, '{\"id\":5780727731628461,\"request\":{\"nickname\":\"test\",\"realName\":\"哈哈哈哈\",\"mobile\":null,\"email\":null,\"gender\":\"UNKNOWN\",\"avatarFileId\":459102358155861,\"deptId\":1,\"postIds\":[],\"roleIds\":[2],\"enabled\":true}}', '72d2538119f74ee4a0c9dab245069eb3', '2026-07-23 06:22:33.946');
INSERT INTO `sys_oper_log` VALUES (334535437474005, 1, 'admin', '认证', '退出登录', 'AuthController#logout', '/api/auth/logout', 'POST', '127.0.0.1', 'SUCCESS', NULL, 10, NULL, '91f5af6aa35f4cc89dd87272b3c07c21', '2026-07-23 07:30:06.234');
INSERT INTO `sys_oper_log` VALUES (695675427364999, 1, 'admin', '定时任务', '新增', 'JobController#create', '/api/system/jobs', 'POST', '127.0.0.1', 'SUCCESS', NULL, 22, '{\"request\":{\"jobName\":\"测试有参调用\",\"jobGroup\":\"omni-job\",\"invokeTarget\":\"sampleScheduledTasks.echo\",\"jobParams\":\"测试有参调用\",\"cronExpression\":\"0 * * * * ?\",\"misfirePolicy\":0,\"concurrent\":false,\"status\":true,\"remark\":null}}', 'bda6b26ce96e4aec9e5ba0d627f7c85e', '2026-07-23 02:50:08.090');
INSERT INTO `sys_oper_log` VALUES (1032828912175881, 1, 'admin', '在线用户', '强制下线', 'OnlineUserController#kick', '/api/system/online-users/29c7fb36ba594c0aacbed892ccb565b0', 'DELETE', '127.0.0.1', 'SUCCESS', NULL, 5, '{\"jti\":\"29c7fb36ba594c0aacbed892ccb565b0\"}', 'da82e000adb04e3eb9a9088b311219f3', '2026-07-23 02:55:25.213');
INSERT INTO `sys_oper_log` VALUES (2242895006155784, 1, 'admin', '认证', '退出登录', 'AuthController#logout', '/api/auth/logout', 'POST', '127.0.0.1', 'SUCCESS', NULL, 3, NULL, 'd936127f61854dac9f9225046f6e900f', '2026-07-23 09:09:20.516');
INSERT INTO `sys_oper_log` VALUES (2431150439459252, 1, 'admin', '开放客户端', '修改', 'OpenApiClientAdminController#update', '/api/open/admin/clients/595458373448572', 'PUT', '127.0.0.1', 'SUCCESS', NULL, 21, '{\"id\":595458373448572,\"request\":{\"name\":\"测试\",\"dailyLimit\":10,\"qpsLimit\":5,\"expireAt\":null,\"remark\":null,\"status\":true,\"ipList\":[],\"endpointIds\":[1]}}', 'd1b4080cc4e74f7aa85db02bf5e947d7', '2026-07-24 06:04:54.934');
INSERT INTO `sys_oper_log` VALUES (2770890372641462, 1, 'admin', '用户管理', '删除', 'UserController#remove', '/api/system/users/4', 'DELETE', '127.0.0.1', 'SUCCESS', NULL, 17, '{\"id\":4}', 'fe97c2865a884054a992b9d870315e64', '2026-07-23 06:21:51.296');
INSERT INTO `sys_oper_log` VALUES (2856808299307602, 1, 'admin', '文件管理', '上传', 'FileController#upload', '/api/system/files', 'POST', '127.0.0.1', 'SUCCESS', NULL, 22, '{\"bizType\":\"avatar\"}', 'a6434a9604924419a1eb4e6a01dac629', '2026-07-23 06:21:59.313');
INSERT INTO `sys_oper_log` VALUES (2956722851403175, 1, 'admin', '认证', '退出登录', 'AuthController#logout', '/api/auth/logout', 'POST', '127.0.0.1', 'SUCCESS', NULL, 6, NULL, '4f57a970cb404a9eb91153b1687aba1e', '2026-07-24 00:27:01.788');
INSERT INTO `sys_oper_log` VALUES (3012174471890136, 1, 'admin', '定时任务', '新增', 'JobController#create', '/api/system/jobs', 'POST', '127.0.0.1', 'SUCCESS', NULL, 65, '{\"request\":{\"jobName\":\"测试无参调用\",\"jobGroup\":\"omni-job\",\"invokeTarget\":\"sampleScheduledTasks.ping\",\"jobParams\":null,\"cronExpression\":\"0 * * * * ?\",\"misfirePolicy\":0,\"concurrent\":false,\"status\":true,\"remark\":null}}', '25d31908b68d4d91aa99e64c4c2c63da', '2026-07-23 02:49:29.637');
INSERT INTO `sys_oper_log` VALUES (3789731840766000, 1, 'admin', '文件管理', '上传', 'FileController#upload', '/api/system/files', 'POST', '127.0.0.1', 'SUCCESS', NULL, 12, '{\"bizType\":\"common\"}', '7680edc030964a42bc6ad26ed09521d2', '2026-07-23 03:45:06.612');
INSERT INTO `sys_oper_log` VALUES (3857888687379482, 1, 'admin', '菜单管理', '删除', 'MenuController#remove', '/api/system/menus/200', 'DELETE', '127.0.0.1', 'SUCCESS', NULL, 15, '{\"id\":200}', 'cfef4c48b3b84daa91964ed47ab8d778', '2026-07-23 04:59:04.324');
INSERT INTO `sys_oper_log` VALUES (3979993648026573, 1, 'admin', '定时任务', '变更状态', 'JobController#changeStatus', '/api/system/jobs/1325085100703352/status', 'PUT', '127.0.0.1', 'SUCCESS', NULL, 40, '{\"id\":1325085100703352,\"request\":{\"status\":false}}', 'da87f43e5cad4fff96be26dff2b6b62a', '2026-07-23 02:55:14.975');
INSERT INTO `sys_oper_log` VALUES (4029843753669654, 1, 'admin', '在线用户', '强制下线', 'OnlineUserController#kick', '/api/system/online-users/b011175d08ac4de49d8c1bdb116e4a9f', 'DELETE', '127.0.0.1', 'SUCCESS', NULL, 4, '{\"jti\":\"b011175d08ac4de49d8c1bdb116e4a9f\"}', '7875e51bf98c4b51a12f19909b8779c5', '2026-07-23 02:55:26.966');
INSERT INTO `sys_oper_log` VALUES (4108268788734606, 1, 'admin', '认证', '退出登录', 'AuthController#logout', '/api/auth/logout', 'POST', '127.0.0.1', 'SUCCESS', NULL, 8, NULL, 'e12fa45bda754c86bbd7948bf893e45d', '2026-07-23 09:06:40.596');
INSERT INTO `sys_oper_log` VALUES (4924639369578054, 1, 'admin', '用户管理', '新增', 'UserController#create', '/api/system/users', 'POST', '127.0.0.1', 'FAIL', '\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Cannot add or update a child row: a foreign key constraint fails (`omni-scaffolding`.`sys_user_role`, CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`))\r\n### The error may exist in file [E:\\code\\java\\omni-scaffolding\\omni-modules\\target\\classes\\mapper\\system\\SysUserQueryMapper.xml]\r\n### The error may involve defaultParameterMap\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Cannot add or update a child row: a foreign key constraint fails (`omni-scaffolding`.`sys_user_role`, CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`))\n; Cannot add or update a child row: a foreign key constraint fails (`omni-scaffolding`.`sys_user_role`, CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_use', 431, '{\"request\":{\"username\":\"11111\",\"password\":\"******\",\"nickname\":\"DDDD\",\"realName\":\"DDDD\",\"mobile\":null,\"email\":null,\"gender\":\"UNKNOWN\",\"avatarFileId\":6401387236930554,\"deptId\":1,\"postIds\":[],\"roleIds\":[1],\"enabled\":true}}', 'df10d524095d44aa947367ef46d3bccd', '2026-07-23 05:58:01.475');
INSERT INTO `sys_oper_log` VALUES (5043917998472424, 1, 'admin', '认证', '退出登录', 'AuthController#logout', '/api/auth/logout', 'POST', '127.0.0.1', 'SUCCESS', NULL, 10, NULL, 'bcf1f2cc21f9408cbd7ad64dcd272679', '2026-07-23 07:38:10.841');
INSERT INTO `sys_oper_log` VALUES (5062892944730460, 1, 'admin', '用户管理', '删除', 'UserController#remove', '/api/system/users/3', 'DELETE', '127.0.0.1', 'SUCCESS', NULL, 30, '{\"id\":3}', '76970225b60844d4af1131d30c2f3697', '2026-07-23 06:21:48.873');
INSERT INTO `sys_oper_log` VALUES (5465304200160021, 1, 'admin', '开放客户端', '新增', 'OpenApiClientAdminController#create', '/api/open/admin/clients', 'POST', '127.0.0.1', 'SUCCESS', NULL, 70, '{\"request\":{\"name\":\"测试\",\"dailyLimit\":100,\"qpsLimit\":5,\"expireAt\":null,\"remark\":null,\"status\":true,\"ipList\":[],\"endpointIds\":[1]}}', '717806f7e84a4094b64ff19a244f0b23', '2026-07-24 05:37:41.779');
INSERT INTO `sys_oper_log` VALUES (5473360244956949, 1, 'admin', '菜单管理', '删除', 'MenuController#remove', '/api/system/menus/211', 'DELETE', '127.0.0.1', 'SUCCESS', NULL, 16, '{\"id\":211}', '2a85bce32a09420b85f6b459b67b618e', '2026-07-23 04:58:59.588');
INSERT INTO `sys_oper_log` VALUES (5709858916694298, 1, 'admin', '认证', '退出登录', 'AuthController#logout', '/api/auth/logout', 'POST', '127.0.0.1', 'SUCCESS', NULL, 3, NULL, '90d1fea7d3e7434ea146e780f325379c', '2026-07-23 09:14:18.606');
INSERT INTO `sys_oper_log` VALUES (6090181652840950, 1, 'admin', '用户管理', '新增', 'UserController#create', '/api/system/users', 'POST', '127.0.0.1', 'SUCCESS', NULL, 102, '{\"request\":{\"username\":\"test\",\"password\":\"******\",\"nickname\":\"test\",\"realName\":null,\"mobile\":null,\"email\":null,\"gender\":\"UNKNOWN\",\"avatarFileId\":459102358155861,\"deptId\":1,\"postIds\":[],\"roleIds\":[2],\"enabled\":true}}', '9fc6492225e74a47a146e4663c4c5842', '2026-07-23 06:22:23.871');
INSERT INTO `sys_oper_log` VALUES (6092025919524956, 1, 'admin', '文件管理', '上传', 'FileController#upload', '/api/system/files', 'POST', '127.0.0.1', 'SUCCESS', NULL, 51, '{\"bizType\":\"avatar\"}', '427393cca10d4dc495b808ae598e2e8e', '2026-07-23 05:57:31.015');
INSERT INTO `sys_oper_log` VALUES (6484953051597552, 1, 'admin', '在线用户', '强制下线', 'OnlineUserController#kick', '/api/system/online-users/96486af0de374a25be485a3d1b7b852d', 'DELETE', '127.0.0.1', 'SUCCESS', NULL, 6, '{\"jti\":\"96486af0de374a25be485a3d1b7b852d\"}', '817a11a1183a4c40bc1de909bce18f9b', '2026-07-23 02:55:21.554');
INSERT INTO `sys_oper_log` VALUES (7487104180237288, 1, 'admin', '文件管理', '上传', 'FileController#upload', '/api/system/files', 'POST', '127.0.0.1', 'SUCCESS', NULL, 59, '{\"bizType\":\"common\"}', '313270608b8349f29ba66d0e1c0c3bee', '2026-07-23 03:43:16.153');
INSERT INTO `sys_oper_log` VALUES (7552193374545186, 1, 'admin', '菜单管理', '删除', 'MenuController#remove', '/api/system/menus/210', 'DELETE', '127.0.0.1', 'SUCCESS', NULL, 67, '{\"id\":210}', '3c7d5bd2ae1c49fe9b0f48b2302257a3', '2026-07-23 04:59:02.224');
INSERT INTO `sys_oper_log` VALUES (7956429450688544, 1, 'admin', '定时任务', '变更状态', 'JobController#changeStatus', '/api/system/jobs/1718173365857657/status', 'PUT', '127.0.0.1', 'SUCCESS', NULL, 59, '{\"id\":1718173365857657,\"request\":{\"status\":false}}', '7773b86d58d840579b59a3e051476c6d', '2026-07-23 02:55:15.571');
INSERT INTO `sys_oper_log` VALUES (8190974474259148, 1, 'admin', '开放客户端', '重置密钥', 'OpenApiClientAdminController#resetKeys', '/api/open/admin/clients/595458373448572/reset-keys', 'POST', '127.0.0.1', 'SUCCESS', NULL, 18, '{\"id\":595458373448572}', 'cc816ec17450409b9aa18747fb42cea8', '2026-07-24 06:04:24.973');
INSERT INTO `sys_oper_log` VALUES (8207723420920212, 1, 'admin', '认证', '退出登录', 'AuthController#logout', '/api/auth/logout', 'POST', '127.0.0.1', 'SUCCESS', NULL, 10, NULL, '506d5844f6254ef1829b3afb74721da5', '2026-07-24 03:01:47.517');
INSERT INTO `sys_oper_log` VALUES (8275576852929831, 1, 'admin', '用户管理', '修改', 'UserController#update', '/api/system/users/5780727731628461', 'PUT', '127.0.0.1', 'SUCCESS', NULL, 59, '{\"id\":5780727731628461,\"request\":{\"nickname\":\"test\",\"realName\":\"哈哈哈哈\",\"mobile\":null,\"email\":null,\"gender\":\"MALE\",\"avatarFileId\":459102358155861,\"deptId\":1,\"postIds\":[],\"roleIds\":[2],\"enabled\":true}}', '63ec2f0375004c36afbbb2aa2f9fc210', '2026-07-23 07:05:43.648');
INSERT INTO `sys_oper_log` VALUES (8551399685245087, 1, 'admin', '开放接口目录', '修改', 'OpenApiEndpointController#update', '/api/open/admin/endpoints/1', 'PUT', '127.0.0.1', 'SUCCESS', NULL, 18, '{\"id\":1,\"request\":{\"code\":\"open.demo.ping\",\"name\":\"开放演示 Ping\",\"httpMethod\":\"GET\",\"pathPattern\":\"/api/open/demo/ping\",\"remark\":\"脚手架演示接口\",\"status\":true}}', '05df3aa98b7e44ba8b0cc07e858b2f50', '2026-07-24 06:17:18.257');
INSERT INTO `sys_oper_log` VALUES (8585360676928128, 1, 'admin', '用户管理', '删除', 'UserController#remove', '/api/system/users/2', 'DELETE', '127.0.0.1', 'SUCCESS', NULL, 73, '{\"id\":2}', '0ccd3436923b494db23409f372bfa33c', '2026-07-23 06:21:46.841');
INSERT INTO `sys_oper_log` VALUES (8628508753370209, 1, 'admin', '在线用户', '强制下线', 'OnlineUserController#kick', '/api/system/online-users/74d6a368a72546f7ab39525732cc7e07', 'DELETE', '127.0.0.1', 'SUCCESS', NULL, 2, '{\"jti\":\"74d6a368a72546f7ab39525732cc7e07\"}', 'c85e90dee8184798946fd148198ab8d5', '2026-07-23 02:55:23.455');
INSERT INTO `sys_oper_log` VALUES (8629541475821578, 1, 'admin', '开放接口目录', '修改', 'OpenApiEndpointController#update', '/api/open/admin/endpoints/1', 'PUT', '127.0.0.1', 'SUCCESS', NULL, 63, '{\"id\":1,\"request\":{\"code\":\"open.demo.ping\",\"name\":\"开放演示 Ping\",\"httpMethod\":\"GET\",\"pathPattern\":\"/api/open/demo/ping\",\"remark\":\"脚手架演示接口\",\"status\":false}}', '2a2e7359621b4fbd97dc19d16d307c49', '2026-07-24 06:17:10.533');
INSERT INTO `sys_oper_log` VALUES (8877465340011695, 1, 'admin', '定时任务', '立即执行', 'JobController#runOnce', '/api/system/jobs/1325085100703352/run', 'POST', '127.0.0.1', 'SUCCESS', NULL, 197, '{\"id\":1325085100703352}', '47dc7f2e822e48baa09c8fa055713832', '2026-07-23 02:49:36.674');
INSERT INTO `sys_oper_log` VALUES (8992884064797923, 1, 'admin', '菜单管理', '删除', 'MenuController#remove', '/api/system/menus/212', 'DELETE', '127.0.0.1', 'SUCCESS', NULL, 54, '{\"id\":212}', '9dbb17f22aa3449d9a64ecd900547a12', '2026-07-23 04:58:57.531');

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `id` bigint NOT NULL,
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位编码',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位名称',
  `sort` int NOT NULL DEFAULT 0,
  `status` tinyint(1) NOT NULL DEFAULT 1,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_post_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '岗位' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'ENGINEER', '工程师', 1, 1, 0, '2026-07-23 09:05:09.049', '2026-07-23 09:05:09.049', 0);
INSERT INTO `sys_post` VALUES (2, 'MANAGER', '经理', 2, 1, 0, '2026-07-23 09:05:09.049', '2026-07-23 09:05:09.049', 0);
INSERT INTO `sys_post` VALUES (3, 'SALES', '销售专员', 3, 1, 0, '2026-07-23 09:05:09.049', '2026-07-23 09:05:09.049', 0);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码（唯一，如 ADMIN）',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `data_scope` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SELF' COMMENT 'ALL/SELF/DEPT/DEPT_AND_CHILD',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0停用',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 非0已删',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_role_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'ADMIN', 'Administrator', 'ALL', 1, 0, '2026-07-23 09:05:08.005', '2026-07-23 09:05:08.005', 0);
INSERT INTO `sys_role` VALUES (2, 'RD_MANAGER', '研发经理', 'DEPT_AND_CHILD', 1, 0, '2026-07-23 09:05:08.974', '2026-07-23 09:05:08.974', 0);
INSERT INTO `sys_role` VALUES (3, 'SALES', '销售员', 'SELF', 1, 0, '2026-07-23 09:05:08.974', '2026-07-23 09:05:08.974', 0);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE,
  INDEX `fk_role_menu_menu`(`menu_id` ASC) USING BTREE,
  CONSTRAINT `fk_role_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_role_menu_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1);
INSERT INTO `sys_role_menu` VALUES (2, 1);
INSERT INTO `sys_role_menu` VALUES (3, 1);
INSERT INTO `sys_role_menu` VALUES (1, 10);
INSERT INTO `sys_role_menu` VALUES (2, 10);
INSERT INTO `sys_role_menu` VALUES (3, 10);
INSERT INTO `sys_role_menu` VALUES (1, 11);
INSERT INTO `sys_role_menu` VALUES (2, 11);
INSERT INTO `sys_role_menu` VALUES (3, 11);
INSERT INTO `sys_role_menu` VALUES (1, 12);
INSERT INTO `sys_role_menu` VALUES (2, 12);
INSERT INTO `sys_role_menu` VALUES (1, 13);
INSERT INTO `sys_role_menu` VALUES (2, 13);
INSERT INTO `sys_role_menu` VALUES (1, 14);
INSERT INTO `sys_role_menu` VALUES (1, 15);
INSERT INTO `sys_role_menu` VALUES (1, 16);
INSERT INTO `sys_role_menu` VALUES (1, 20);
INSERT INTO `sys_role_menu` VALUES (1, 21);
INSERT INTO `sys_role_menu` VALUES (1, 22);
INSERT INTO `sys_role_menu` VALUES (1, 23);
INSERT INTO `sys_role_menu` VALUES (1, 24);
INSERT INTO `sys_role_menu` VALUES (1, 25);
INSERT INTO `sys_role_menu` VALUES (1, 30);
INSERT INTO `sys_role_menu` VALUES (1, 31);
INSERT INTO `sys_role_menu` VALUES (1, 32);
INSERT INTO `sys_role_menu` VALUES (1, 33);
INSERT INTO `sys_role_menu` VALUES (1, 34);
INSERT INTO `sys_role_menu` VALUES (1, 35);
INSERT INTO `sys_role_menu` VALUES (1, 40);
INSERT INTO `sys_role_menu` VALUES (1, 41);
INSERT INTO `sys_role_menu` VALUES (1, 42);
INSERT INTO `sys_role_menu` VALUES (1, 43);
INSERT INTO `sys_role_menu` VALUES (1, 44);
INSERT INTO `sys_role_menu` VALUES (1, 50);
INSERT INTO `sys_role_menu` VALUES (1, 51);
INSERT INTO `sys_role_menu` VALUES (1, 52);
INSERT INTO `sys_role_menu` VALUES (1, 53);
INSERT INTO `sys_role_menu` VALUES (1, 54);
INSERT INTO `sys_role_menu` VALUES (1, 55);
INSERT INTO `sys_role_menu` VALUES (1, 60);
INSERT INTO `sys_role_menu` VALUES (1, 61);
INSERT INTO `sys_role_menu` VALUES (1, 62);
INSERT INTO `sys_role_menu` VALUES (1, 63);
INSERT INTO `sys_role_menu` VALUES (1, 64);
INSERT INTO `sys_role_menu` VALUES (1, 65);
INSERT INTO `sys_role_menu` VALUES (1, 70);
INSERT INTO `sys_role_menu` VALUES (1, 71);
INSERT INTO `sys_role_menu` VALUES (1, 72);
INSERT INTO `sys_role_menu` VALUES (1, 73);
INSERT INTO `sys_role_menu` VALUES (1, 74);
INSERT INTO `sys_role_menu` VALUES (1, 75);
INSERT INTO `sys_role_menu` VALUES (1, 76);
INSERT INTO `sys_role_menu` VALUES (1, 80);
INSERT INTO `sys_role_menu` VALUES (1, 81);
INSERT INTO `sys_role_menu` VALUES (1, 82);
INSERT INTO `sys_role_menu` VALUES (1, 83);
INSERT INTO `sys_role_menu` VALUES (1, 84);
INSERT INTO `sys_role_menu` VALUES (1, 85);
INSERT INTO `sys_role_menu` VALUES (1, 86);
INSERT INTO `sys_role_menu` VALUES (1, 87);
INSERT INTO `sys_role_menu` VALUES (1, 88);
INSERT INTO `sys_role_menu` VALUES (1, 89);
INSERT INTO `sys_role_menu` VALUES (1, 90);
INSERT INTO `sys_role_menu` VALUES (1, 91);
INSERT INTO `sys_role_menu` VALUES (1, 92);
INSERT INTO `sys_role_menu` VALUES (1, 93);
INSERT INTO `sys_role_menu` VALUES (1, 94);
INSERT INTO `sys_role_menu` VALUES (1, 95);
INSERT INTO `sys_role_menu` VALUES (1, 96);
INSERT INTO `sys_role_menu` VALUES (1, 97);
INSERT INTO `sys_role_menu` VALUES (1, 98);
INSERT INTO `sys_role_menu` VALUES (1, 99);
INSERT INTO `sys_role_menu` VALUES (1, 100);
INSERT INTO `sys_role_menu` VALUES (1, 110);
INSERT INTO `sys_role_menu` VALUES (1, 111);
INSERT INTO `sys_role_menu` VALUES (1, 112);
INSERT INTO `sys_role_menu` VALUES (1, 113);
INSERT INTO `sys_role_menu` VALUES (1, 130);
INSERT INTO `sys_role_menu` VALUES (1, 131);
INSERT INTO `sys_role_menu` VALUES (1, 132);
INSERT INTO `sys_role_menu` VALUES (1, 133);
INSERT INTO `sys_role_menu` VALUES (1, 140);
INSERT INTO `sys_role_menu` VALUES (1, 141);
INSERT INTO `sys_role_menu` VALUES (1, 150);
INSERT INTO `sys_role_menu` VALUES (1, 151);
INSERT INTO `sys_role_menu` VALUES (1, 160);
INSERT INTO `sys_role_menu` VALUES (1, 161);
INSERT INTO `sys_role_menu` VALUES (1, 162);
INSERT INTO `sys_role_menu` VALUES (1, 163);
INSERT INTO `sys_role_menu` VALUES (1, 200);
INSERT INTO `sys_role_menu` VALUES (1, 210);
INSERT INTO `sys_role_menu` VALUES (1, 211);
INSERT INTO `sys_role_menu` VALUES (1, 212);
INSERT INTO `sys_role_menu` VALUES (1, 300);
INSERT INTO `sys_role_menu` VALUES (1, 310);
INSERT INTO `sys_role_menu` VALUES (1, 311);
INSERT INTO `sys_role_menu` VALUES (1, 312);
INSERT INTO `sys_role_menu` VALUES (1, 320);
INSERT INTO `sys_role_menu` VALUES (1, 321);
INSERT INTO `sys_role_menu` VALUES (1, 322);
INSERT INTO `sys_role_menu` VALUES (1, 400);
INSERT INTO `sys_role_menu` VALUES (1, 410);
INSERT INTO `sys_role_menu` VALUES (1, 411);
INSERT INTO `sys_role_menu` VALUES (1, 412);
INSERT INTO `sys_role_menu` VALUES (1, 413);
INSERT INTO `sys_role_menu` VALUES (1, 500);
INSERT INTO `sys_role_menu` VALUES (1, 510);
INSERT INTO `sys_role_menu` VALUES (1, 511);
INSERT INTO `sys_role_menu` VALUES (1, 512);
INSERT INTO `sys_role_menu` VALUES (1, 513);
INSERT INTO `sys_role_menu` VALUES (1, 514);
INSERT INTO `sys_role_menu` VALUES (1, 520);
INSERT INTO `sys_role_menu` VALUES (1, 521);
INSERT INTO `sys_role_menu` VALUES (1, 522);
INSERT INTO `sys_role_menu` VALUES (1, 523);
INSERT INTO `sys_role_menu` VALUES (1, 524);
INSERT INTO `sys_role_menu` VALUES (1, 525);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录用户名（唯一）',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希（BCrypt）',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示昵称',
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `mobile` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `gender` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNKNOWN' COMMENT 'UNKNOWN/MALE/FEMALE',
  `avatar_file_id` bigint NULL DEFAULT NULL COMMENT '头像文件 ID，关联 sys_file.id',
  `dept_id` bigint NOT NULL COMMENT '所属部门',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1是 0否',
  `must_change_pwd` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否强制改密：1是 0否',
  `pwd_changed_at` datetime(3) NULL DEFAULT NULL COMMENT '最近修改密码时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 非0已删',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_sys_user_mobile`(`mobile` ASC) USING BTREE,
  UNIQUE INDEX `uk_sys_user_email`(`email` ASC) USING BTREE,
  INDEX `fk_sys_user_dept`(`dept_id` ASC) USING BTREE,
  CONSTRAINT `fk_sys_user_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表（JPA 写路径主表）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$Ui4rR7gf0t9lootpk0TMb.FYd.C049Ln8jb0dkrMs9M6BPqLPyazC', 'Administrator', '系统管理员', '13800000001', 'admin@omni.local', 'MALE', NULL, 1, 1, 0, NULL, 0, '2026-07-23 09:05:08.002', '2026-07-23 01:05:14.817', 1);
INSERT INTO `sys_user` VALUES (2, 'rd_mgr', '$2a$10$z0PjJxEsXea7iqNrqIHFHuWDJ8NVOrn86Nt0gE5pxLLhX6xPOg6AW', '????', '????', '13800000002', 'rd_mgr@omni.local', 'MALE', NULL, 2, 1, 0, NULL, 1, '2026-07-23 09:05:08.979', '2026-07-23 06:21:46.798', 2);
INSERT INTO `sys_user` VALUES (3, 'sales1', '$2a$10$vloj5G2Q0Lu6YkKAOb8dCusQKsz.vuKyCsl4up.TR2BG4.8IGdYWC', '???', '???', '13800000003', 'sales1@omni.local', 'FEMALE', NULL, 3, 1, 0, NULL, 1, '2026-07-23 09:05:08.979', '2026-07-23 06:21:48.846', 2);
INSERT INTO `sys_user` VALUES (4, 'rd_dev', '$2a$10$/bVcyWfsQX.F/JmzCwQQPO.0I0HqhHyNotvEfEEoTl6YXutHUCxwS', '????', '????', '13800000004', 'rd_dev@omni.local', 'MALE', NULL, 2, 1, 0, NULL, 1, '2026-07-23 09:05:08.979', '2026-07-23 06:21:51.283', 2);
INSERT INTO `sys_user` VALUES (5780727731628461, 'test', '$2a$10$JdZ5X4YdTfqAttSlLB/DKeUCf45YvQBUzueQXbzF3JOFFMCHgVQ62', 'test', '哈哈哈哈', NULL, NULL, 'MALE', 459102358155861, 1, 1, 0, NULL, 0, '2026-07-23 06:22:23.842', '2026-07-23 07:05:43.605', 2);

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE,
  INDEX `fk_user_post_post`(`post_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_post_post` FOREIGN KEY (`post_id`) REFERENCES `sys_post` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_post_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1, 2);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
  INDEX `fk_user_role_role`(`role_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户-角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (5780727731628461, 2);

SET FOREIGN_KEY_CHECKS = 1;
