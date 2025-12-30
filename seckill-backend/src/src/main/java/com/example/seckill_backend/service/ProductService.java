package com.example.seckill_backend.service;

import com.example.seckill_backend.model.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    Product updateProduct(Product product);
    void deleteProduct(Long id);
    Product getProductById(Long id);
    List<Product> getAllProducts();
    boolean checkSeckillStock(Long productId, int quantity);

    /**
     * 扣减活动商品秒杀库存
     * @param activityId 活动ID
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 扣减是否成功
     */
    boolean deductActivityProductStock(Long activityId, Long productId, int quantity);

    /**
     * 回滚活动商品秒杀库存
     * @param activityId 活动ID
     * @param productId 商品ID
     * @param quantity 回滚数量
     * @return 回滚是否成功
     */
    boolean revertActivityProductStock(Long activityId, Long productId, int quantity);
}