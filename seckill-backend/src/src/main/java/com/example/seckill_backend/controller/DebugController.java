package com.example.seckill_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);
    
    @Value("${spring.web.resources.static-locations}")
    private String staticLocations;
    
    @GetMapping("/static-config")
    public ResponseEntity<String> getStaticConfig() {
        return ResponseEntity.ok("静态资源配置: " + staticLocations);
    }
    
    @GetMapping("/upload-dir")
    public ResponseEntity<String> checkUploadDir() {
        String uploadDir = "./uploads/";
        Path uploadPath = Paths.get(uploadDir);
        
        try {
            // 获取当前工作目录
            String currentDir = System.getProperty("user.dir");
            logger.info("当前工作目录: {}", currentDir);
            
            // 检查上传目录
            if (Files.exists(uploadPath)) {
                String files = Files.list(uploadPath)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.joining(", "));
                
                return ResponseEntity.ok("上传目录存在，文件: " + files);
            } else {
                return ResponseEntity.ok("上传目录不存在: " + uploadPath.toAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("检查上传目录失败", e);
            return ResponseEntity.ok("检查上传目录失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/working-dir")
    public ResponseEntity<String> getWorkingDir() {
        return ResponseEntity.ok("当前工作目录: " + System.getProperty("user.dir"));
    }
}