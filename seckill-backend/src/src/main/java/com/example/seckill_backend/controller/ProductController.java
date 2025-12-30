package com.example.seckill_backend.controller;

import com.example.seckill_backend.common.ApiResponse;
import com.example.seckill_backend.model.Product;
import com.example.seckill_backend.model.dto.ProductCreateRequest;
import com.example.seckill_backend.model.dto.ProductUpdateRequest;
import com.example.seckill_backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * ProductController
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ApiResponse<Product> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setActivityId(request.getActivityId());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setSeckillPrice(request.getSeckillPrice());
        product.setTotalStock(request.getTotalStock());
        product.setSeckillStock(request.getSeckillStock());
        product.setStatus(request.getStatus());
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        return ApiResponse.success(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ApiResponse<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        Product product = new Product();
        product.setId(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setActivityId(request.getActivityId());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setSeckillPrice(request.getSeckillPrice());
        product.setTotalStock(request.getTotalStock());
        product.setSeckillStock(request.getSeckillStock());
        product.setStatus(request.getStatus());
        product.setUpdateTime(new Date());
        return ApiResponse.success(productService.updateProduct(product));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> getProductById(@PathVariable Long id) {
        return ApiResponse.success(productService.getProductById(id));
    }

    @GetMapping
    public ApiResponse<List<Product>> getAllProducts() {
        return ApiResponse.success(productService.getAllProducts());
    }

    @GetMapping("/{id}/stock")
    public ApiResponse<Boolean> checkStock(@PathVariable Long id, @RequestParam(defaultValue = "1") int quantity) {
        return ApiResponse.success(productService.checkSeckillStock(id, quantity));
    }
}