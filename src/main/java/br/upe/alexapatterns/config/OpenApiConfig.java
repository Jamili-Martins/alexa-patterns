package br.upe.alexapatterns.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura a documentação e a autenticação no Swagger.
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

  /**
   * Documenta o uso de tokens Bearer nas rotas protegidas.
   */
  @Bean
  public OpenAPI openApi() {
    SecurityScheme bearer = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");

    return new OpenAPI()
            .info(new Info().title("AlexaPatterns").version("1.0.0")
                    .description("Simulador de assistente musical"))
            .components(new Components().addSecuritySchemes("bearerAuth", bearer))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}
