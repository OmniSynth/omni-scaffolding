package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.config.OmniUploadProperties;
import com.omni.scaffolding.modules.system.dto.AvatarUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 头像本地存储服务。
 *
 * <p>文件写入 {@code omni.upload.base-dir}/avatars，对外返回
 * {@code /uploads/avatars/xxx} 形式的相对 URL，由静态资源映射对外提供访问。
 */
@Service
@RequiredArgsConstructor
public class AvatarStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final OmniUploadProperties uploadProperties;

    /**
     * 校验大小与类型后保存头像。
     *
     * @param file multipart 文件，字段名通常为 {@code file}
     * @return 可访问的相对 URL
     * @throws BusinessException 空文件、超限、类型不支持或 IO 失败
     */
    public AvatarUploadResult storeAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请上传头像文件");
        }
        if (file.getSize() > uploadProperties.getAvatarMaxBytes()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "头像文件过大");
        }
        String ext = resolveExtension(file.getOriginalFilename(), file.getContentType());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的头像格式");
        }

        try {
            Path dir = Path.of(uploadProperties.getBaseDir(), "avatars").toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target);
            String url = uploadProperties.getUrlPrefix().replaceAll("/$", "") + "/avatars/" + filename;
            return new AvatarUploadResult(url);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "头像保存失败");
        }
    }

    /**
     * 优先从文件名后缀解析扩展名，否则回退 Content-Type。
     */
    private static String resolveExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            if (ALLOWED_EXT.contains(ext)) {
                return ext.equals("jpeg") ? "jpg" : ext;
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
