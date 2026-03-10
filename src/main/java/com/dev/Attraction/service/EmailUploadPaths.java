package com.dev.Attraction.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailUploadPaths {

    @Value("${spring.upload.path}")
    private String uploadRoot;

    @Value("${app.publicBaseUrl}")
    private String publicBaseUrl;
    
    // 실제 파일 저장 루트
    public Path root() {
        return Paths.get(uploadRoot).toAbsolutePath().normalize();
    }

    // 임시 이미지 저장: {root}/email/tmp/{uuid}/
    public Path tmpDir(String uuid) {
        return root().resolve("email").resolve("tmp").resolve(uuid);
    }

    // 실제 이미지 저장: {root}/email/content/{yyyy-MM-dd}/
    public Path contentDir(LocalDate date) {
        return root().resolve("email").resolve("content").resolve(date.toString());
    }

    // 공개 URL prefix (MVC ResourceHandler)
    // /admin/upload/** => file:{root}/
    public String publicUrlOf(Path file) {
        Path root = Paths.get(uploadRoot).toAbsolutePath().normalize();
        Path abs = file.toAbsolutePath().normalize();
        Path rel = root.relativize(abs);

        String relUrl = rel.toString().replace("\\", "/"); // windows 대비
        // ✅ /upload/** 로 노출
        return publicBaseUrl + "/upload/" + relUrl;
    }
}