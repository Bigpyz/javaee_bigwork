use seckill;
-- 为商品表添加活动ID和商品图片地址字段
ALTER TABLE product ADD COLUMN activity_id BIGINT COMMENT '关联的活动ID';
ALTER TABLE product ADD COLUMN image_url VARCHAR(255) COMMENT '商品图片地址';

-- 添加外键约束
ALTER TABLE product ADD CONSTRAINT fk_product_activity FOREIGN KEY (activity_id) REFERENCES activity(id);