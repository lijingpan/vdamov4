ALTER TABLE da_product
    ADD COLUMN product_type VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '商品类型',
    ADD COLUMN spec_mode VARCHAR(16) NOT NULL DEFAULT 'SINGLE' COMMENT '规格模式',
    ADD COLUMN description VARCHAR(500) NULL COMMENT '商品描述',
    ADD COLUMN attr_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用商品属性',
    ADD COLUMN material_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用加料',
    ADD COLUMN weighed_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否称重商品';

CREATE TABLE da_product_sku (
    id BIGINT PRIMARY KEY COMMENT '主键',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    sku_code VARCHAR(64) NULL COMMENT 'SKU编码',
    barcode VARCHAR(64) NULL COMMENT '商品条码',
    spec_key VARCHAR(255) NOT NULL COMMENT '规格唯一键',
    spec_name VARCHAR(255) NOT NULL COMMENT '规格展示名',
    price_in_cent INT NOT NULL COMMENT '销售价分',
    line_price_in_cent INT NOT NULL DEFAULT 0 COMMENT '划线价分',
    cost_price_in_cent INT NOT NULL DEFAULT 0 COMMENT '成本价分',
    box_fee_in_cent INT NOT NULL DEFAULT 0 COMMENT '包装费分',
    stock_qty INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    auto_replenish TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否自动补货',
    weight_unit_gram INT NULL COMMENT '称重基准克数',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    creator VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag INT DEFAULT 1 COMMENT '删除标识',
    test_flag INT DEFAULT 0 COMMENT '测试标识'
) COMMENT='商品SKU表';

CREATE TABLE da_product_spec (
    id BIGINT PRIMARY KEY COMMENT '主键',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    name VARCHAR(64) NOT NULL COMMENT '规格名',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    creator VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag INT DEFAULT 1 COMMENT '删除标识',
    test_flag INT DEFAULT 0 COMMENT '测试标识'
) COMMENT='商品规格组表';

CREATE TABLE da_product_spec_value (
    id BIGINT PRIMARY KEY COMMENT '主键',
    product_spec_id BIGINT NOT NULL COMMENT '规格组ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    name VARCHAR(64) NOT NULL COMMENT '规格值',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    creator VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag INT DEFAULT 1 COMMENT '删除标识',
    test_flag INT DEFAULT 0 COMMENT '测试标识'
) COMMENT='商品规格值表';

CREATE TABLE da_product_attr (
    id BIGINT PRIMARY KEY COMMENT '主键',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    name VARCHAR(64) NOT NULL COMMENT '属性名',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    creator VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag INT DEFAULT 1 COMMENT '删除标识',
    test_flag INT DEFAULT 0 COMMENT '测试标识'
) COMMENT='商品属性组表';

CREATE TABLE da_product_attr_value (
    id BIGINT PRIMARY KEY COMMENT '主键',
    product_attr_id BIGINT NOT NULL COMMENT '属性组ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    name VARCHAR(64) NOT NULL COMMENT '属性值',
    is_default TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认值',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    creator VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag INT DEFAULT 1 COMMENT '删除标识',
    test_flag INT DEFAULT 0 COMMENT '测试标识'
) COMMENT='商品属性值表';
