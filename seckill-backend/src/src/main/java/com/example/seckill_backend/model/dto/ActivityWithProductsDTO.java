package com.example.seckill_backend.model.dto;

import com.example.seckill_backend.model.Activity;
import lombok.Data;

import java.util.List;

@Data
public class ActivityWithProductsDTO {
    private Activity activity;
    private List<SeckillProductCreateDTO> seckillProducts;
}