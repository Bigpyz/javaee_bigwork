use seckill;
-- activity_stats表：存储活动的汇总统计数据
CREATE TABLE activity_stats (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  activity_name VARCHAR(100) NOT NULL,
  pv INT NOT NULL DEFAULT 0 COMMENT '页面浏览量',
  uv INT NOT NULL DEFAULT 0 COMMENT '独立访客数',
  participants INT NOT NULL DEFAULT 0 COMMENT '参与人数',
  sold_quantity INT NOT NULL DEFAULT 0 COMMENT '售出商品数',
  total_gmv DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '成交总额',
  pending_orders INT NOT NULL DEFAULT 0 COMMENT '待支付订单数',
  completed_orders INT NOT NULL DEFAULT 0 COMMENT '已完成订单数',
  cancelled_orders INT NOT NULL DEFAULT 0 COMMENT '已取消订单数',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE,
  UNIQUE KEY uk_activity_id (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动统计数据表';