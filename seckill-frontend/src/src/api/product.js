// src/api/product.js
import http from '../utils/http';

export function getAllProducts() {
  return http.get(`/api/products`);
}

export function getProductById(id) {
  return http.get(`/api/products/${id}`);
}

export function checkStock(id, quantity = 1) {
  return http.get(`/api/products/${id}/stock`, {
    params: { quantity }
  });
}
