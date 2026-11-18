package br.com.fiap.msobras.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    /** O "Try it out" do Swagger chama o API Gateway (porta fixa), que balanceia entre as instancias. */
    @Bean
    public OpenAPI openAPI(@Value("${app.gateway-url:http://localhost:8082}") String gatewayUrl) {
        return new OpenAPI()
                .info(new Info()
                        .title("ms-obras")
                        .version("v1")
                        .description("Catalogo de artistas e obras de arte urbana (ArteNaRua)"))
                .servers(List.of(new Server().url(gatewayUrl).description("API Gateway")));
    }
}
