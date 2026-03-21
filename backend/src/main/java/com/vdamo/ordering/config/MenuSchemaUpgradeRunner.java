package com.vdamo.ordering.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MenuSchemaUpgradeRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MenuSchemaUpgradeRunner.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public MenuSchemaUpgradeRunner(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (hasMenuTypeColumn()) {
                return;
            }

            log.warn("Column da_menu.menu_type is missing. Applying compatibility upgrade.");
            jdbcTemplate.execute("ALTER TABLE da_menu ADD COLUMN menu_type VARCHAR(16) NULL COMMENT 'MENU or BUTTON' AFTER name");
            jdbcTemplate.execute("""
                    UPDATE da_menu
                    SET menu_type = CASE
                        WHEN COALESCE(route, '') = '' THEN 'BUTTON'
                        ELSE 'MENU'
                    END
                    WHERE menu_type IS NULL OR menu_type = ''
                    """);
            jdbcTemplate.execute(
                    "ALTER TABLE da_menu MODIFY COLUMN menu_type VARCHAR(16) NOT NULL COMMENT 'MENU or BUTTON'");
            log.info("Compatibility upgrade applied for da_menu.menu_type.");
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to inspect da_menu schema", ex);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to upgrade da_menu schema for menu_type", ex);
        }
    }

    private boolean hasMenuTypeColumn() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            return hasColumn(metaData, catalog, "da_menu", "menu_type")
                    || hasColumn(metaData, catalog, "DA_MENU", "MENU_TYPE");
        }
    }

    private boolean hasColumn(DatabaseMetaData metaData, String catalog, String tableName, String columnName)
            throws SQLException {
        try (ResultSet columns = metaData.getColumns(catalog, null, tableName, columnName)) {
            return columns.next();
        }
    }
}
