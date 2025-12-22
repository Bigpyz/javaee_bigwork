package com.example.seckill_backend.model;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private Date registerTime;
    private String phone;
    private String email;
    private Integer status;
    private Date lastLoginTime;
}