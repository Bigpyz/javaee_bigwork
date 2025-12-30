// src/api/order.js
import http from '../utils/http';

export function createSeckillOrder(userId, activityId, productId, quantity = 1, captchaId, captchaValue) {
  return http.post(`/api/orders/seckill`, {
    userId,
    activityId,
    productId,
    quantity,
    captchaId,
    captchaValue
  });
}

export function getOrderById(id) {
  return http.get(`/api/orders/${id}`);
}

export function getOrderByOrderNo(orderNo) {
  return http.get(`/api/orders/order-no/${orderNo}`);
}

export function getOrdersByUserId(userId) {
  return http.get(`/api/orders/user/${userId}`);
}

export function cancelOrder(id) {
  return http.post(`/api/orders/${id}/cancel`);
}

export function payOrder(id) {
  return http.post(`/api/orders/${id}/pay`);
}
