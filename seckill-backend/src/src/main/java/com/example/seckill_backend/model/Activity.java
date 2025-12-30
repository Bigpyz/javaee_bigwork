package com.example.seckill_backend.model;

import lombok.Data;

import java.util.Date;

@Data
public class Activity {
    private Long id;
    private String name;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private String rule;
    private Date createTime;
    private Date updateTime;
}