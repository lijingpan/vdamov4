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

INSERT INTO da_table (id, store_id, area_name, table_name, capacity, status) VALUES
(2101, 2, 'Hall A', 'A01', 4, 'IDLE'),
(2102, 2, 'Hall A', 'A02', 6, 'IN_USE'),
(2103, 2, 'Hall B', 'B01', 8, 'WAITING_CHECKOUT'),
(5401, 54, 'Main Hall', 'T01', 4, 'IN_USE'),
(5402, 54, 'Main Hall', 'T02', 4, 'IDLE'),
(5403, 54, 'VIP', 'V01', 10, 'DISABLED');

INSERT INTO da_order (
    id, order_no, store_id, table_id, member_id,
    original_amount_in_cent, discount_amount_in_cent, payable_amount_in_cent, paid_amount_in_cent,
    order_status, payment_status, append_count, create_time
) VALUES
(90001, 'OD202603200001', 2, 2102, 362220, 9800, 1200, 8600, 0, 'IN_PROGRESS', 'UNPAID', 1, CURRENT_TIMESTAMP),
(90002, 'OD202603200002', 2, 2103, NULL, 15600, 600, 15000, 15000, 'WAITING_CHECKOUT', 'PARTIAL', 0, CURRENT_TIMESTAMP),
(90003, 'OD202603200003', 54, 5401, 362223, 42000, 2000, 40000, 0, 'IN_PROGRESS', 'UNPAID', 2, CURRENT_TIMESTAMP),
(90004, 'OD202603190001', 54, 5402, NULL, 18800, 1800, 17000, 17000, 'COMPLETED', 'PAID', 0, DATEADD('DAY', -1, CURRENT_TIMESTAMP));

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
(4, NULL, 'Order', '/orders', 'order:view', 4),
(5, NULL, 'Device', '/devices', 'device:view', 5),
(6, NULL, 'Product', '/products', 'product:view', 6),
(7, NULL, 'Member', '/members', 'member:view', 7),
(8, NULL, 'Role', '/roles', 'role:view', 8),
(9, NULL, 'User', '/users', 'user:view', 9),
(10, NULL, 'Menu', '/menus', 'menu:view', 10);

INSERT INTO da_user_role (id, user_id, role_id) VALUES
(1, 1, 1),
(2, 2, 2);

INSERT INTO da_role_menu (id, role_id, menu_id) VALUES
(1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4), (5, 1, 5), (6, 1, 6), (7, 1, 7), (8, 1, 8), (9, 1, 9), (10, 1, 10),
(11, 2, 1), (12, 2, 2), (13, 2, 3), (14, 2, 4), (15, 2, 5), (16, 2, 6), (17, 2, 7);

INSERT INTO da_user_store (id, user_id, store_id) VALUES
(1, 1, 2), (2, 1, 54), (3, 2, 54);
