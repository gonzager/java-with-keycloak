package com.fluxit.architecture.reference.authenticated.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenAPIConfiguration {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI().addSecurityItem(new SecurityRequirement().
                        addList("Bearer Authentication"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .info(new Info().title("Backend Java Autenticado")
                        .description("Ejemplo de Backend Java Autenticado muy sencillo.")
                        .version("1.0")
                        .license(new License().name("GPLv3")
                                .url("https://fsfe.org/activities/gplv3/gplv3.es.html")));
    }
}
