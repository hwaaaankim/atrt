package com.dev.Attraction.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${spring.upload.path}")
    private String uploadPath;

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = Paths.get(uploadPath).toAbsolutePath().normalize();

        // ✅ 공개 리소스는 /upload/** 로
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + root.toString() + "/")
                .setCachePeriod(3600);
    }
}