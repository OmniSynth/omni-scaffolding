package com.omni.scaffolding.modules.system.support;

import com.omni.scaffolding.modules.system.dto.DeptView;
import com.omni.scaffolding.modules.system.dto.MenuTreeNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 扁平列表转树形结构（菜单、部门等）。
 *
 * <p>按 {@code sort}、{@code id} 递归排序；孤儿节点（父 ID 不存在）提升为根节点。
 */
public final class TreeBuilder {

    /**
     * 工具类，禁止实例化。
     */
    private TreeBuilder() {
    }

    /**
     * 将扁平菜单列表构建为树。
     *
     * @param flat 扁平节点列表
     * @return 根节点列表（含嵌套 children）
     */
    public static List<MenuTreeNode> buildMenuTree(List<MenuTreeNode> flat) {
        return build(flat, MenuTreeNode::getId, MenuTreeNode::getParentId, MenuTreeNode::getChildren, MenuTreeNode::setChildren,
                Comparator.comparing(MenuTreeNode::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(MenuTreeNode::getId));
    }

    /**
     * 将扁平部门列表构建为树。
     *
     * @param flat 扁平节点列表
     * @return 根节点列表（含嵌套 children）
     */
    public static List<DeptView> buildDeptTree(List<DeptView> flat) {
        return build(flat, DeptView::getId, DeptView::getParentId, DeptView::getChildren, DeptView::setChildren,
                Comparator.comparing(DeptView::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(DeptView::getId));
    }

    /**
     * 通用扁平列表转树：按父 ID 挂载子节点，孤儿节点提升为根。
     *
     * @param flat           扁平节点列表
     * @param idGetter       取节点 ID
     * @param parentGetter   取父节点 ID
     * @param childrenGetter 取子节点列表
     * @param childrenSetter 设置子节点列表
     * @param comparator     排序比较器
     * @param <T>            节点类型
     * @return 根节点列表
     */
    private static <T> List<T> build(List<T> flat,
                                     Function<T, Long> idGetter,
                                     Function<T, Long> parentGetter,
                                     Function<T, List<T>> childrenGetter,
                                     BiConsumer<T, List<T>> childrenSetter,
                                     Comparator<T> comparator) {
        Map<Long, T> map = new LinkedHashMap<>();
        for (T node : flat) {
            childrenSetter.accept(node, new ArrayList<>());
            map.put(idGetter.apply(node), node);
        }
        List<T> roots = new ArrayList<>();
        for (T node : flat) {
            Long parentId = parentGetter.apply(node);
            if (parentId == null || parentId == 0L || !map.containsKey(parentId)) {
                roots.add(node);
            } else {
                childrenGetter.apply(map.get(parentId)).add(node);
            }
        }
        sortRecursive(roots, childrenGetter, comparator);
        return roots;
    }

    /**
     * 递归对树节点及其子节点排序。
     *
     * @param nodes          当前层节点
     * @param childrenGetter 取子节点列表
     * @param comparator     排序比较器
     * @param <T>            节点类型
     */
    private static <T> void sortRecursive(List<T> nodes, Function<T, List<T>> childrenGetter, Comparator<T> comparator) {
        nodes.sort(comparator);
        for (T node : nodes) {
            List<T> children = childrenGetter.apply(node);
            if (children != null && !children.isEmpty()) {
                sortRecursive(children, childrenGetter, comparator);
            }
        }
    }

    /**
     * 按允许 ID 集合裁剪部门树（保留祖先链路上的节点）。
     *
     * @param tree       完整部门树
     * @param allowedIds 当前用户可见的部门 ID
     * @return 裁剪后的树
     */
    public static List<DeptView> filterDeptTreeByIds(List<DeptView> tree, java.util.Set<Long> allowedIds) {
        List<DeptView> result = new ArrayList<>();
        for (DeptView node : tree) {
            DeptView copy = copyDept(node, allowedIds);
            if (copy != null) {
                result.add(copy);
            }
        }
        return result;
    }

    /**
     * 递归复制部门子树，仅保留自身或子孙在允许集合内的节点。
     *
     * @param node       当前节点
     * @param allowedIds 可见部门 ID 集合
     * @return 裁剪后的副本，整棵子树均不可见时 {@code null}
     */
    private static DeptView copyDept(DeptView node, java.util.Set<Long> allowedIds) {
        List<DeptView> children = new ArrayList<>();
        for (DeptView child : node.getChildren()) {
            DeptView c = copyDept(child, allowedIds);
            if (c != null) {
                children.add(c);
            }
        }
        boolean selfAllowed = allowedIds.contains(node.getId());
        if (!selfAllowed && children.isEmpty()) {
            return null;
        }
        DeptView copy = new DeptView();
        copy.setId(node.getId());
        copy.setParentId(node.getParentId());
        copy.setName(node.getName());
        copy.setSort(node.getSort());
        copy.setAncestors(node.getAncestors());
        copy.setStatus(node.getStatus());
        copy.setChildren(children);
        return copy;
    }

    /**
     * 忽略大小写比较两个字符串（{@code null} 安全）。
     *
     * @param a 字符串 a
     * @param b 字符串 b
     * @return 相等返回 {@code true}
     */
    public static boolean equalsIgnoreCase(String a, String b) {
        return Objects.equals(a == null ? null : a.toUpperCase(), b == null ? null : b.toUpperCase());
    }
}
