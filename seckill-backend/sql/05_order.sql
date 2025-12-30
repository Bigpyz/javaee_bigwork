CREATE TABLE `order` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(50) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  activity_id BIGINT NOT NULL,
  seckill_price DECIMAL(10, 2) NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  total_amount DECIMAL(10, 2) NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消, 5-超时未支付',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  payment_time DATETIME,
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (product_id) REFERENCES product(id),
  FOREIGN KEY (activity_id) REFERENCES activity(id)
);