-- ============================================================
-- 家畜养殖信息采集及收益估计系统 — 数据库建表+初始化数据
-- 版本: v1.0
-- 数据库: MySQL 8.0+
-- ============================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS livestock_farming DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE livestock_farming;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE sys_user (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL COMMENT '登录用户名',
    password        VARCHAR(255) NOT NULL COMMENT '加密密码',
    real_name       VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    phone           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    role            TINYINT      NOT NULL DEFAULT 1 COMMENT '角色: 1=养殖户 2=管理员',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删 1=已删',
    UNIQUE KEY uk_username (username)
) COMMENT '系统用户';

-- ============================================================
-- 2. 家畜种类表（支持父子分类：家禽 → 鸡/鸭/鹅）
-- ============================================================
CREATE TABLE livestock_category (
    id                      BIGINT       AUTO_INCREMENT PRIMARY KEY,
    parent_id               BIGINT       DEFAULT NULL COMMENT '父种类ID，NULL表示顶级种类',
    category_name           VARCHAR(50)  NOT NULL COMMENT '种类名称',
    growth_cycle            INT          NOT NULL DEFAULT 0 COMMENT '生长周期（天）',
    slaughter_weight        DECIMAL(8,2) DEFAULT NULL COMMENT '出栏标准体重（kg），NULL表示不使用此标准',
    slaughter_month_age     INT          DEFAULT NULL COMMENT '出栏标准月龄，NULL表示不使用此标准',
    feed_consumption_seedling DECIMAL(8,2) DEFAULT NULL COMMENT '苗种阶段日均饲料消耗（kg）',
    feed_consumption_young  DECIMAL(8,2) DEFAULT NULL COMMENT '青年阶段日均饲料消耗（kg）',
    feed_consumption_adult  DECIMAL(8,2) DEFAULT NULL COMMENT '成年阶段日均饲料消耗（kg）',
    feed_type               VARCHAR(100) DEFAULT NULL COMMENT '推荐饲料类型',
    common_diseases         TEXT         DEFAULT NULL COMMENT '常见病害，逗号分隔',
    breed_mode              TINYINT      DEFAULT NULL COMMENT '养殖模式: 1=散养 2=圈养',
    base_price              DECIMAL(10,2) DEFAULT NULL COMMENT '基准价（元/kg，用于收益预估）',
    feed_price              DECIMAL(10,2) DEFAULT 3.50 COMMENT '饲料单价（元/kg）',
    daily_mgmt_fee          DECIMAL(10,2) DEFAULT 0.00 COMMENT '每日管理费（元/头）',
    estimated_slaughter_rate DECIMAL(5,2) DEFAULT 90.00 COMMENT '预估出栏率（%）',
    sort_order              INT          DEFAULT 0 COMMENT '排序号',
    create_time             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted                 TINYINT      NOT NULL DEFAULT 0,
    KEY idx_parent_id (parent_id)
) COMMENT '家畜种类';

-- ============================================================
-- 3. 养殖场地表
-- ============================================================
CREATE TABLE breeding_site (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    site_code       VARCHAR(50)  NOT NULL COMMENT '场地编号',
    site_name       VARCHAR(100) NOT NULL COMMENT '场地名称',
    location        VARCHAR(200) DEFAULT NULL COMMENT '位置描述',
    capacity        INT          DEFAULT NULL COMMENT '最大容量（头/只）',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_site_code (site_code, deleted)
) COMMENT '养殖场地';

-- ============================================================
-- 4. 存栏批次表
-- ============================================================
CREATE TABLE batch (
    id                      BIGINT       AUTO_INCREMENT PRIMARY KEY,
    batch_no                VARCHAR(50)  NOT NULL COMMENT '批次编号，如 P20260508001',
    category_id             BIGINT       NOT NULL COMMENT '家畜种类ID',
    current_stage           TINYINT      NOT NULL DEFAULT 1 COMMENT '当前生长阶段: 1=苗种 2=青年 3=成年 4=出栏前',
    initial_quantity        INT          NOT NULL COMMENT '入场数量',
    current_quantity        INT          NOT NULL COMMENT '当前存栏量',
    entry_date              DATE         NOT NULL COMMENT '入场日期',
    expected_slaughter_date DATE         DEFAULT NULL COMMENT '预计出栏日期',
    site_id                 BIGINT       DEFAULT NULL COMMENT '养殖场地ID',
    responsible_person      VARCHAR(50)  DEFAULT NULL COMMENT '负责人',
    status                  TINYINT      NOT NULL DEFAULT 1 COMMENT '批次状态: 1=饲养中 2=已结束',
    remark                  VARCHAR(500) DEFAULT NULL COMMENT '备注（来源、品种等）',
    create_time             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted                 TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_batch_no (batch_no, deleted),
    KEY idx_category_id (category_id),
    KEY idx_site_id (site_id),
    KEY idx_status (status),
    KEY idx_entry_date (entry_date)
) COMMENT '存栏批次';

