package org.example.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentification"))
                .components(new Components().addSecuritySchemes("Bearer Authentification", createBearerScheme()))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
            .title("주식 가격 예측 대시보드 프로젝트")
            .description("Spring doc를 사용한 PredicTick swagger UI")
            .version("1.0.0");
    }

    private SecurityScheme createBearerScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }

}
