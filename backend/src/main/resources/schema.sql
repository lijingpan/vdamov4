CREATE TABLE da_store (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
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
    price_in_cent INT NOT NULL,
    active BOOLEAN NOT NULL,
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