-- ============================================================
-- 5. 存栏变动记录表
-- ============================================================
CREATE TABLE batch_change (
    id                      BIGINT       AUTO_INCREMENT PRIMARY KEY,
    batch_id                BIGINT       NOT NULL COMMENT '批次ID',
    change_type             TINYINT      NOT NULL COMMENT '变动类型: 1=出栏 2=死亡 3=转群 4=补栏 5=转入 6=转出',
    change_quantity         INT          NOT NULL COMMENT '变动数量',
    change_date             DATE         NOT NULL COMMENT '变动日期',
    quantity_after_change   INT          NOT NULL COMMENT '变动后存栏量',
    remark                  VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                 TINYINT      NOT NULL DEFAULT 0,
    KEY idx_batch_id (batch_id),
    KEY idx_change_type (change_type),
    KEY idx_change_date (change_date)
) COMMENT '存栏变动记录';

-- ============================================================
-- 6. 成本记录表
-- ============================================================
CREATE TABLE cost_record (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    batch_no        VARCHAR(50)  DEFAULT NULL COMMENT '批次编号',
    batch_id        BIGINT       DEFAULT NULL COMMENT '关联批次ID（NULL表示按种类录入的公共成本）',
    category_id     BIGINT       NOT NULL COMMENT '家畜种类ID',
    cost_type       TINYINT      NOT NULL COMMENT '成本类型: 1=饲料 2=苗种 3=防疫 4=人工',
    amount          DECIMAL(12,2) NOT NULL COMMENT '金额（元）',
    cost_date       DATE         NOT NULL COMMENT '发生日期',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    KEY idx_batch_id (batch_id),
    KEY idx_category_id (category_id),
    KEY idx_cost_type (cost_type),
    KEY idx_cost_date (cost_date)
) COMMENT '成本记录';

-- ============================================================
-- 7. 价格规则表
-- ============================================================
CREATE TABLE price_rule (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    category_id     BIGINT       NOT NULL COMMENT '适用家畜种类ID',
    rule_name       VARCHAR(100) NOT NULL COMMENT '规则名称',
    direction       TINYINT      NOT NULL COMMENT '浮动方向: 1=上浮 2=下浮',
    ratio           DECIMAL(5,2) NOT NULL COMMENT '浮动比例（%）',
    apply_condition VARCHAR(500) NOT NULL COMMENT '适用条件描述，如 月份∈[12,1,2]',
    priority        INT          NOT NULL DEFAULT 0 COMMENT '优先级，数值小的优先',
    enabled         TINYINT      NOT NULL DEFAULT 1 COMMENT '启用状态: 0=停用 1=启用',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    KEY idx_category_id (category_id),
    KEY idx_enabled (enabled)
) COMMENT '价格规则';

-- ============================================================
-- 8. 养殖建议模板表
-- ============================================================
CREATE TABLE breeding_advice_template (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    category_id     BIGINT       NOT NULL COMMENT '适用家畜种类ID',
    growth_stage    TINYINT      NOT NULL COMMENT '适用生长阶段: 1=苗种 2=青年 3=成年 4=出栏前',
    advice_type     VARCHAR(50)  DEFAULT NULL COMMENT '建议类型，如 饲料配比、防疫提醒、出栏提醒',
    advice_content  TEXT         NOT NULL COMMENT '建议内容',
    sort_order      INT          DEFAULT 0 COMMENT '排序号',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    KEY idx_category_stage (category_id, growth_stage)
) COMMENT '养殖建议模板';

