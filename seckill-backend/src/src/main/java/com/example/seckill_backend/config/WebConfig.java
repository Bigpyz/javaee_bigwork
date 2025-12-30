package com.example.seckill_backend.config;

import com.example.seckill_backend.common.MockAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MockAuthInterceptor mockAuthInterceptor;

    @Value("${seckill.upload.dir:./uploads/}")
    private String uploadDir;

    public WebConfig(MockAuthInterceptor mockAuthInterceptor) {
        this.mockAuthInterceptor = mockAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mockAuthInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源处理
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + normalizeUploadDir(uploadDir))
                .setCachePeriod(3600)
                .resourceChain(true);
    }

    private String normalizeUploadDir(String dir) {
        if (dir == null || dir.isBlank()) {
            return "./uploads/";
        }
        return dir.endsWith("/") || dir.endsWith("\\") ? dir : (dir + "/");
    }
}