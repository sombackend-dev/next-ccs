package com.sombackend.nextccs.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nextCcsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Next-CCS API")
                        .version("1.0.0")
                        .description(
                                "Connected Car Core System\n\n" +
                                        "Java 21 / Spring Boot 4 / MySQL\n\n" +
                                        "주요 기능:\n" +
                                        "- 차량 등록 및 조회\n" +
                                        "- 차량 예약 생성 및 기간별 조회\n" +
                                        "- 비관적 락 기반 동시성 제어\n" +
                                        "- 복합 인덱스 기반 예약 조회 최적화"
                        ));
    }
}