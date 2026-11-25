package com.univ.momcare.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 静态资源与跨域配置。
 * /uploads/** 映射到「项目根目录」下的 uploads/（不是用户主目录），换机器即可用。
 *
 * 兼容两种启动位置：项目根目录（./uploads）与 backend 模块目录（../uploads），
 * 取第一个真实存在的目录，避免因工作目录不同导致图片整体 404。
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String abs = resolveUploadDir().toString();
        log.info("[uploads] 静态资源目录映射为 {}", abs);
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + abs + "/");
    }

    private Path resolveUploadDir() {
        List<Path> candidates = List.of(
                Paths.get(uploadDir),
                Paths.get("./uploads"),
                Paths.get("../uploads"),
                Paths.get("../../uploads"));
        for (Path p : candidates) {
            Path abs = p.toAbsolutePath().normalize();
            if (Files.isDirectory(abs)) {
                return abs;
            }
        }
        Path abs = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(abs);
        } catch (IOException e) {
            log.warn("[uploads] 目录创建失败：{}", abs);
        }
        return abs;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
