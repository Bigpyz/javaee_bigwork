package com.example.seckill_backend.controller;

import com.example.seckill_backend.common.ApiResponse;
import com.example.seckill_backend.model.Activity;
import com.example.seckill_backend.model.ActivityProduct;
import com.example.seckill_backend.model.ActivityStats;
import com.example.seckill_backend.model.dto.ActivityWithProductsDTO;

import com.example.seckill_backend.service.ActivityService;
import com.example.seckill_backend.service.ActivityStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityStatsService activityStatsService;

    @PostMapping
    public ApiResponse<Activity> createActivity(@RequestBody Activity activity) {
        return ApiResponse.success(activityService.createActivity(activity));
    }

    @PostMapping("/with-products")
    public ApiResponse<Activity> createActivityWithProducts(@RequestBody ActivityWithProductsDTO dto) {
        return ApiResponse.success(activityService.createActivityWithProducts(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<Activity> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        activity.setId(id);
        return ApiResponse.success(activityService.updateActivity(activity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<Activity> getActivityById(@PathVariable Long id) {
        return ApiResponse.success(activityService.getActivityById(id));
    }

    @GetMapping
    public ApiResponse<List<Activity>> getAllActivities() {
        return ApiResponse.success(activityService.getAllActivities());
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<Activity>> getActivitiesByStatus(@PathVariable int status) {
        return ApiResponse.success(activityService.getActivitiesByStatus(status));
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<Activity>> getUpcomingActivities() {
        return ApiResponse.success(activityService.getUpcomingActivities());
    }

    @GetMapping("/active")
    public ApiResponse<List<Activity>> getActiveActivities() {
        return ApiResponse.success(activityService.getActiveActivities());
    }

    @PostMapping("/{id}/start")
    public ApiResponse<Boolean> startActivity(@PathVariable Long id) {
        return ApiResponse.success(activityService.startActivity(id));
    }

    @PostMapping("/{id}/end")
    public ApiResponse<Boolean> endActivity(@PathVariable Long id) {
        return ApiResponse.success(activityService.endActivity(id));
    }

    @PostMapping("/products")
    public ApiResponse<ActivityProduct> addProductToActivity(@RequestBody ActivityProduct activityProduct) {
        return ApiResponse.success(activityService.addProductToActivity(activityProduct));
    }

    @GetMapping("/{id}/products")
    public ApiResponse<List<ActivityProduct>> getProductsByActivityId(@PathVariable Long id) {
        return ApiResponse.success(activityService.getProductsByActivityId(id));
    }

    @PostMapping("/update-status")
    public ApiResponse<Void> updateActivityStatus() {
        activityService.updateActivityStatus();
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/stats")
    public ApiResponse<ActivityStats> getActivityStats(@PathVariable Long id) {
        return ApiResponse.success(activityStatsService.getActivityStats(id));
    }

    @PostMapping("/{id}/record-visit")
    public ApiResponse<Void> recordVisit(@PathVariable Long id, @RequestParam(required = false) Long userId, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        activityStatsService.recordVisit(id, userId, ipAddress, userAgent);
        return ApiResponse.success(null);
    }
}