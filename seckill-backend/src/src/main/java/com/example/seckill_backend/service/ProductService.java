package com.example.seckill_backend.service;

import com.example.seckill_backend.model.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    Product updateProduct(Product product);
    void deleteProduct(Long id);
    Product getProductById(Long id);
    List<Product> getAllProducts();
    boolean deductSeckillStock(Long productId, int quantity);
    boolean revertSeckillStock(Long productId, int quantity);
    boolean checkSeckillStock(Long productId, int quantity);
}