package com.example.seckill_backend.mapper;

import com.example.seckill_backend.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Date;

@Mapper
public interface UserMapper {
    User getUserById(@Param("id") Long id);

    User getUserByUsername(@Param("username") String username);

    int insertUser(User user);

    int updateUser(User user);

    int deleteUser(@Param("id") Long id);

    /**
     * 更新用户最后登录时间
     */
    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") Date lastLoginTime);

    /**
     * 检查用户是否在指定时间内注册（用于新用户资格校验）
     */
    int countNewUsersByRegisterTime(@Param("id") Long id, @Param("days") int days);
}