-- ============================================================
-- 9. 养殖建议生成记录表
-- ============================================================
CREATE TABLE breeding_advice (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    batch_id        BIGINT       NOT NULL COMMENT '批次ID',
    template_id     BIGINT       DEFAULT NULL COMMENT '关联模板ID',
    growth_stage    TINYINT      NOT NULL COMMENT '当前生长阶段',
    advice_content  TEXT         NOT NULL COMMENT '建议内容',
    is_read         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读: 0=未读 1=已读',
    trigger_type    TINYINT      DEFAULT NULL COMMENT '触发方式: 1=转群触发 2=时间驱动 3=手动查看',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    KEY idx_batch_id (batch_id),
    KEY idx_growth_stage (growth_stage)
) COMMENT '养殖建议生成记录';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 默认管理员账号（密码: admin123）
INSERT INTO sys_user (username, password, real_name, role) VALUES
('admin', 'admin123', 'System Admin', 2);

-- 预设家畜种类
INSERT INTO livestock_category (id, parent_id, category_name, growth_cycle, slaughter_weight, slaughter_month_age,
    feed_consumption_seedling, feed_consumption_young, feed_consumption_adult,
    feed_type, breed_mode, base_price, feed_price, daily_mgmt_fee, estimated_slaughter_rate, sort_order) VALUES
(1,  NULL, '生猪',   180, 110.00, 6,  1.2, 2.0, 2.8, '玉米-豆粕型',    2, 16.00, 3.50, 0.70, 90.00, 1),
(2,  NULL, '肉牛',   365, 500.00, 12, 5.0, 9.0, 12.0, '青贮-精饲料型', 1, 30.00, 3.50, 3.00, 95.00, 2),
(3,  NULL, '肉羊',   210, 40.00,  7,  1.0, 2.0, 3.0, '牧草-精饲料型',  1, 50.00, 3.50, 0.70, 92.00, 3),
(4,  NULL, '家禽',   0, NULL,   NULL, NULL, NULL, NULL, NULL,               2, NULL,  NULL, NULL, NULL,   4),
(5,  4,    '鸡',     60,  2.00,   NULL, 0.05, 0.08, 0.12, '配合饲料',      2, 12.00, 3.50, 0.03, 92.00, 5),
(6,  4,    '鸭',     70,  3.00,   NULL, 0.05, 0.08, 0.13, '配合饲料',      2, 10.00, 3.50, 0.03, 90.00, 6),
(7,  4,    '鹅',     90,  5.00,   NULL, 0.10, 0.15, 0.25, '青草-配合饲料', 1, 15.00, 3.50, 0.06, 88.00, 7);

-- 预设价格规则
INSERT INTO price_rule (category_id, rule_name, direction, ratio, apply_condition, priority, enabled) VALUES
(2, 'Winter Beef Price Up',   1, 10.00, 'month IN (12,1,2)',  1, 1),
(2, 'Spring Festival Beef Up', 1, 8.00,  'month = 1',         2, 1),
(4, 'Poultry Peak Season Down', 2, 5.00, 'month IN (6,7,8)',  1, 1),
(1, 'Spring Festival Pork Up', 1, 8.00,  'month = 1',         1, 1),
(3, 'Winter Hotpot Season Up',   1, 12.00, 'month IN (11,12,1)', 1, 1);

-- 演示数据：养殖场地
INSERT INTO breeding_site (id, site_code, site_name, location, capacity, remark) VALUES
(1, 'DQ-001', '东区育肥场', '养殖区东侧', 500, '标准化育肥猪舍'),
(2, 'XQ-001', '西区繁育基地', '养殖区西侧', 300, '牛舍及产房'),
(3, 'NQ-001', '南区生态养殖区', '养殖区南侧', 800, '生态放养场地'),
(4, 'BQ-001', '北区家禽养殖场', '养殖区北侧', 1000, '多层笼养设备');

