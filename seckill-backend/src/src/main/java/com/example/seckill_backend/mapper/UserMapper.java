package com.example.seckill_backend.mapper;

import com.example.seckill_backend.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Date;

/**
 * 用户数据访问接口
 * 定义了用户相关的数据库操作方法
 */
@Mapper  // MyBatis注解，标记为数据访问层接口
public interface UserMapper {
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    User getUserById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User getUserByUsername(@Param("username") String username);

    /**
     * 插入新用户
     * @param user 用户对象
     * @return 影响的行数
     */
    int insertUser(User user);

    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 影响的行数
     */
    int updateUser(User user);

    /**
     * 删除用户
     * @param id 用户ID
     * @return 影响的行数
     */
    int deleteUser(@Param("id") Long id);

    /**
     * 更新用户最后登录时间
     * @param id 用户ID
     * @param lastLoginTime 最后登录时间
     * @return 影响的行数
     */
    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") Date lastLoginTime);

    /**
     * 检查用户是否在指定时间内注册（用于新用户资格校验）
     * @param id 用户ID
     * @param days 天数
     * @return 符合条件的用户数量
     */
    int countNewUsersByRegisterTime(@Param("id") Long id, @Param("days") int days);
}
