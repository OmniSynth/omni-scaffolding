package com.omni.scaffolding.modules.ops.dto;

import lombok.Data;

/**
 * MySQL 进程列表项。
 */
@Data
public class MysqlProcessView {

    /**
     * 连接 ID。
     */
    private Long id;

    /**
     * 用户名。
     */
    private String user;

    /**
     * 客户端主机。
     */
    private String host;

    /**
     * 当前库。
     */
    private String db;

    /**
     * 命令类型。
     */
    private String command;

    /**
     * 执行时长（秒）。
     */
    private Long time;

    /**
     * 状态。
     */
    private String state;

    /**
     * 当前 SQL。
     */
    private String info;
}
