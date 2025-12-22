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
