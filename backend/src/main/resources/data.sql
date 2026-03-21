INSERT INTO da_store (id, name, country_code, status, business_modes) VALUES
(2, 'chindatest', 'CN', 'OPEN', 'DINE_IN,TAKEAWAY'),
(54, 'CHINDAHOTPOT 010', 'TH', 'REST', 'DINE_IN');

INSERT INTO da_store_device (id, store_id, name, type, purpose, brand, sn, size, online_status, enabled) VALUES
(1001, 2, 'kitchen-printer-1', 'PRINTER', 'KITCHEN_PRINT', 'Feie', '922421085', '58mm', 'ONLINE', TRUE),
(2001, 2, 'voice-speaker-1', 'SPEAKER', 'ORDER_BROADCAST', 'YunLaba', 'DMCGB008202408000003', '--', 'ONLINE', TRUE),
(3001, 54, 'front-printer-1', 'PRINTER', 'CHECKOUT_PRINT', 'Feie', '922476189', '58mm', 'OFFLINE', FALSE);

INSERT INTO da_member (id, store_id, level_code, display_name, country_code, phone_national, phone_e164) VALUES
(362223, 54, 'VIP1', 'N', '+66', '893699324', '+66893699324'),
(362220, 2, 'NORMAL', 'Patcharee', '+86', '13800138000', '+8613800138000');

INSERT INTO da_product (id, store_id, name, code, category_code, price_in_cent, active) VALUES
(1169, 2, 'Signature Milk Tea', 'signature-milk-tea', 'DRINK', 1990, TRUE),
(1165, 54, 'Buffet 388', 'buffet-388', 'SET_MEAL', 35000, TRUE);

INSERT INTO da_product_category (id, store_id, category_name, category_code, sort_order, enabled) VALUES
(1301, 2, 'Drinks', 'DRINK', 10, TRUE),
(1302, 2, 'Snacks', 'SNACK', 20, TRUE),
(1303, 54, 'Set Meal', 'SET_MEAL', 10, TRUE),
(1304, 54, 'Seafood', 'SEAFOOD', 20, FALSE);

INSERT INTO da_table (id, store_id, area_name, table_name, capacity, status) VALUES
(2101, 2, 'Hall A', 'A01', 4, 'IDLE'),
(2102, 2, 'Hall A', 'A02', 6, 'IN_USE'),
(2103, 2, 'Hall B', 'B01', 8, 'WAITING_CHECKOUT'),
(5401, 54, 'Main Hall', 'T01', 4, 'IN_USE'),
(5402, 54, 'Main Hall', 'T02', 4, 'IDLE'),
(5403, 54, 'VIP', 'V01', 10, 'DISABLED');

INSERT INTO da_table_area (id, store_id, area_name, area_code, sort_order, enabled) VALUES
(3101, 2, 'Hall A', 'HALL_A', 10, TRUE),
(3102, 2, 'Hall B', 'HALL_B', 20, TRUE),
(3103, 54, 'Main Hall', 'MAIN_HALL', 10, TRUE),
(3104, 54, 'VIP', 'VIP', 20, FALSE);

INSERT INTO da_order (
    id, order_no, store_id, table_id, member_id,
    original_amount_in_cent, discount_amount_in_cent, payable_amount_in_cent, paid_amount_in_cent,
    order_status, payment_status, append_count, create_time
) VALUES
(90001, 'OD202603200001', 2, 2102, 362220, 9800, 1200, 8600, 0, 'IN_PROGRESS', 'UNPAID', 1, CURRENT_TIMESTAMP),
(90002, 'OD202603200002', 2, 2103, NULL, 15600, 600, 15000, 15000, 'WAITING_CHECKOUT', 'PARTIAL', 0, CURRENT_TIMESTAMP),
(90003, 'OD202603200003', 54, 5401, 362223, 42000, 2000, 40000, 0, 'IN_PROGRESS', 'UNPAID', 2, CURRENT_TIMESTAMP),
(90004, 'OD202603190001', 54, 5402, NULL, 18800, 1800, 17000, 17000, 'COMPLETED', 'PAID', 0, DATEADD('DAY', -1, CURRENT_TIMESTAMP));

INSERT INTO da_order_item (
    id, order_id, store_id, product_id, item_name, item_code, unit_price_in_cent, quantity,
    original_amount_in_cent, discount_amount_in_cent, payable_amount_in_cent, item_status, append_round, remark
) VALUES
(91001, 90001, 2, 1169, 'Signature Milk Tea', 'signature-milk-tea', 1990, 2, 3980, 380, 3600, 'CONFIRMED', 0, 'Less sugar'),
(91002, 90001, 2, NULL, 'Wagyu Sliced Beef', 'wagyu-sliced-beef', 6200, 1, 6200, 820, 5380, 'CONFIRMED', 1, 'Append item'),
(91003, 90002, 2, NULL, 'Spicy Soup Base', 'spicy-soup-base', 6800, 1, 6800, 300, 6500, 'SERVED', 0, NULL),
(91004, 90002, 2, NULL, 'Snowflake Beef', 'snowflake-beef', 8800, 1, 8800, 300, 8500, 'SERVED', 0, NULL),
(91005, 90003, 54, 1165, 'Buffet 388', 'buffet-388', 35000, 1, 35000, 2000, 33000, 'CONFIRMED', 0, NULL),
(91006, 90003, 54, NULL, 'Premium Seafood Platter', 'premium-seafood-platter', 9000, 1, 9000, 0, 9000, 'CONFIRMED', 1, 'Append from waiter'),
(91007, 90004, 54, NULL, 'Family Set Meal', 'family-set-meal', 18800, 1, 18800, 1800, 17000, 'SERVED', 0, NULL);

