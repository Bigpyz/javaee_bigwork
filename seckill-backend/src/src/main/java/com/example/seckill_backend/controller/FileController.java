package com.example.seckill_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    // 上传文件保存路径 - 使用系统通用目录
    private static final String UPLOAD_DIR = "/var/uploads/seckill/";
    
    // 获取绝对路径，用于日志和调试
    private String getUploadAbsolutePath() {
        try {
            return new File(UPLOAD_DIR).getAbsolutePath();
        } catch (Exception e) {
            logger.error("获取上传目录绝对路径失败", e);
            return UPLOAD_DIR;
        }
    }
    
    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam(value = "file", required = false) MultipartFile file) {
        logger.info("接收到图片上传请求");
        try {

            // 检查文件是否为空
            if (file == null || file.isEmpty()) {
                logger.warn("上传失败：文件为空");
                return new ResponseEntity<>("上传失败：文件为空", HttpStatus.BAD_REQUEST);
            }
            
            // 记录文件基本信息
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType();
            long fileSize = file.getSize();
            logger.info("开始处理文件：名称={}, 类型={}, 大小={}字节", originalFilename, contentType, fileSize);
            
            // 检查文件类型
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.warn("上传失败：文件类型不符合要求，实际类型={}", contentType);
                return new ResponseEntity<>("上传失败：请上传图片文件", HttpStatus.BAD_REQUEST);
            }
            
            // 检查文件大小（限制为2MB）
            if (fileSize > 2 * 1024 * 1024) {
                logger.warn("上传失败：文件大小超过限制，实际大小={}字节", fileSize);
                return new ResponseEntity<>("上传失败：图片大小不能超过2MB", HttpStatus.BAD_REQUEST);
            }
            
            // 确保上传目录存在
            Path uploadPath = Paths.get(UPLOAD_DIR);
            String absolutePath = getUploadAbsolutePath();
            logger.info("上传目录相对路径: {}, 绝对路径: {}", UPLOAD_DIR, absolutePath);
            
            if (!Files.exists(uploadPath)) {
                logger.info("上传目录不存在，正在创建：{}", uploadPath);
                Files.createDirectories(uploadPath);
                logger.info("上传目录创建成功：{}", uploadPath);
            }
            
            // 生成唯一文件名
            String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = UUID.randomUUID().toString() + fileExtension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(fileName);
            logger.info("正在保存文件到路径：{} (绝对路径: {})", filePath, filePath.toAbsolutePath());
            Files.copy(file.getInputStream(), filePath);
            logger.info("文件保存成功，大小: {}字节", Files.size(filePath));
            logger.info("文件是否可读: {}", Files.isReadable(filePath));
            
            // 返回图片访问URL
            String imageUrl = "/uploads/" + fileName;
            logger.info("图片上传完成，访问URL：{}", imageUrl);
            return new ResponseEntity<>(imageUrl, HttpStatus.OK);
            
        } catch (IOException e) {
            logger.error("文件上传失败：{}", e.getMessage(), e);
            return new ResponseEntity<>("上传失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}