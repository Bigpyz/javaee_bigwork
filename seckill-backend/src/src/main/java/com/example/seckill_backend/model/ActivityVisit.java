package com.example.seckill_backend.model;

import lombok.Data;

import java.util.Date;

@Data
public class ActivityVisit {
    private Long id;
    private Long activityId;
    private Long userId;
    private String ipAddress;
    private String userAgent;
    private Date visitTime;
    private Integer isUnique;
}