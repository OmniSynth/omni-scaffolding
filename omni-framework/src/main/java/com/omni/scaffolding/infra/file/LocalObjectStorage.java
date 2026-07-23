package com.omni.scaffolding.infra.file;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.file.ObjectStorage;
import com.omni.scaffolding.common.file.StorageTypes;
import com.omni.scaffolding.common.file.StoreRequest;
import com.omni.scaffolding.common.file.StoredObject;
import com.omni.scaffolding.common.file.StoredObjectRef;
import com.omni.scaffolding.config.OmniFileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 本地磁盘对象存储。
 *
 * <p>根目录为 {@code omni.file.local.base-dir}；对象键相对该目录，并做路径穿越校验。
 * 对外不提供静态映射，内容统一经鉴权/签名接口输出。
 */
@Component
@RequiredArgsConstructor
public class LocalObjectStorage implements ObjectStorage {

    private final OmniFileProperties fileProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public String type() {
        return StorageTypes.LOCAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoredObject store(StoreRequest request) {
        try {
            Path root = rootDir();
            Path target = root.resolve(request.objectKey()).normalize();
            if (!target.startsWith(root)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "非法对象键");
            }
            Files.createDirectories(target.getParent());
            Files.copy(request.content(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredObject(StorageTypes.LOCAL, null, request.objectKey());
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "本地文件保存失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream open(StoredObjectRef ref) {
        try {
            Path root = rootDir();
            Path target = root.resolve(ref.objectKey()).normalize();
            if (!target.startsWith(root) || !Files.isRegularFile(target)) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
            }
            return Files.newInputStream(target);
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "本地文件读取失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(StoredObjectRef ref) {
        try {
            Path root = rootDir();
            Path target = root.resolve(ref.objectKey()).normalize();
            if (target.startsWith(root)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "本地文件删除失败");
        }
    }

    /**
     * 规范化后的本地根目录。
     */
    private Path rootDir() {
        return Path.of(fileProperties.getLocal().getBaseDir()).toAbsolutePath().normalize();
    }
}
