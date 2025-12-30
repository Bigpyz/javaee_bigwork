package com.example.seckill_backend.mapper;

import com.example.seckill_backend.model.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityMapper {
    int insert(Activity activity);
    int update(Activity activity);
    int deleteById(Long id);
    Activity selectById(Long id);
    List<Activity> selectAll();
    List<Activity> selectByStatus(int status);
    List<Activity> selectUpcoming();
    List<Activity> selectActive();
    int updateStatus(@Param("id") Long id, @Param("status") int status);
}