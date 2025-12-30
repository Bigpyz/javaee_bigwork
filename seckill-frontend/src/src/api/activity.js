// src/api/activity.js
import http from '../utils/http';

export function getAllActivities() {
  return http.get(`/api/activities`);
}

export function getActivityById(id) {
  return http.get(`/api/activities/${id}`);
}

export function getActivitiesByStatus(status) {
  return http.get(`/api/activities/status/${status}`);
}

export function getUpcomingActivities() {
  return http.get(`/api/activities/upcoming`);
}

export function getActiveActivities() {
  return http.get(`/api/activities/active`);
}

export function getProductsByActivityId(activityId) {
  return http.get(`/api/activities/${activityId}/products`);
}



export function getProductById(productId) {
  return http.get(`/api/products/${productId}`);
}

export function updateActivity(id, activity) {
  return http.put(`/api/activities/${id}`, activity);
}

export function createActivity(activity) {
  return http.post(`/api/activities`, activity);
}

export function closeActivity(id) {
  return http.post(`/api/activities/${id}/end`);
}

export function getActivityStats(activityId) {
  return http.get(`/api/activities/${activityId}/stats`);
}

export function createActivityWithProducts(activityData) {
  return http.post(`/api/activities/with-products`, activityData);
}

export function recordVisit(activityId, userId) {
  return http.post(`/api/activities/${activityId}/record-visit`, null, {
    params: {
      userId
    }
  });
}
