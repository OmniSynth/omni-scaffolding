package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.modules.system.dto.dept.DeptSaveRequest;
import com.omni.scaffolding.modules.system.dto.dept.DeptView;
import com.omni.scaffolding.modules.system.dto.excel.DeptExportRow;
import com.omni.scaffolding.modules.system.entity.SysDept;
import com.omni.scaffolding.modules.system.mapper.SysDeptQueryMapper;
import com.omni.scaffolding.modules.system.mapper.SysUserQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysDeptRepository;
import com.omni.scaffolding.modules.system.support.TreeBuilder;
import com.omni.scaffolding.security.SecurityUtils;
import com.omni.scaffolding.security.datascope.DataScopeQuery;
import com.omni.scaffolding.security.datascope.DataScopeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 部门领域服务。
 *
 * <p>维护部门树（{@code parentId}/{@code ancestors}）；列表查询按当前用户数据范围裁剪可见节点。
 * 根部门（id=1）不可删除；有子部门或下属用户时不可删除。
 */
@Service
@RequiredArgsConstructor
public class DeptService {

    private final SysDeptRepository deptRepository;
    private final SysDeptQueryMapper deptQueryMapper;
    private final SysUserQueryMapper userQueryMapper;
    private final DataScopeResolver dataScopeResolver;

    /**
     * 部门树；非 ALL 数据范围时仅保留可见部门及其必要祖先以维持树形结构。
     *
     * @return 树形读模型列表
     */
    @Transactional(readOnly = true)
    public List<DeptView> tree() {
        List<DeptView> all = deptQueryMapper.listAll();
        List<DeptView> tree = TreeBuilder.buildDeptTree(all);
        DataScopeQuery ds = dataScopeResolver.resolve();
        if (ds.isAll()) {
            return tree;
        }
        Set<Long> allowed = new HashSet<>();
        if (DataScopeType.SELF.name().equals(ds.getType()) || DataScopeType.DEPT.name().equals(ds.getType())) {
            if (SecurityUtils.requireDeptId() != null) {
                allowed.add(SecurityUtils.requireDeptId());
            }
        } else {
            allowed.addAll(ds.getDeptIds());
        }
        return TreeBuilder.filterDeptTreeByIds(tree, allowed);
    }

    /**
     * 导出当前可见部门树（扁平化）。
     *
     * @return 导出行列表
     */
    @Transactional(readOnly = true)
    public List<DeptExportRow> exportDepts() {
        List<DeptExportRow> rows = new ArrayList<>();
        flattenDeptTree(tree(), "根节点", rows);
        return rows;
    }

    /**
     * 深度优先遍历部门树，扁平化为导出行列表。
     *
     * @param nodes      当前层节点
     * @param parentName 上级部门名称（根层为「根节点」）
     * @param rows       导出行累积列表
     */
    private void flattenDeptTree(List<DeptView> nodes, String parentName, List<DeptExportRow> rows) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        for (DeptView node : nodes) {
            DeptExportRow row = new DeptExportRow();
            row.setId(node.getId());
            row.setParentName(parentName);
            row.setName(node.getName());
            row.setSort(node.getSort());
            row.setUserCount(node.getUserCount() == null ? 0L : node.getUserCount());
            row.setStatus(Boolean.TRUE.equals(node.getStatus()) ? "正常" : "停用");
            rows.add(row);
            flattenDeptTree(node.getChildren(), node.getName(), rows);
        }
    }

    /**
     * 新增部门；自动根据父节点计算 {@code ancestors} 路径。
     *
     * @param request 保存请求
     * @return 新建读模型
     */
    @Transactional
    public DeptView create(DeptSaveRequest request) {
        String ancestors = resolveAncestors(request.getParentId());
        SysDept dept = new SysDept();
        dept.setId(IdGenerator.nextId());
        dept.setParentId(request.getParentId());
        dept.setName(request.getName().trim());
        dept.setSort(request.getSort());
        dept.setAncestors(ancestors);
        dept.setStatus(request.getStatus());
        dept.setDeleted(0);
        deptRepository.save(dept);
        return findView(dept.getId());
    }

    /**
     * 更新部门；禁止把父节点设为自己或自己的子孙。
     *
     * @param deptId  部门主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Transactional
    public DeptView update(Long deptId, DeptSaveRequest request) {
        SysDept dept = deptRepository.findByIdAndDeleted(deptId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "部门不存在"));
        if (deptId.equals(request.getParentId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上级部门不能是自己");
        }
        String ancestors = resolveAncestors(request.getParentId());
        if (ancestors.contains("," + deptId + ",") || ancestors.endsWith("," + deptId) || ancestors.equals(String.valueOf(deptId))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上级部门不能是自己的下级");
        }
        dept.setParentId(request.getParentId());
        dept.setName(request.getName().trim());
        dept.setSort(request.getSort());
        dept.setAncestors(ancestors);
        dept.setStatus(request.getStatus());
        deptRepository.save(dept);
        return findView(deptId);
    }

    /**
     * 逻辑删除部门；根部门、有子节点、有下属用户时拒绝。
     *
     * @param deptId 部门主键
     */
    @Transactional
    public void remove(Long deptId) {
        if (deptId.equals(1L)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能删除根部门");
        }
        SysDept dept = deptRepository.findByIdAndDeleted(deptId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "部门不存在"));
        if (deptRepository.countByParentIdAndDeleted(deptId, 0) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "请先删除子部门");
        }
        if (userQueryMapper.countByDeptId(deptId) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "部门下仍有用户，无法删除");
        }
        dept.setDeleted(1);
        deptRepository.save(dept);
    }

    /**
     * 根据父部门计算祖先路径：根为 {@code "0"}，否则 {@code parent.ancestors + "," + parent.id}。
     */
    private String resolveAncestors(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return "0";
        }
        SysDept parent = deptRepository.findByIdAndDeleted(parentId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "上级部门不存在"));
        return parent.getAncestors() + "," + parent.getId();
    }

    /**
     * 从读模型列表中取回刚写入的部门（含用户数等聚合字段）。
     */
    private DeptView findView(Long id) {
        return deptQueryMapper.listAll().stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "部门读取失败"));
    }
}
