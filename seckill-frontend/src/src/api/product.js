// src/api/product.js
import http from '../utils/http';

export function getAllProducts() {
  return http.get(`/api/products`);
}

export function getProductById(id) {
  return http.get(`/api/products/${id}`);
}

export function createProduct(product) {
  return http.post(`/api/products`, product);
}

export function updateProduct(id, product) {
  return http.put(`/api/products/${id}`, product);
}

export function checkStock(id, quantity = 1) {
  return http.get(`/api/products/${id}/stock`, {
    params: { quantity }
  });
}
