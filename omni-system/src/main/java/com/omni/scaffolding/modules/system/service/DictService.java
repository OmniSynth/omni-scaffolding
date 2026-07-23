package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.cache.CacheNames;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.modules.system.dto.DictDataSaveRequest;
import com.omni.scaffolding.modules.system.dto.DictDataView;
import com.omni.scaffolding.modules.system.dto.DictOptionView;
import com.omni.scaffolding.modules.system.dto.DictTypeSaveRequest;
import com.omni.scaffolding.modules.system.dto.DictTypeView;
import com.omni.scaffolding.modules.system.dto.excel.DictDataExportRow;
import com.omni.scaffolding.modules.system.dto.excel.DictTypeExportRow;
import com.omni.scaffolding.modules.system.entity.SysDictData;
import com.omni.scaffolding.modules.system.entity.SysDictType;
import com.omni.scaffolding.modules.system.mapper.SysDictDataQueryMapper;
import com.omni.scaffolding.modules.system.mapper.SysDictTypeQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysDictDataRepository;
import com.omni.scaffolding.modules.system.repository.SysDictTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据字典领域服务。
 *
 * <p>维护字典类型与字典数据两项；业务表单通过 {@link #listOptions(String)} 按类型编码拉取启用项（带缓存）。
 * 类型 / 数据写操作会失效对应 {@code dictOptions} 缓存。
 */
@Service
@RequiredArgsConstructor
public class DictService {

    /** 导出最大行数，避免一次拉取过大。 */
    private static final long EXPORT_LIMIT = 10_000L;

    private final SysDictTypeRepository dictTypeRepository;
    private final SysDictDataRepository dictDataRepository;
    private final SysDictTypeQueryMapper dictTypeQueryMapper;
    private final SysDictDataQueryMapper dictDataQueryMapper;

    // -------------------------------------------------------------------------
    // 类型
    // -------------------------------------------------------------------------

    /**
     * 分页查询字典类型；{@code keyword} 匹配编码或名称，可为空。
     *
     * @param keyword 可选，匹配编码 / 名称
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<DictTypeView> listTypes(String keyword, Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        long total = dictTypeQueryMapper.countTypes(keyword);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, dictTypeQueryMapper.listTypes(keyword, pq.getSize(), pq.getOffset()));
    }

    /**
     * 字典类型详情（含下属数据条数）；不存在则 404。
     *
     * @param typeId 类型主键
     * @return 读模型
     */
    @Transactional(readOnly = true)
    public DictTypeView getType(Long typeId) {
        DictTypeView view = dictTypeQueryMapper.findById(typeId);
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典类型不存在");
        }
        return view;
    }

    /**
     * 导出字典类型（过滤条件与列表一致，最多 {@link #EXPORT_LIMIT} 行）。
     *
     * @param keyword 可选，匹配编码 / 名称
     * @return 导出行列表
     */
    @Transactional(readOnly = true)
    public List<DictTypeExportRow> exportTypes(String keyword) {
        return dictTypeQueryMapper.listTypes(keyword, EXPORT_LIMIT, 0).stream()
                .map(this::toTypeExportRow)
                .toList();
    }

    /**
     * 新增字典类型；编码在未删除范围内唯一。
     *
     * @param request 保存请求
     * @return 新建读模型
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DICT_OPTIONS, key = "#request.code")
    public DictTypeView createType(DictTypeSaveRequest request) {
        String code = request.getCode().trim();
        if (dictTypeRepository.existsByCodeAndDeleted(code, 0)) {
            throw new BusinessException(ErrorCode.CONFLICT, "字典编码已存在");
        }
        SysDictType type = new SysDictType();
        type.setId(IdGenerator.nextId());
        type.setCode(code);
        type.setName(request.getName().trim());
        type.setRemark(blankToNull(request.getRemark()));
        type.setSort(request.getSort());
        type.setStatus(Boolean.TRUE.equals(request.getStatus()));
        type.setDeleted(0);
        dictTypeRepository.save(type);
        return getType(type.getId());
    }

    /**
     * 修改字典类型；编码创建后不可变更（避免与数据项 {@code type_code} 脱节）。
     *
     * @param typeId  类型主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DICT_OPTIONS, key = "#result.code")
    public DictTypeView updateType(Long typeId, DictTypeSaveRequest request) {
        SysDictType type = dictTypeRepository.findByIdAndDeleted(typeId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "字典类型不存在"));
        String newCode = request.getCode().trim();
        if (!type.getCode().equals(newCode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "字典编码创建后不可修改");
        }
        type.setName(request.getName().trim());
        type.setRemark(blankToNull(request.getRemark()));
        type.setSort(request.getSort());
        type.setStatus(Boolean.TRUE.equals(request.getStatus()));
        dictTypeRepository.save(type);
        return getType(typeId);
    }

    /**
     * 单独切换字典类型启停；停用后 {@link #listOptions(String)} 不再返回该类型数据。
     *
     * @param typeId 类型主键
     * @param status 是否启用
     * @return 更新后读模型
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DICT_OPTIONS, key = "#result.code")
    public DictTypeView changeTypeStatus(Long typeId, boolean status) {
        SysDictType type = dictTypeRepository.findByIdAndDeleted(typeId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "字典类型不存在"));
        type.setStatus(status);
        dictTypeRepository.save(type);
        return getType(typeId);
    }

    /**
     * 逻辑删除字典类型；仍有字典数据时拒绝。
     *
     * @param typeId 类型主键
     * @return 类型编码，供缓存失效使用
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DICT_OPTIONS, key = "#result")
    public String removeType(Long typeId) {
        SysDictType type = dictTypeRepository.findByIdAndDeleted(typeId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "字典类型不存在"));
        if (dictTypeQueryMapper.countDataByTypeCode(type.getCode()) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "请先删除该类型下的字典数据");
        }
        type.setDeleted(1);
        dictTypeRepository.save(type);
        return type.getCode();
    }

    // -------------------------------------------------------------------------
    // 数据
    // -------------------------------------------------------------------------

    /**
     * 分页查询某类型下的字典数据；{@code typeCode} 必填，{@code keyword} 匹配标签或键值。
     *
     * @param typeCode 字典类型编码
     * @param keyword  可选，匹配标签 / 键值
     * @param page     页码
     * @param size     每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<DictDataView> listData(String typeCode, String keyword, Long page, Long size) {
        requireTypeCode(typeCode);
        PageQuery pq = PageQuery.of(page, size);
        long total = dictDataQueryMapper.countData(typeCode, keyword);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, dictDataQueryMapper.listData(typeCode, keyword, pq.getSize(), pq.getOffset()));
    }

    /**
     * 字典数据详情；不存在则 404。
     *
     * @param dataId 数据主键
     * @return 读模型
     */
    @Transactional(readOnly = true)
    public DictDataView getData(Long dataId) {
        DictDataView view = dictDataQueryMapper.findById(dataId);
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典数据不存在");
        }
        return view;
    }

    /**
     * 导出某类型下的字典数据（最多 {@link #EXPORT_LIMIT} 行）。
     *
     * @param typeCode 字典类型编码
     * @param keyword  可选，匹配标签 / 键值
     * @return 导出行列表
     */
    @Transactional(readOnly = true)
    public List<DictDataExportRow> exportData(String typeCode, String keyword) {
        requireTypeCode(typeCode);
        return dictDataQueryMapper.listData(typeCode, keyword, EXPORT_LIMIT, 0).stream()
                .map(this::toDataExportRow)
                .toList();
    }

    /**
     * 新增字典数据；类型须存在；同类型下键值唯一；设为默认项时清除同类型其他默认标记。
     *
     * @param request 保存请求
     * @return 新建读模型
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DICT_OPTIONS, key = "#request.typeCode")
    public DictDataView createData(DictDataSaveRequest request) {
        String typeCode = request.getTypeCode().trim();
        assertTypeExists(typeCode);
        String value = request.getValue().trim();
        if (dictDataRepository.existsByTypeCodeAndValueAndDeleted(typeCode, value, 0)) {
            throw new BusinessException(ErrorCode.CONFLICT, "字典键值已存在");
        }
        if (Boolean.TRUE.equals(request.getDefaultFlag())) {
            dictDataQueryMapper.clearDefaultFlag(typeCode, null);
        }
        SysDictData data = new SysDictData();
        data.setId(IdGenerator.nextId());
        applyData(data, request, typeCode, value);
        data.setDeleted(0);
        dictDataRepository.save(data);
        return getData(data.getId());
    }

    /**
     * 修改字典数据；不可变更所属类型；键值冲突时拒绝；设为默认项时清除同类型其他默认标记。
     *
     * @param dataId  数据主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DICT_OPTIONS, key = "#result.typeCode")
    public DictDataView updateData(Long dataId, DictDataSaveRequest request) {
        SysDictData data = dictDataRepository.findByIdAndDeleted(dataId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "字典数据不存在"));
        String typeCode = request.getTypeCode().trim();
        if (!data.getTypeCode().equals(typeCode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不可变更所属字典类型");
        }
        String value = request.getValue().trim();
        dictDataRepository.findByTypeCodeAndValueAndDeleted(typeCode, value, 0).ifPresent(other -> {
            if (!other.getId().equals(dataId)) {
                throw new BusinessException(ErrorCode.CONFLICT, "字典键值已存在");
            }
        });
        if (Boolean.TRUE.equals(request.getDefaultFlag())) {
            dictDataQueryMapper.clearDefaultFlag(typeCode, dataId);
        }
        applyData(data, request, typeCode, value);
        dictDataRepository.save(data);
        return getData(dataId);
    }

    /**
     * 单独切换字典数据启停；停用项不出现在 {@link #listOptions(String)} 结果中。
     *
     * @param dataId 数据主键
     * @param status 是否启用
     * @return 更新后读模型
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DICT_OPTIONS, key = "#result.typeCode")
    public DictDataView changeDataStatus(Long dataId, boolean status) {
        SysDictData data = dictDataRepository.findByIdAndDeleted(dataId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "字典数据不存在"));
        data.setStatus(status);
        dictDataRepository.save(data);
        return getData(dataId);
    }

    /**
     * 逻辑删除字典数据。
     *
     * @param dataId 数据主键
     * @return 所属类型编码，供缓存失效使用
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DICT_OPTIONS, key = "#result")
    public String removeData(Long dataId) {
        SysDictData data = dictDataRepository.findByIdAndDeleted(dataId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "字典数据不存在"));
        String typeCode = data.getTypeCode();
        data.setDeleted(1);
        dictDataRepository.save(data);
        return typeCode;
    }

    /**
     * 业务下拉选项：仅返回「类型启用 + 数据启用」的项；结果缓存于 {@code dictOptions}。
     *
     * @param typeCode 字典类型编码，如 {@code sys_gender}
     * @return 选项列表
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.DICT_OPTIONS, key = "#p0")
    public List<DictOptionView> listOptions(String typeCode) {
        requireTypeCode(typeCode);
        return dictDataQueryMapper.listOptions(typeCode.trim());
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    /**
     * 将请求字段应用到字典数据实体。
     *
     * @param data      目标实体
     * @param request   保存请求
     * @param typeCode  字典类型编码
     * @param value     字典项值
     */
    private void applyData(SysDictData data, DictDataSaveRequest request, String typeCode, String value) {
        data.setTypeCode(typeCode);
        data.setLabel(request.getLabel().trim());
        data.setValue(value);
        data.setSort(request.getSort());
        data.setCssClass(blankToNull(request.getCssClass()));
        data.setDefaultFlag(Boolean.TRUE.equals(request.getDefaultFlag()));
        data.setStatus(Boolean.TRUE.equals(request.getStatus()));
        data.setRemark(blankToNull(request.getRemark()));
    }

    /**
     * 校验字典类型存在且未删除。
     *
     * @param typeCode 字典类型编码
     */
    private void assertTypeExists(String typeCode) {
        dictTypeRepository.findByCodeAndDeleted(typeCode, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "字典类型不存在"));
    }

    /**
     * 校验类型编码非空。
     *
     * @param typeCode 字典类型编码
     */
    private static void requireTypeCode(String typeCode) {
        if (typeCode == null || typeCode.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "字典类型编码不能为空");
        }
    }

    /**
     * 类型读模型转导出行。
     *
     * @param view 字典类型读模型
     * @return 导出行
     */
    private DictTypeExportRow toTypeExportRow(DictTypeView view) {
        DictTypeExportRow row = new DictTypeExportRow();
        row.setId(view.getId());
        row.setCode(view.getCode());
        row.setName(view.getName());
        row.setSort(view.getSort());
        row.setStatus(Boolean.TRUE.equals(view.getStatus()) ? "启用" : "停用");
        row.setDataCount(view.getDataCount() == null ? 0L : view.getDataCount());
        row.setRemark(view.getRemark());
        return row;
    }

    /**
     * 数据读模型转导出行。
     *
     * @param view 字典数据读模型
     * @return 导出行
     */
    private DictDataExportRow toDataExportRow(DictDataView view) {
        DictDataExportRow row = new DictDataExportRow();
        row.setId(view.getId());
        row.setTypeCode(view.getTypeCode());
        row.setLabel(view.getLabel());
        row.setValue(view.getValue());
        row.setSort(view.getSort());
        row.setCssClass(view.getCssClass());
        row.setDefaultFlag(Boolean.TRUE.equals(view.getDefaultFlag()) ? "是" : "否");
        row.setStatus(Boolean.TRUE.equals(view.getStatus()) ? "启用" : "停用");
        row.setRemark(view.getRemark());
        return row;
    }

    /**
     * 空白字符串转 {@code null}。
     *
     * @param value 原始字符串
     * @return 非空白时返回 trim 后的值，否则 {@code null}
     */
    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
