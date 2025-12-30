package com.example.seckill_backend.service;

import com.example.seckill_backend.model.Activity;
import com.example.seckill_backend.model.ActivityProduct;
import com.example.seckill_backend.model.ActivityStats;
import com.example.seckill_backend.model.dto.ActivityWithProductsDTO;

import java.util.List;

public interface ActivityService {
    Activity createActivity(Activity activity);
    Activity createActivityWithProducts(ActivityWithProductsDTO activityWithProductsDTO);
    Activity updateActivity(Activity activity);
    void deleteActivity(Long id);
    Activity getActivityById(Long id);
    List<Activity> getAllActivities();
    List<Activity> getActivitiesByStatus(int status);
    List<Activity> getUpcomingActivities();
    List<Activity> getActiveActivities();
    boolean startActivity(Long id);
    boolean endActivity(Long id);
    ActivityProduct addProductToActivity(ActivityProduct activityProduct);
    List<ActivityProduct> getProductsByActivityId(Long activityId);
    void updateActivityStatus();

}