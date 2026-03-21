ALTER TABLE da_menu
    ADD COLUMN menu_type VARCHAR(16) NULL COMMENT 'MENU or BUTTON' AFTER name;

UPDATE da_menu
SET menu_type = CASE
    WHEN route IS NOT NULL AND route <> '' THEN 'MENU'
    ELSE 'BUTTON'
END
WHERE menu_type IS NULL OR menu_type = '';

ALTER TABLE da_menu
    MODIFY COLUMN menu_type VARCHAR(16) NOT NULL COMMENT 'MENU or BUTTON';

