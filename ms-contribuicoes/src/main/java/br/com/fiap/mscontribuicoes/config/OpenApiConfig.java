package br.com.fiap.mscontribuicoes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(@Value("${app.gateway-url:http://localhost:8082}") String gatewayUrl) {
        return new OpenAPI()
                .info(new Info()
                        .title("ms-contribuicoes")
                        .version("v1")
                        .description("Sugestoes de obras novas e moderacao (ArteNaRua)"))
                .servers(List.of(new Server().url(gatewayUrl).description("API Gateway")));
    }
}