INSERT INTO da_order_append_log (
    id, order_id, store_id, append_round, action_type, append_item_count, append_amount_in_cent, operate_time, operator_name, note
) VALUES
(92001, 90001, 2, 1, 'APPEND_ITEM', 1, 5380, DATEADD('MINUTE', 8, CURRENT_TIMESTAMP), 'Alice', 'Customer added beef'),
(92002, 90003, 54, 1, 'APPEND_ITEM', 1, 9000, DATEADD('MINUTE', 12, CURRENT_TIMESTAMP), 'Nok', 'Add seafood'),
(92003, 90003, 54, 2, 'ADJUST_PRICE', 0, 0, DATEADD('MINUTE', 20, CURRENT_TIMESTAMP), 'Nok', 'Kitchen out of stock compensation');

INSERT INTO da_payment_record (
    id, order_id, store_id, payment_no, payment_method, payment_channel,
    paid_amount_in_cent, change_amount_in_cent, payment_status, paid_time, cashier_name, remark
) VALUES
(93001, 90002, 2, 'PM202603200001', 'CASH', 'OFFLINE', 10000, 0, 'SUCCESS', DATEADD('MINUTE', 25, CURRENT_TIMESTAMP), 'Bob', 'first payment'),
(93002, 90002, 2, 'PM202603200002', 'CARD', 'POS', 5000, 0, 'SUCCESS', DATEADD('MINUTE', 40, CURRENT_TIMESTAMP), 'Bob', 'checkout'),
(93003, 90004, 54, 'PM202603190001', 'PROMPTPAY', 'THAI_QR', 17000, 0, 'SUCCESS', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'Mint', 'paid in full');

INSERT INTO da_user (id, username, password, display_name, enabled) VALUES
(1, 'admin', 'admin123', 'System Admin', TRUE),
(2, 'store.manager', 'store123', 'Store Manager', TRUE);

INSERT INTO da_role (id, code, name) VALUES
(1, 'SUPER_ADMIN', 'Super Admin'),
(2, 'STORE_MANAGER', 'Store Manager');

INSERT INTO da_menu (id, parent_id, name, route, permission_code, sort_order) VALUES
(1, NULL, 'Dashboard', '/dashboard', 'dashboard:view', 1),
(2, NULL, 'Store', '/stores', 'store:view', 2),
(3, NULL, 'Table', '/tables', 'table:view', 3),
(4, NULL, 'Table Area', '/table-areas', 'table.area:view', 4),
(5, NULL, 'Order', '/orders', 'order:view', 5),
(6, NULL, 'Sales Report', '/sales-reports', 'sales.report:view', 6),
(7, NULL, 'Device', '/devices', 'device:view', 7),
(8, NULL, 'Product', '/products', 'product:view', 8),
(9, NULL, 'Product Category', '/product-categories', 'product.category:view', 9),
(10, NULL, 'Member', '/members', 'member:view', 10),
(11, NULL, 'Role', '/roles', 'role:view', 11),
(12, NULL, 'User', '/users', 'user:view', 12),
(13, NULL, 'Menu', '/menus', 'menu:view', 13),
(1101, 11, 'Role Create', '', 'role:create', 111),
(1102, 11, 'Role Update', '', 'role:update', 112),
(1201, 12, 'User Create', '', 'user:create', 121),
(1202, 12, 'User Update', '', 'user:update', 122),
(1301, 13, 'Menu Create', '', 'menu:create', 131),
(1302, 13, 'Menu Update', '', 'menu:update', 132),
(1401, 2, 'Store Create', '', 'store:create', 21),
(1402, 2, 'Store Update', '', 'store:update', 22),
(1403, 2, 'Store Status', '', 'store:status', 23),
(1501, 4, 'Table Area Create', '', 'table.area:create', 41),
(1502, 4, 'Table Area Update', '', 'table.area:update', 42),
(1503, 4, 'Table Area Enable', '', 'table.area:enable', 43),
(1601, 3, 'Table Create', '', 'table:create', 31),
(1602, 3, 'Table Update', '', 'table:update', 32),
(1603, 3, 'Table Status', '', 'table:status', 33);

INSERT INTO da_user_role (id, user_id, role_id) VALUES
(1, 1, 1),
(2, 2, 2);

INSERT INTO da_role_menu (id, role_id, menu_id) VALUES
(1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4), (5, 1, 5), (6, 1, 6), (7, 1, 7),
(8, 1, 8), (9, 1, 9), (10, 1, 10), (11, 1, 11), (12, 1, 12), (13, 1, 13),
(24, 1, 1101), (25, 1, 1102), (26, 1, 1201), (27, 1, 1202), (28, 1, 1301), (29, 1, 1302),
(30, 1, 1401), (31, 1, 1402), (32, 1, 1403),
(33, 1, 1501), (34, 1, 1502), (35, 1, 1503),
(36, 1, 1601), (37, 1, 1602), (38, 1, 1603),
(14, 2, 1), (15, 2, 2), (16, 2, 3), (17, 2, 4), (18, 2, 5), (19, 2, 6),
(20, 2, 7), (21, 2, 8), (22, 2, 9), (23, 2, 10),
(39, 2, 1402), (40, 2, 1403),
(41, 2, 1501), (42, 2, 1502), (43, 2, 1503),
(44, 2, 1601), (45, 2, 1602), (46, 2, 1603);

INSERT INTO da_user_store (id, user_id, store_id) VALUES
(1, 1, 2), (2, 1, 54), (3, 2, 54);
