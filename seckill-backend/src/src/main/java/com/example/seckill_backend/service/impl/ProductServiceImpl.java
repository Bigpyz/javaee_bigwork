package com.example.seckill_backend.service.impl;

import com.example.seckill_backend.mapper.ProductMapper;
import com.example.seckill_backend.model.Product;
import com.example.seckill_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;

    @Override
    public Product createProduct(Product product) {
        productMapper.insert(product);
        return product;
    }

    @Override
    public Product updateProduct(Product product) {
        productMapper.update(product);
        return product;
    }

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<Product> getAllProducts() {
        return productMapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductSeckillStock(Long productId, int quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getSeckillStock() < quantity) {
            return false;
        }
        return productMapper.updateSeckillStock(productId, quantity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revertSeckillStock(Long productId, int quantity) {
        return productMapper.revertSeckillStock(productId, quantity) > 0;
    }

    @Override
    public boolean checkSeckillStock(Long productId, int quantity) {
        Product product = productMapper.selectById(productId);
        return product != null && product.getSeckillStock() >= quantity;
    }
}