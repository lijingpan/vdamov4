INSERT INTO da_menu (id, parent_id, name, menu_type, route, permission_code, sort_order, creator, updater)
SELECT 1404, 2, 'Store Delete', 'BUTTON', '', 'store:delete', 24, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM da_menu WHERE id = 1404 OR permission_code = 'store:delete');

INSERT INTO da_menu (id, parent_id, name, menu_type, route, permission_code, sort_order, creator, updater)
SELECT 1504, 4, 'Table Area Delete', 'BUTTON', '', 'table.area:delete', 44, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM da_menu WHERE id = 1504 OR permission_code = 'table.area:delete');

INSERT INTO da_menu (id, parent_id, name, menu_type, route, permission_code, sort_order, creator, updater)
SELECT 1604, 3, 'Table Delete', 'BUTTON', '', 'table:delete', 34, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM da_menu WHERE id = 1604 OR permission_code = 'table:delete');

INSERT INTO da_menu (id, parent_id, name, menu_type, route, permission_code, sort_order, creator, updater)
SELECT 1704, 8, 'Product Delete', 'BUTTON', '', 'product:delete', 84, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM da_menu WHERE id = 1704 OR permission_code = 'product:delete');

INSERT INTO da_menu (id, parent_id, name, menu_type, route, permission_code, sort_order, creator, updater)
SELECT 1804, 9, 'Product Category Delete', 'BUTTON', '', 'product.category:delete', 94, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM da_menu WHERE id = 1804 OR permission_code = 'product.category:delete');

INSERT INTO da_menu (id, parent_id, name, menu_type, route, permission_code, sort_order, creator, updater)
SELECT 1904, 7, 'Device Delete', 'BUTTON', '', 'device:delete', 74, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM da_menu WHERE id = 1904 OR permission_code = 'device:delete');

INSERT INTO da_menu (id, parent_id, name, menu_type, route, permission_code, sort_order, creator, updater)
SELECT 2003, 10, 'Member Delete', 'BUTTON', '', 'member:delete', 103, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM da_menu WHERE id = 2003 OR permission_code = 'member:delete');

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 80, role.id, 1404, 'system', 'system'
FROM da_role role
WHERE role.code = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 80 OR (role_id = role.id AND menu_id = 1404));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 81, role.id, 1504, 'system', 'system'
FROM da_role role
WHERE role.code = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 81 OR (role_id = role.id AND menu_id = 1504));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 82, role.id, 1604, 'system', 'system'
FROM da_role role
WHERE role.code = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 82 OR (role_id = role.id AND menu_id = 1604));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 83, role.id, 1704, 'system', 'system'
FROM da_role role
WHERE role.code = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 83 OR (role_id = role.id AND menu_id = 1704));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 84, role.id, 1804, 'system', 'system'
FROM da_role role
WHERE role.code = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 84 OR (role_id = role.id AND menu_id = 1804));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 85, role.id, 1904, 'system', 'system'
FROM da_role role
WHERE role.code = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 85 OR (role_id = role.id AND menu_id = 1904));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 86, role.id, 2003, 'system', 'system'
FROM da_role role
WHERE role.code = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 86 OR (role_id = role.id AND menu_id = 2003));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 87, role.id, 1504, 'system', 'system'
FROM da_role role
WHERE role.code = 'STORE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 87 OR (role_id = role.id AND menu_id = 1504));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 88, role.id, 1604, 'system', 'system'
FROM da_role role
WHERE role.code = 'STORE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 88 OR (role_id = role.id AND menu_id = 1604));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 89, role.id, 1704, 'system', 'system'
FROM da_role role
WHERE role.code = 'STORE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 89 OR (role_id = role.id AND menu_id = 1704));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 90, role.id, 1804, 'system', 'system'
FROM da_role role
WHERE role.code = 'STORE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 90 OR (role_id = role.id AND menu_id = 1804));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 91, role.id, 1904, 'system', 'system'
FROM da_role role
WHERE role.code = 'STORE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 91 OR (role_id = role.id AND menu_id = 1904));

INSERT INTO da_role_menu (id, role_id, menu_id, creator, updater)
SELECT 92, role.id, 2003, 'system', 'system'
FROM da_role role
WHERE role.code = 'STORE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM da_role_menu WHERE id = 92 OR (role_id = role.id AND menu_id = 2003));
