use seckill;
-- activity_visit表：记录用户访问活动页面的详细信息
CREATE TABLE activity_visit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  user_id BIGINT COMMENT '用户ID，未登录用户为NULL',
  ip_address VARCHAR(50) NOT NULL COMMENT '访问者IP地址',
  user_agent VARCHAR(200) COMMENT '用户浏览器信息',
  visit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  is_unique TINYINT NOT NULL DEFAULT 1 COMMENT '是否为独立访客：1-是，0-否',
  FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动页面访问记录表';