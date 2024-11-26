// WebConfig.java
package com.campers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // 모든 도메인 허용 또는 특정 도메인 지정
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드 파일 경로를 static으로 처리
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:src/main/resources/uploads/"); // 파일 업로드 경로 설정
    }
}
