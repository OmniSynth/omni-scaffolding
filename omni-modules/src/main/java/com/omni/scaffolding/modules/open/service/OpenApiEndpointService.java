package com.omni.scaffolding.modules.open.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.modules.open.dto.endpoint.OpenEndpointSaveRequest;
import com.omni.scaffolding.modules.open.dto.endpoint.OpenEndpointView;
import com.omni.scaffolding.modules.open.entity.OpenApiEndpoint;
import com.omni.scaffolding.modules.open.mapper.OpenApiClientQueryMapper;
import com.omni.scaffolding.modules.open.mapper.OpenApiEndpointQueryMapper;
import com.omni.scaffolding.modules.open.repository.OpenApiEndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * 开放接口目录服务。
 *
 * <p>维护可供第三方调用的 method + path_pattern；删除前须无客户端绑定。
 */
@Service
@RequiredArgsConstructor
public class OpenApiEndpointService {

    private final OpenApiEndpointRepository endpointRepository;
    private final OpenApiEndpointQueryMapper endpointQueryMapper;
    private final OpenApiClientQueryMapper clientQueryMapper;

    /**
     * 分页查询开放接口。
     *
     * @param keyword 可选关键字
     * @param status  可选启停
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<OpenEndpointView> list(String keyword, Boolean status, Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        long total = endpointQueryMapper.countEndpoints(keyword, status);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, endpointQueryMapper.listEndpoints(keyword, status, pq.getSize(), pq.getOffset()));
    }

    /**
     * 全部启用中的接口（下拉）。
     *
     * @return 启用列表
     */
    @Transactional(readOnly = true)
    public List<OpenEndpointView> listEnabled() {
        return endpointQueryMapper.listAllEnabled();
    }

    /**
     * 接口详情；不存在则 404。
     *
     * @param id 主键
     * @return 读模型
     */
    @Transactional(readOnly = true)
    public OpenEndpointView detail(Long id) {
        OpenEndpointView view = endpointQueryMapper.findById(id);
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "开放接口不存在");
        }
        return view;
    }

    /**
     * 创建开放接口；编码唯一。
     *
     * @param request 保存请求
     * @return 新建读模型
     */
    @Transactional
    public OpenEndpointView create(OpenEndpointSaveRequest request) {
        String code = request.getCode().trim();
        if (endpointRepository.existsByCodeAndDeleted(code, 0)) {
            throw new BusinessException(ErrorCode.CONFLICT, "接口编码已存在");
        }
        OpenApiEndpoint entity = new OpenApiEndpoint();
        entity.setId(IdGenerator.nextId());
        apply(entity, request);
        entity.setDeleted(0);
        // 须 flush：随后 MyBatis 读详情
        endpointRepository.saveAndFlush(entity);
        return detail(entity.getId());
    }

    /**
     * 更新开放接口；编码冲突时拒绝。
     *
     * @param id      主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Transactional
    public OpenEndpointView update(Long id, OpenEndpointSaveRequest request) {
        OpenApiEndpoint entity = endpointRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "开放接口不存在"));
        String code = request.getCode().trim();
        if (endpointRepository.existsByCodeAndDeletedAndIdNot(code, 0, id)) {
            throw new BusinessException(ErrorCode.CONFLICT, "接口编码已存在");
        }
        apply(entity, request);
        endpointRepository.saveAndFlush(entity);
        return detail(id);
    }

    /**
     * 逻辑删除；仍有客户端绑定时拒绝。
     *
     * @param id 主键
     */
    @Transactional
    public void remove(Long id) {
        OpenApiEndpoint entity = endpointRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "开放接口不存在"));
        if (clientQueryMapper.countBindingsByEndpointId(id) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "仍有客户端绑定该接口，无法删除");
        }
        entity.setDeleted(1);
        endpointRepository.save(entity);
    }

    /**
     * 将请求字段写到实体（HTTP 方法统一大写）。
     */
    private static void apply(OpenApiEndpoint entity, OpenEndpointSaveRequest request) {
        entity.setCode(request.getCode().trim());
        entity.setName(request.getName().trim());
        entity.setHttpMethod(request.getHttpMethod().trim().toUpperCase(Locale.ROOT));
        entity.setPathPattern(request.getPathPattern().trim());
        entity.setRemark(request.getRemark());
        entity.setStatus(Boolean.TRUE.equals(request.getStatus()));
    }
}
