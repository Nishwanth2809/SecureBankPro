package com.securebank.pro.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Phase 16 — Swagger / OpenAPI Documentation Configuration.
 *
 * Exposes:
 *  - Swagger UI  →  http://localhost:8080/swagger-ui.html
 *  - OpenAPI JSON →  http://localhost:8080/v3/api-docs
 *
 * A global JWT Bearer security scheme is registered so every protected
 * endpoint can be tested directly from the Swagger UI after pasting a token.
 */
@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "BearerAuth";

    @Bean
    public OpenAPI secureBankOpenAPI() {
        return new OpenAPI()
                // ── API Metadata ──────────────────────────────────────────
                .info(new Info()
                        .title("SecureBankPro REST API")
                        .description("""
                                **SecureBankPro** is a full-stack Spring Boot banking application \
                                built across 16 learning phases covering Java core concepts, \
                                Spring Data JPA, Spring Security, JWT authentication, \
                                transaction management, multithreading, and testing.

                                ### Authentication
                                1. Call `POST /api/auth/login` to obtain a JWT token.
                                2. Click the **Authorize** button (🔒) at the top-right of this page.
                                3. Enter `Bearer <your-token>` in the *Value* field and click **Authorize**.
                                4. All protected endpoints will now include the token automatically.

                                ### Default Credentials
                                | Role | Email | Password |
                                |------|-------|----------|
                                | ADMIN | admin@securebank.com | Admin@123 |
                                | CUSTOMER | nishant@example.com | Pass@123 |
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SecureBankPro Team")
                                .email("admin@securebank.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // ── Server URLs ───────────────────────────────────────────
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))

                // ── JWT Bearer Security Scheme ────────────────────────────
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste your JWT token (without 'Bearer' prefix). " +
                                        "Obtain one from POST /api/auth/login.")))

                // Apply JWT security globally to all endpoints
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
