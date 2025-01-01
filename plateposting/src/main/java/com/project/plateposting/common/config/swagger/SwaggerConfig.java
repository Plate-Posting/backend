package com.project.plateposting.common.config.swagger;

import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private Info apiInfo() {
        return new Info()
                .title("plateposting")
                .description("식음료 커뮤니티 REST API Document")
                .version("0.0.1");
    }
}
