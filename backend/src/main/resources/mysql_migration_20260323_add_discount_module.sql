CREATE TABLE IF NOT EXISTS da_discount (
    id BIGINT PRIMARY KEY COMMENT 'Primary key',
    store_id BIGINT NOT NULL COMMENT 'Store id',
    name VARCHAR(100) NOT NULL COMMENT 'Discount name',
    code VARCHAR(64) NOT NULL COMMENT 'Discount code in one store',
    discount_type VARCHAR(32) NOT NULL COMMENT 'MEMBER_PRICE/ORDER_DISCOUNT/FULL_REDUCTION/COUPON',
    amount_type VARCHAR(16) NOT NULL COMMENT 'FIXED/PERCENT',
    amount_value INT NOT NULL COMMENT 'Fixed amount in cent or percent value',
    threshold_amount_in_cent INT NOT NULL DEFAULT 0 COMMENT 'Minimum order threshold in cent',
    stackable TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Can stack with other discounts',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Enable status',
    start_time DATETIME NULL COMMENT 'Effective start time',
    end_time DATETIME NULL COMMENT 'Effective end time',
    remark VARCHAR(255) NULL COMMENT 'Remark',
    creator VARCHAR(32) DEFAULT 'system' COMMENT 'Creator',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updater VARCHAR(32) DEFAULT 'system' COMMENT 'Updater',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    del_flag INT DEFAULT 1 COMMENT 'Delete flag',
    test_flag INT DEFAULT 0 COMMENT 'Test flag'
) COMMENT='Discount rule table';

INSERT INTO da_menu (id, parent_id, name, menu_type, route, permission_code, sort_order, creator, updater)
SELECT 14, NULL, 'Discount', 'MENU', '/discounts', 'discount:view', 14, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM da_menu WHERE id = 14 OR permission_code = 'discount:view');

INSERT INTO da_menu (id, parent_id, name, menu_type, route, permission_code, sort_order, creator, updater)
SELECT 2201, 14, 'Discount Create', 'BUTTON', '', 'discount:create', 141, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM da_menu WHERE id = 2201 OR permission_code = 'discount:create');

INSERT INTO da_menu (id, parent_id, name, menu_type, route, permission_code, sort_order, creator, updater)
SELECT 2202, 14, 'Discount Update', 'BUTTON', '', 'discount:update', 142, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM da_menu WHERE id = 2202 OR permission_code = 'discount:update');

INSERT INTO da_menu (id, parent_id, name, menu_type, route, permission_code, sort_order, creator, updater)
SELECT 2203, 14, 'Discount Enable', 'BUTTON', '', 'discount:enable', 143, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM da_menu WHERE id = 2203 OR permission_code = 'discount:enable');

INSERT INTO da_menu (id, parent_id, name, menu_type, route, permission_code, sort_order, creator, updater)
SELECT 2204, 14, 'Discount Delete', 'BUTTON', '', 'discount:delete', 144, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM da_menu WHERE id = 2204 OR permission_code = 'discount:delete');

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 93, role.id, 14, 'system', 'system'
FROM da_role role
WHERE role.code = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 93 OR (role_id = role.id AND menu_id = 14));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 94, role.id, 2201, 'system', 'system'
FROM da_role role
WHERE role.code = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 94 OR (role_id = role.id AND menu_id = 2201));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 95, role.id, 2202, 'system', 'system'
FROM da_role role
WHERE role.code = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 95 OR (role_id = role.id AND menu_id = 2202));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 96, role.id, 2203, 'system', 'system'
FROM da_role role
WHERE role.code = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 96 OR (role_id = role.id AND menu_id = 2203));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 97, role.id, 2204, 'system', 'system'
FROM da_role role
WHERE role.code = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 97 OR (role_id = role.id AND menu_id = 2204));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 98, role.id, 14, 'system', 'system'
FROM da_role role
WHERE role.code = 'STORE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 98 OR (role_id = role.id AND menu_id = 14));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 99, role.id, 2201, 'system', 'system'
FROM da_role role
WHERE role.code = 'STORE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 99 OR (role_id = role.id AND menu_id = 2201));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 100, role.id, 2202, 'system', 'system'
FROM da_role role
WHERE role.code = 'STORE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 100 OR (role_id = role.id AND menu_id = 2202));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 101, role.id, 2203, 'system', 'system'
FROM da_role role
WHERE role.code = 'STORE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 101 OR (role_id = role.id AND menu_id = 2203));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 102, role.id, 2204, 'system', 'system'
FROM da_role role
WHERE role.code = 'STORE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 102 OR (role_id = role.id AND menu_id = 2204));
