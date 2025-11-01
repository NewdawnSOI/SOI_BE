package com.soi.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${swagger.server.url:http://localhost}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        // serverUrl이 이미 포트를 포함하면 그대로 쓰고, 아니면 포트 붙이기
        String baseUrl = serverUrl.contains(":") ? serverUrl : serverUrl + ":" + serverPort;

        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url(baseUrl).description("현재 서버"),
                        new Server().url("http://localhost:8080").description("로컬 개발 서버")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("SOI API")
                .description("SOI 애플리케이션을 위한 REST API 문서")
                .version("v1.0.0")
                .contact(new Contact()
                        .name("SOI")
                        .email("itisnewdawn@gmail.com")
                        .url("https://github.com/NewdawnSOI"));
    }
}
