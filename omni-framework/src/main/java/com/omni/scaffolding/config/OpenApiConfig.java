package com.omni.scaffolding.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI 配置。
 *
 * <p>文档地址：{@code /swagger-ui.html}；受保护接口在 UI 中通过 Bearer JWT 鉴权。
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 文档元信息与 Bearer JWT 安全方案。
     *
     * @return 供 Swagger UI 使用的 {@link OpenAPI} 实例
     */
    @Bean
    public OpenAPI openAPI() {
        final String scheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Omni Scaffolding API")
                        .description("Java 21 + Virtual Threads monolithic scaffolding")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme,
                        new SecurityScheme()
                                .name(scheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
