package com.example.seckill_backend.mapper;

import com.example.seckill_backend.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
    int insert(Product product);
    int update(Product product);
    int deleteById(Long id);
    Product selectById(Long id);
    List<Product> selectAll();
    int updateSeckillStock(@Param("id") Long id, @Param("decrement") int decrement);
    int revertSeckillStock(@Param("id") Long id, @Param("increment") int increment);
}