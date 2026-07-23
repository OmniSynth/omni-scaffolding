package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.modules.system.dto.PostSaveRequest;
import com.omni.scaffolding.modules.system.dto.PostView;
import com.omni.scaffolding.modules.system.dto.excel.PostExportRow;
import com.omni.scaffolding.modules.system.entity.SysPost;
import com.omni.scaffolding.modules.system.mapper.SysPostQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 岗位领域服务。
 *
 * <p>维护岗位编码、名称、排序与启停；用户可通过 {@code sys_user_post} 绑定多个岗位。
 * 仍有用户绑定时不允许删除。
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private static final long EXPORT_LIMIT = 10_000L;

    private final SysPostRepository postRepository;
    private final SysPostQueryMapper postQueryMapper;

    /**
     * 岗位分页列表；{@code keyword} 匹配编码或名称，可为空。
     *
     * @param keyword 可选，匹配编码 / 名称
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<PostView> list(String keyword, Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        long total = postQueryMapper.countPosts(keyword);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, postQueryMapper.listPosts(keyword, pq.getSize(), pq.getOffset()));
    }

    /**
     * 导出岗位列表（支持与列表相同的关键字过滤）。
     *
     * @param keyword 可选，匹配编码 / 名称
     * @return 导出行列表
     */
    @Transactional(readOnly = true)
    public List<PostExportRow> exportPosts(String keyword) {
        return postQueryMapper.listPosts(keyword, EXPORT_LIMIT, 0).stream()
                .map(this::toExportRow)
                .toList();
    }

    /**
     * 岗位读模型转导出行（状态转中文）。
     *
     * @param view 岗位读模型
     * @return 导出行
     */
    private PostExportRow toExportRow(PostView view) {
        PostExportRow row = new PostExportRow();
        row.setId(view.getId());
        row.setCode(view.getCode());
        row.setName(view.getName());
        row.setSort(view.getSort());
        row.setStatus(Boolean.TRUE.equals(view.getStatus()) ? "启用" : "停用");
        return row;
    }

    /**
     * 岗位详情；不存在则 404。
     *
     * @param postId 岗位主键
     * @return 读模型
     */
    @Transactional(readOnly = true)
    public PostView detail(Long postId) {
        PostView view = postQueryMapper.findById(postId);
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "岗位不存在");
        }
        return view;
    }

    /**
     * 创建岗位；编码全局唯一（未删除范围内）。
     *
     * @param request 保存请求
     * @return 新建读模型
     */
    @Transactional
    public PostView create(PostSaveRequest request) {
        if (postRepository.existsByCodeAndDeleted(request.getCode().trim(), 0)) {
            throw new BusinessException(ErrorCode.CONFLICT, "岗位编码已存在");
        }
        SysPost post = new SysPost();
        post.setId(IdGenerator.nextId());
        apply(post, request);
        post.setDeleted(0);
        postRepository.save(post);
        return detail(post.getId());
    }

    /**
     * 更新岗位；编码冲突时拒绝。
     *
     * @param postId  岗位主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Transactional
    public PostView update(Long postId, PostSaveRequest request) {
        SysPost post = postRepository.findByIdAndDeleted(postId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "岗位不存在"));
        postRepository.findByCodeAndDeleted(request.getCode().trim(), 0).ifPresent(other -> {
            if (!other.getId().equals(postId)) {
                throw new BusinessException(ErrorCode.CONFLICT, "岗位编码已存在");
            }
        });
        apply(post, request);
        postRepository.save(post);
        return detail(postId);
    }

    /**
     * 逻辑删除岗位；已分配给用户时拒绝。
     *
     * @param postId 岗位主键
     */
    @Transactional
    public void remove(Long postId) {
        SysPost post = postRepository.findByIdAndDeleted(postId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "岗位不存在"));
        if (postQueryMapper.countUsersByPostId(postId) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "岗位已分配给用户，无法删除");
        }
        post.setDeleted(1);
        postRepository.save(post);
    }

    /**
     * 将请求字段应用到实体。
     */
    private void apply(SysPost post, PostSaveRequest request) {
        post.setCode(request.getCode().trim());
        post.setName(request.getName().trim());
        post.setSort(request.getSort());
        post.setStatus(request.getStatus());
    }
}