-- 演示数据：存栏批次
INSERT INTO batch (id, batch_no, category_id, current_stage, initial_quantity, current_quantity, entry_date, expected_slaughter_date, site_id, responsible_person, status, remark) VALUES
(1, 'P20260201001', 1, 3, 100, 96, '2026-02-01', '2026-07-30', 1, '张三', 1, '杜洛克商品猪'),
(2, 'P20260401001', 1, 2, 80, 78, '2026-04-01', '2026-09-28', 1, '张三', 1, '长白仔猪'),
(3, 'P20251101001', 2, 3, 50, 48, '2025-11-01', '2026-10-31', 2, '李四', 1, '西门塔尔肉牛'),
(4, 'P20260301001', 2, 2, 30, 30, '2026-03-01', '2027-03-01', 2, '李四', 1, '安格斯青年牛'),
(5, 'P20260315001', 3, 2, 200, 195, '2026-03-15', '2026-10-11', 3, '王五', 1, '湖羊'),
(6, 'P20260420001', 5, 1, 500, 480, '2026-04-20', '2026-06-19', 4, '赵六', 1, '白羽肉鸡'),
(7, 'P20260425001', 6, 1, 300, 290, '2026-04-25', '2026-07-04', 4, '赵六', 1, '樱桃谷鸭'),
(8, 'P20260301002', 7, 2, 100, 95, '2026-03-01', '2026-05-30', 3, '王五', 1, '狮头鹅');

-- 演示数据：存栏变动（入场记录）
INSERT INTO batch_change (batch_id, change_type, change_quantity, change_date, quantity_after_change, remark) VALUES
(1, 5, 100, '2026-02-01', 100, '首批仔猪转入'),
(1, 2, 2,  '2026-03-15', 98,  '弱仔死亡'),
(1, 2, 2,  '2026-04-20', 96,  '呼吸道治疗无效'),
(2, 5, 80, '2026-04-01', 80,  '仔猪转入'),
(2, 2, 2,  '2026-05-01', 78,  '应激死亡'),
(3, 5, 50, '2025-11-01', 50,  '育肥牛转入'),
(3, 2, 2,  '2026-02-10', 48,  '意外伤亡'),
(4, 5, 30, '2026-03-01', 30,  '青年牛转入'),
(5, 5, 200, '2026-03-15', 200, '羔羊转入'),
(5, 2, 5,  '2026-04-10', 195, '弱羔死亡'),
(6, 5, 500, '2026-04-20', 500, '鸡苗进场'),
(6, 2, 20, '2026-05-05', 480, '弱雏淘汰'),
(7, 5, 300, '2026-04-25', 300, '鸭苗进场'),
(7, 2, 10, '2026-05-08', 290, '弱雏淘汰'),
(8, 5, 100, '2026-03-01', 100, '雏鹅转入'),
(8, 2, 5,  '2026-04-15', 95,  '意外死亡');

-- 演示数据：成本记录（近3个月）
INSERT INTO cost_record (batch_no, batch_id, category_id, cost_type, amount, cost_date, remark) VALUES
('P20260201001', 1, 1, 1, 24500.00, '2026-03-01', '3月饲料采购'),
('P20260201001', 1, 1, 1, 25200.00, '2026-04-01', '4月饲料采购'),
('P20260201001', 1, 1, 1, 24800.00, '2026-05-01', '5月饲料采购'),
('P20260201001', 1, 1, 3, 3000.00,  '2026-03-15', '3月人工费'),
('P20260201001', 1, 1, 3, 3000.00,  '2026-04-15', '4月人工费'),
('P20260201001', 1, 1, 3, 3000.00,  '2026-05-15', '5月人工费'),
('P20260201001', 1, 1, 2, 1500.00,  '2026-03-20', '春季防疫'),
('P20260401001', 2, 1, 1, 8600.00,  '2026-04-01', '4月饲料采购'),
('P20260401001', 2, 1, 1, 9200.00,  '2026-05-01', '5月饲料采购'),
('P20260401001', 2, 1, 3, 2000.00,  '2026-04-15', '4月人工费'),
('P20260401001', 2, 1, 3, 2000.00,  '2026-05-15', '5月人工费'),
('P20251101001', 3, 2, 1, 18000.00, '2026-03-01', '3月精饲料'),
('P20251101001', 3, 2, 1, 18500.00, '2026-04-01', '4月精饲料'),
('P20251101001', 3, 2, 1, 18200.00, '2026-05-01', '5月精饲料'),
('P20251101001', 3, 2, 3, 5000.00,  '2026-03-15', '3月人工费'),
('P20251101001', 3, 2, 3, 5000.00,  '2026-04-15', '4月人工费'),
('P20251101001', 3, 2, 3, 5000.00,  '2026-05-15', '5月人工费'),
('P20251101001', 3, 2, 4, 1200.00,  '2026-03-10', '3月水电'),
('P20251101001', 3, 2, 4, 1300.00,  '2026-04-10', '4月水电'),
('P20251101001', 3, 2, 4, 1250.00,  '2026-05-10', '5月水电'),
('P20260301001', 4, 2, 1, 6500.00,  '2026-04-01', '4月精饲料'),
('P20260301001', 4, 2, 1, 6800.00,  '2026-05-01', '5月精饲料'),
('P20260315001', 5, 3, 1, 9600.00,  '2026-04-01', '4月饲料'),
('P20260315001', 5, 3, 1, 10200.00, '2026-05-01', '5月饲料'),
('P20260315001', 5, 3, 3, 3500.00,  '2026-04-15', '4月人工费'),
('P20260315001', 5, 3, 3, 3500.00,  '2026-05-15', '5月人工费'),
('P20260420001', 6, 5, 1, 3800.00,  '2026-05-01', '5月饲料'),
('P20260420001', 6, 5, 3, 1500.00,  '2026-05-15', '5月人工费'),
('P20260425001', 7, 6, 1, 2200.00,  '2026-05-01', '5月饲料'),
('P20260301002', 8, 7, 1, 3500.00,  '2026-04-01', '4月饲料'),
('P20260301002', 8, 7, 1, 3600.00,  '2026-05-01', '5月饲料');

