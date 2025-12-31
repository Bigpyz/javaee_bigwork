// src/api/user.js
import http from '../utils/http';

/**
 * 用户注册API
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise} 注册结果Promise
 */
export function register(username, password) {
  return http.post(`/api/users/register`, {
    username,
    password,
  });
}

/**
 * 用户登录API
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise} 登录结果Promise，包含用户信息
 */
export function login(username, password) {
  return http.post(`/api/users/login`, {
    username,
    password,
  });
}

/**
 * 获取当前登录用户信息API
 * @returns {Promise} 用户信息Promise
 */
export function me() {
  return http.get(`/api/users/me`);
}

/**
 * 用户登出API
 * @returns {Promise} 登出结果Promise
 */
export function logout() {
  return http.post(`/api/users/logout`);
}

/**
 * 管理员登录API
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise} 登录结果Promise
 */
export function adminLogin(username, password) {
  return http.post(`/api/admin/login`, {
    username,
    password,
  });
}

/**
 * 管理员登出API
 * @returns {Promise} 登出结果Promise
 */
export function adminLogout() {
  return http.post(`/api/admin/logout`);
}

/**
 * 获取用户信息API
 * @param {number} userId - 用户ID
 * @returns {Promise} 用户信息Promise
 */
export function getUserProfile(userId) {
  return http.get(`/api/users/profile/${userId}`);
}

/**
 * 检查用户参与资格API
 * @param {number} userId - 用户ID
 * @param {string} rule - 资格规则
 * @returns {Promise} 资格检查结果Promise
 */
export function checkEligibility(userId, rule) {
  return http.get(`/api/users/check-eligibility/${userId}`, {
    params: { rule }
  });
}

/**
 * 检查是否为新用户API
 * @param {number} userId - 用户ID
 * @param {number} days - 天数阈值，默认为7天
 * @returns {Promise} 检查结果Promise
 */
export function checkNewUser(userId, days = 7) {
  return http.get(`/api/users/check-new-user/${userId}`, {
    params: { days }
  });
}
