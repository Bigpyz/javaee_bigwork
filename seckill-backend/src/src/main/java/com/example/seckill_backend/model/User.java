package com.example.seckill_backend.model;

import lombok.Data;

import java.util.Date;

/**
 * 用户实体类
 * 对应数据库中的user表
 */
@Data  // Lombok注解，自动生成getter、setter等方法
public class User {
    private Long id;              // 用户ID
    private String username;       // 用户名
    private String password;       // 密码（MD5加密）
    private Date registerTime;     // 注册时间
    private String phone;          // 手机号
    private String email;          // 邮箱
    private Integer status;        // 状态（1：正常，0：禁用）
    private Date lastLoginTime;    // 最后登录时间
}
