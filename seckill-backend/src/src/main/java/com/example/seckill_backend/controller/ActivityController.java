package com.example.seckill_backend.controller;

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
    public Activity createActivity(@RequestBody Activity activity) {
        return activityService.createActivity(activity);
    }

    @PostMapping("/with-products")
    public Activity createActivityWithProducts(@RequestBody ActivityWithProductsDTO dto) {
        return activityService.createActivityWithProducts(dto);
    }

    @PutMapping("/{id}")
    public Activity updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        activity.setId(id);
        return activityService.updateActivity(activity);
    }

    @DeleteMapping("/{id}")
    public void deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
    }

    @GetMapping("/{id}")
    public Activity getActivityById(@PathVariable Long id) {
        return activityService.getActivityById(id);
    }

    @GetMapping
    public List<Activity> getAllActivities() {
        return activityService.getAllActivities();
    }

    @GetMapping("/status/{status}")
    public List<Activity> getActivitiesByStatus(@PathVariable int status) {
        return activityService.getActivitiesByStatus(status);
    }

    @GetMapping("/upcoming")
    public List<Activity> getUpcomingActivities() {
        return activityService.getUpcomingActivities();
    }

    @GetMapping("/active")
    public List<Activity> getActiveActivities() {
        return activityService.getActiveActivities();
    }

    @PostMapping("/{id}/start")
    public boolean startActivity(@PathVariable Long id) {
        return activityService.startActivity(id);
    }

    @PostMapping("/{id}/end")
    public boolean endActivity(@PathVariable Long id) {
        return activityService.endActivity(id);
    }

    @PostMapping("/products")
    public ActivityProduct addProductToActivity(@RequestBody ActivityProduct activityProduct) {
        return activityService.addProductToActivity(activityProduct);
    }

    @GetMapping("/{id}/products")
    public List<ActivityProduct> getProductsByActivityId(@PathVariable Long id) {
        return activityService.getProductsByActivityId(id);
    }

    @PostMapping("/update-status")
    public void updateActivityStatus() {
        activityService.updateActivityStatus();
    }

    @GetMapping("/{id}/stats")
    public ActivityStats getActivityStats(@PathVariable Long id) {
        return activityStatsService.getActivityStats(id);
    }

    @PostMapping("/{id}/record-visit")
    public void recordVisit(@PathVariable Long id, @RequestParam(required = false) Long userId, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        activityStatsService.recordVisit(id, userId, ipAddress, userAgent);
    }


}