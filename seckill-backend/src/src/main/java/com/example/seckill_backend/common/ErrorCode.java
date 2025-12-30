package com.example.seckill_backend.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    OK("0", "OK", HttpStatus.OK),

    BAD_REQUEST("40000", "请求参数错误", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("40100", "未登录或无权限", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("40300", "无权限", HttpStatus.FORBIDDEN),
    NOT_FOUND("40400", "资源不存在", HttpStatus.NOT_FOUND),

    CAPTCHA_RATE_LIMITED("42901", "验证码请求过于频繁", HttpStatus.TOO_MANY_REQUESTS),
    CAPTCHA_INVALID("40010", "验证码错误或已过期", HttpStatus.BAD_REQUEST),

    OUT_OF_STOCK("40020", "商品库存不足", HttpStatus.BAD_REQUEST),
    TOO_FREQUENT("42900", "请求过于频繁", HttpStatus.TOO_MANY_REQUESTS),

    PAYMENT_SIGNATURE_INVALID("40030", "支付回调验签失败", HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_MISMATCH("40031", "支付金额不匹配", HttpStatus.BAD_REQUEST),
    PAYMENT_REPLAY("40032", "支付回调重复或重放", HttpStatus.BAD_REQUEST),

    INTERNAL_ERROR("50000", "系统异常", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