-- 演示数据：养殖建议模板
INSERT INTO breeding_advice_template (id, category_id, growth_stage, advice_type, advice_content, sort_order) VALUES
(1, 1, 1, '饲料配比', '苗猪阶段建议使用仔猪专用料，蛋白质含量18%-20%，每日饲喂4-5次，少喂勤添。', 1),
(2, 1, 2, '饲料配比', '青年猪阶段蛋白质含量可降至16%，增加能量饲料比例，每日饲喂3次。', 2),
(3, 1, 3, '饲料配比', '育肥猪阶段使用高能低蛋白日粮，蛋白质14%-15%，注意控制脂肪沉积。', 3),
(4, 1, 4, '出栏提醒', '已到达出栏标准体重，建议尽快安排出栏，避免饲料浪费。', 4),
(5, 1, 2, '防疫提醒', '青年猪阶段需进行口蹄疫、猪瘟疫苗加强免疫。', 5),
(6, 2, 1, '饲料配比', '犊牛阶段以母乳或代乳粉为主，逐步引导采食优质粗饲料。', 1),
(7, 2, 2, '饲料配比', '青年牛阶段粗饲料比例60%，精饲料40%，保证日增重1.0-1.2kg。', 2),
(8, 2, 3, '饲料配比', '育肥牛阶段精饲料比例提升至60%-70%，添加适量矿物质和维生素。', 3);

-- 演示数据：养殖建议
INSERT INTO breeding_advice (batch_id, template_id, growth_stage, advice_content, is_read, trigger_type) VALUES
(1, 3, 3, '育肥猪阶段使用高能低蛋白日粮，蛋白质14%-15%，注意控制脂肪沉积。当前存栏96头，预计出栏日期2026-07-30。', 0, 2),
(2, 2, 2, '青年猪阶段蛋白质含量可降至16%，增加能量饲料比例，每日饲喂3次。当前存栏78头。', 0, 2),
(3, 8, 3, '育肥牛阶段精饲料比例提升至60%-70%，添加适量矿物质和维生素。当前存栏48头，预计出栏2026-10-31。', 1, 2),
(5, 7, 2, '青年羊阶段注意补充蛋白质和矿物质，保证日增重达标。当前存栏195只。', 0, 2);

-- ============================================================
-- 10. 系统配置表
-- ============================================================
CREATE TABLE system_config (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    config_key      VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value    VARCHAR(255) DEFAULT NULL COMMENT '配置值',
    config_name     VARCHAR(100) NOT NULL COMMENT '配置名称',
    description     VARCHAR(500) DEFAULT NULL COMMENT '配置说明',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key)
) COMMENT '系统配置';

INSERT INTO system_config (config_key, config_value, config_name, description) VALUES
('farm_name', '智慧养殖管理系统', '系统名称', '系统显示名称'),
('contact_phone', '', '联系电话', '系统联系电话'),
('notification_enabled', 'true', '启用通知', '是否启用系统通知功能');
