CREATE TABLE activity_product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  seckill_price DECIMAL(10, 2) NOT NULL,
  seckill_stock INT NOT NULL DEFAULT 0,
  limit_per_user INT NOT NULL DEFAULT 1 COMMENT '每人限购数量',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (activity_id) REFERENCES activity(id),
  FOREIGN KEY (product_id) REFERENCES product(id),
  UNIQUE KEY uk_activity_product (activity_id, product_id)
);