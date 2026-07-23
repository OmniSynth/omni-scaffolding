package com.omni.scaffolding.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * JPA 实体审计基类：创建/更新时间 + 乐观锁版本号。
 *
 * <p>所有走 JPA 写路径的业务表实体应继承本类；Schema 需包含同名列（见 Flyway 脚本）。
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditableEntity {

    /**
     * 创建时间（UTC Instant），插入时由 JPA Auditing 自动填充，不可更新。
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * 最后更新时间，每次 flush 时由 JPA Auditing 自动刷新。
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * 乐观锁版本号；并发更新冲突时抛 OptimisticLockException。
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
