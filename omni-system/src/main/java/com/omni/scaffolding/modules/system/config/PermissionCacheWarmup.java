package com.omni.scaffolding.modules.system.config;

import com.omni.scaffolding.modules.system.service.PermissionCacheEvictor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 启动完成后清空动态权限缓存。
 *
 * <p>Flyway 菜单迁移不会走 {@code MenuService}，否则会留下过期的 {@code userPermissions}，
 * 侧栏已显示新菜单但接口仍 403「无权限访问」。
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class PermissionCacheWarmup implements ApplicationRunner {

    private final PermissionCacheEvictor permissionCacheEvictor;

    @Override
    public void run(ApplicationArguments args) {
        permissionCacheEvictor.evictAll();
        log.info("已清空动态权限缓存 userPermissions（适配菜单迁移）");
    }
}
