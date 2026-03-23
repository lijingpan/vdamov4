CREATE TABLE da_store (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    status VARCHAR(32) NOT NULL,
    business_modes VARCHAR(255) NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_store_device (
    id BIGINT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(32) NOT NULL,
    purpose VARCHAR(64),
    brand VARCHAR(64),
    sn VARCHAR(64),
    size VARCHAR(32),
    online_status VARCHAR(32),
    enabled BOOLEAN NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_member (
    id BIGINT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    level_code VARCHAR(32) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    phone_national VARCHAR(32) NOT NULL,
    phone_e164 VARCHAR(32) NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_product (
    id BIGINT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(64) NOT NULL,
    category_code VARCHAR(32) NOT NULL,
    product_type VARCHAR(32) NOT NULL,
    spec_mode VARCHAR(16) NOT NULL,
    price_in_cent INT NOT NULL,
    description VARCHAR(500),
    attr_enabled BOOLEAN NOT NULL,
    material_enabled BOOLEAN NOT NULL,
    weighed_enabled BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_product_sku (
    id BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    sku_code VARCHAR(64),
    barcode VARCHAR(64),
    spec_key VARCHAR(255) NOT NULL,
    spec_name VARCHAR(255) NOT NULL,
    price_in_cent INT NOT NULL,
    line_price_in_cent INT NOT NULL,
    cost_price_in_cent INT NOT NULL,
    box_fee_in_cent INT NOT NULL,
    stock_qty INT NOT NULL,
    auto_replenish BOOLEAN NOT NULL,
    weight_unit_gram INT,
    sort_order INT NOT NULL,
    active BOOLEAN NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_product_spec (
    id BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_product_spec_value (
    id BIGINT PRIMARY KEY,
    product_spec_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_product_attr (
    id BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_product_attr_value (
    id BIGINT PRIMARY KEY,
    product_attr_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    is_default BOOLEAN NOT NULL,
    sort_order INT NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_product_category (
    id BIGINT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    category_code VARCHAR(32) NOT NULL,
    sort_order INT NOT NULL,
    enabled BOOLEAN NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_discount (
    id BIGINT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(64) NOT NULL,
    discount_type VARCHAR(32) NOT NULL,
    amount_type VARCHAR(16) NOT NULL,
    amount_value INT NOT NULL,
    threshold_amount_in_cent INT NOT NULL,
    stackable BOOLEAN NOT NULL,
    enabled BOOLEAN NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    remark VARCHAR(255),
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_table (
    id BIGINT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    area_name VARCHAR(64) NOT NULL,
    table_name VARCHAR(64) NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_table_area (
    id BIGINT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    area_name VARCHAR(64) NOT NULL,
    area_code VARCHAR(32) NOT NULL,
    sort_order INT NOT NULL,
    enabled BOOLEAN NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_order (
    id BIGINT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL,
    store_id BIGINT NOT NULL,
    table_id BIGINT NOT NULL,
    member_id BIGINT,
    original_amount_in_cent INT NOT NULL,
    discount_amount_in_cent INT NOT NULL,
    payable_amount_in_cent INT NOT NULL,
    paid_amount_in_cent INT NOT NULL,
    order_status VARCHAR(32) NOT NULL,
    payment_status VARCHAR(32) NOT NULL,
    append_count INT NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_order_item (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    product_id BIGINT,
    item_name VARCHAR(100) NOT NULL,
    item_code VARCHAR(64),
    unit_price_in_cent INT NOT NULL,
    quantity INT NOT NULL,
    original_amount_in_cent INT NOT NULL,
    discount_amount_in_cent INT NOT NULL,
    payable_amount_in_cent INT NOT NULL,
    item_status VARCHAR(32) NOT NULL,
    append_round INT NOT NULL,
    remark VARCHAR(255),
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_order_append_log (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    append_round INT NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    append_item_count INT NOT NULL,
    append_amount_in_cent INT NOT NULL,
    operate_time TIMESTAMP NOT NULL,
    operator_name VARCHAR(100),
    note VARCHAR(255),
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_payment_record (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    payment_no VARCHAR(64) NOT NULL,
    payment_method VARCHAR(32) NOT NULL,
    payment_channel VARCHAR(32),
    paid_amount_in_cent INT NOT NULL,
    change_amount_in_cent INT NOT NULL,
    payment_status VARCHAR(32) NOT NULL,
    paid_time TIMESTAMP NOT NULL,
    cashier_name VARCHAR(100),
    remark VARCHAR(255),
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_role (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_menu (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT,
    name VARCHAR(100) NOT NULL,
    menu_type VARCHAR(16) NOT NULL,
    route VARCHAR(128) NOT NULL,
    permission_code VARCHAR(128) NOT NULL,
    sort_order INT NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_user_role (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_role_menu (
    id BIGINT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);

CREATE TABLE da_user_store (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    creator VARCHAR(32) DEFAULT 'system',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(32) DEFAULT 'system',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 1,
    test_flag INT DEFAULT 0
);
