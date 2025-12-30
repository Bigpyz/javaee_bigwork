// src/api/user.js
import http from '../utils/http';

export function register(username, password) {
  return http.post(`/api/users/register`, {
    username,
    password,
  });
}

export function login(username, password) {
  return http.post(`/api/users/login`, {
    username,
    password,
  });
}

export function me() {
  return http.get(`/api/users/me`);
}

export function logout() {
  return http.post(`/api/users/logout`);
}

export function adminLogin(username, password) {
  return http.post(`/api/admin/login`, {
    username,
    password,
  });
}

export function adminLogout() {
  return http.post(`/api/admin/logout`);
}

export function getUserProfile(userId) {
  return http.get(`/api/users/profile/${userId}`);
}

export function checkEligibility(userId, rule) {
  return http.get(`/api/users/check-eligibility/${userId}`, {
    params: { rule }
  });
}

export function checkNewUser(userId, days = 7) {
  return http.get(`/api/users/check-new-user/${userId}`, {
    params: { days }
  });
}
