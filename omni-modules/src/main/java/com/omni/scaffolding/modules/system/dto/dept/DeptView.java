package com.omni.scaffolding.modules.system.dto.dept;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门树节点读模型。
 */
@Data
public class DeptView {

    /**
     * 部门 ID。
     */
    private Long id;

    /**
     * 父部门 ID，根为 0。
     */
    private Long parentId;

    /**
     * 部门名称。
     */
    private String name;

    /**
     * 排序。
     */
    private Integer sort;

    /**
     * 祖先路径，如 0,1。
     */
    private String ancestors;

    /**
     * 是否启用。
     */
    private Boolean status;

    /**
     * 直属该部门的用户数（未删除用户）。
     */
    private Long userCount;

    /**
     * 子部门。
     */
    private List<DeptView> children = new ArrayList<>();
}
