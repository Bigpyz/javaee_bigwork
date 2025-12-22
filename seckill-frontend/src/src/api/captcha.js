// src/api/captcha.js
import http from '../utils/http';

export function generateCaptcha(userId, productId) {
  return http.get(`/api/captcha/generate`, {
    params: {
      userId,
      productId
    }
  });
}

export function verifyCaptcha(captchaId, captchaValue) {
  return http.post(`/api/captcha/verify`, null, {
    params: {
      captchaId,
      captchaValue
    }
  });
}