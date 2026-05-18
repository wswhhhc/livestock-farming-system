-- 给家畜种类表添加饲料单价字段（元/kg），默认 3.50 元/kg
ALTER TABLE livestock_category
    ADD COLUMN feed_price DECIMAL(10, 2) DEFAULT 3.50 COMMENT '饲料单价(元/kg)' AFTER base_price;
