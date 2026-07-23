package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.file.StoreRequest;
import com.omni.scaffolding.common.file.StoredObject;
import com.omni.scaffolding.common.file.StoredObjectRef;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.config.OmniFileProperties;
import com.omni.scaffolding.infra.file.FileContentSigner;
import com.omni.scaffolding.infra.file.FileStorageEngine;
import com.omni.scaffolding.modules.system.dto.file.FilePreviewUrlView;
import com.omni.scaffolding.modules.system.dto.file.FileView;
import com.omni.scaffolding.modules.system.entity.SysFile;
import com.omni.scaffolding.modules.system.mapper.SysFileQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysFileRepository;
import com.omni.scaffolding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 统一文件服务：上传落库、分页查询、鉴权/签名内容下载、预览 URL、删除。
 *
 * <p>写路径：JPA + {@link FileStorageEngine}；列表读：MyBatis QueryMapper（从库约定）。
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private static final Set<String> AVATAR_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final SysFileRepository fileRepository;
    private final SysFileQueryMapper fileQueryMapper;
    private final FileStorageEngine storageEngine;
    private final FileContentSigner contentSigner;
    private final OmniFileProperties fileProperties;

    /**
     * 分页查询文件，并为每条记录填充短时 {@code previewUrl}。
     *
     * @param keyword           可选，文件名关键字
     * @param bizType           可选，业务类型
     * @param storageType       可选，存储类型
     * @param contentTypePrefix 可选，MIME 前缀
     * @param pageQuery         分页参数
     * @return 分页结果
     */
    public PageResult<FileView> page(String keyword, String bizType, String storageType,
                                     String contentTypePrefix, PageQuery pageQuery) {
        long total = fileQueryMapper.countFiles(keyword, bizType, storageType, contentTypePrefix);
        List<FileView> records = fileQueryMapper.searchFiles(
                keyword, bizType, storageType, contentTypePrefix, pageQuery.getSize(), pageQuery.getOffset());
        records.forEach(this::fillPreviewUrl);
        return pageQuery.toResult(total, records);
    }

    /**
     * 按主键获取未删除文件详情（含 previewUrl）。
     *
     * @param id 文件主键
     * @return 读模型
     */
    public FileView get(Long id) {
        SysFile file = requireFile(id);
        FileView view = toView(file);
        fillPreviewUrl(view);
        return view;
    }

    /**
     * 校验大小/类型后写入对象存储，并保存 {@link SysFile} 元数据。
     *
     * @param file    上传文件
     * @param bizType 业务类型；空则 {@code common}；{@code avatar} 限制图片扩展名与大小
     * @return 含 id 与 previewUrl 的读模型
     */
    @Transactional
    public FileView upload(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请上传文件");
        }
        String resolvedBiz = StringUtils.hasText(bizType) ? bizType.trim() : "common";
        validateSize(file, resolvedBiz);
        validateContentType(file);

        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = resolveExtension(originalName, file.getContentType());
        if ("avatar".equalsIgnoreCase(resolvedBiz) && !AVATAR_EXT.contains(ext)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的头像格式");
        }

        String objectKey = buildObjectKey(resolvedBiz, ext);
        long id = IdGenerator.nextId();
        try (InputStream in = file.getInputStream()) {
            StoredObject stored = storageEngine.store(new StoreRequest(
                    objectKey,
                    file.getContentType(),
                    file.getSize(),
                    in,
                    originalName));
            SysFile entity = new SysFile();
            entity.setId(id);
            entity.setOriginalName(originalName);
            entity.setContentType(file.getContentType());
            entity.setSizeBytes(file.getSize());
            entity.setStorageType(stored.storageType());
            entity.setOssProvider(stored.ossProvider());
            entity.setObjectKey(stored.objectKey());
            entity.setBizType(resolvedBiz);
            entity.setCreatedBy(SecurityUtils.getUserId());
            entity.setDeleted(0);
            fileRepository.save(entity);
            FileView view = toView(entity);
            fillPreviewUrl(view);
            return view;
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件读取失败");
        }
    }

    /**
     * 签发短时预览 URL（文件须存在且未删除）。
     *
     * @param id 文件主键
     * @return 预览地址与过期秒
     */
    public FilePreviewUrlView previewUrl(Long id) {
        requireFile(id);
        long expire = contentSigner.defaultExpireEpoch();
        String sign = contentSigner.sign(id, expire);
        return new FilePreviewUrlView(id, contentSigner.buildContentPath(id, expire, sign), expire);
    }

    /**
     * 流式输出文件内容；须已登录（非匿名）或签名合法。
     *
     * @param id     文件主键
     * @param expire 签名过期时间（Unix 秒）
     * @param sign   HMAC 签名
     * @return 带 Content-Type / Disposition 的流式响应
     */
    public ResponseEntity<InputStreamResource> content(Long id, Long expire, String sign) {
        assertReadable(id, expire, sign);
        SysFile file = requireFile(id);
        InputStream stream = storageEngine.open(new StoredObjectRef(
                file.getStorageType(), file.getOssProvider(), file.getObjectKey()));
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(file.getContentType())) {
            try {
                mediaType = MediaType.parseMediaType(file.getContentType());
            } catch (Exception ignored) {
                // keep octet-stream
            }
        }
        String encoded = URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encoded)
                .contentType(mediaType)
                .contentLength(file.getSizeBytes() == null ? -1 : file.getSizeBytes())
                .body(new InputStreamResource(stream));
    }

    /**
     * 逻辑删除元数据；若开启 physical-delete 则尽力物理删除对象（失败不回滚元数据）。
     *
     * @param id 文件主键
     */
    @Transactional
    public void remove(Long id) {
        SysFile file = requireFile(id);
        file.setDeleted(1);
        fileRepository.save(file);
        if (fileProperties.isPhysicalDelete()) {
            try {
                storageEngine.delete(new StoredObjectRef(
                        file.getStorageType(), file.getOssProvider(), file.getObjectKey()));
            } catch (BusinessException ex) {
                // 元数据已删；物理删除失败不回滚，避免残留阻断业务
            }
        }
    }

    /**
     * 断言可读：签名有效，或 SecurityContext 中已认证（非 anonymous）。
     */
    private void assertReadable(Long id, Long expire, String sign) {
        if (contentSigner.verify(id, expire, sign)) {
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() != null
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return;
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "无权访问文件或签名已失效");
    }

    /**
     * 加载未删除文件，不存在则 404。
     */
    private SysFile requireFile(Long id) {
        return fileRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "文件不存在"));
    }

    /**
     * 按业务类型校验大小上限。
     */
    private void validateSize(MultipartFile file, String bizType) {
        long max = "avatar".equalsIgnoreCase(bizType)
                ? fileProperties.getAvatarMaxBytes()
                : fileProperties.getMaxBytes();
        if (file.getSize() > max) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件过大");
        }
    }

    /**
     * 若配置了 MIME 白名单则校验 Content-Type。
     */
    private void validateContentType(MultipartFile file) {
        List<String> allowed = fileProperties.getAllowedContentTypes();
        if (allowed == null || allowed.isEmpty()) {
            return;
        }
        String ct = file.getContentType();
        if (!StringUtils.hasText(ct) || allowed.stream().noneMatch(a -> a.equalsIgnoreCase(ct))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的文件类型");
        }
    }

    /**
     * 为读模型填充短时 previewUrl。
     */
    private void fillPreviewUrl(FileView view) {
        if (view == null || view.getId() == null) {
            return;
        }
        long expire = contentSigner.defaultExpireEpoch();
        String sign = contentSigner.sign(view.getId(), expire);
        view.setPreviewUrl(contentSigner.buildContentPath(view.getId(), expire, sign));
    }

    /**
     * 实体转读模型（不含 previewUrl）。
     */
    private static FileView toView(SysFile file) {
        FileView view = new FileView();
        view.setId(file.getId());
        view.setOriginalName(file.getOriginalName());
        view.setContentType(file.getContentType());
        view.setSizeBytes(file.getSizeBytes());
        view.setStorageType(file.getStorageType());
        view.setOssProvider(file.getOssProvider());
        view.setObjectKey(file.getObjectKey());
        view.setBizType(file.getBizType());
        view.setMd5(file.getMd5());
        view.setCreatedBy(file.getCreatedBy());
        view.setCreatedAt(file.getCreatedAt());
        return view;
    }

    /**
     * 生成对象键：{@code {bizType}/{uuid}[.ext]}。
     */
    private static String buildObjectKey(String bizType, String ext) {
        String safeBiz = bizType.replaceAll("[^a-zA-Z0-9_-]", "_");
        String name = UUID.randomUUID().toString().replace("-", "");
        if (StringUtils.hasText(ext)) {
            return safeBiz + "/" + name + "." + ext;
        }
        return safeBiz + "/" + name;
    }

    /**
     * 从文件名或 Content-Type 解析扩展名；jpeg 归一为 jpg。
     */
    private static String resolveExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                    .toLowerCase(Locale.ROOT);
            if (!ext.isBlank() && ext.length() <= 16) {
                return "jpeg".equals(ext) ? "jpg" : ext;
            }
        }
        if (contentType == null) {
            return "";
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "";
        };
    }
}
