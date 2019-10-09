package stubidp.stubidp.repositories.jdbc.migrations;

import org.flywaydb.core.Flyway;

public class DatabaseMigrationRunner {

    public void runMigration(String dbUrl) {
        Flyway.configure()
                .dataSource(dbUrl, null, null)
                .baselineVersion("0")
                .baselineOnMigrate(true)
                .locations("classpath:db.migrations.common", getDBSpecificMigration(dbUrl))
                .load()
                .migrate();
    }

    private String getDBSpecificMigration(String dbUrl) {
        return dbUrl.contains(":h2:") ? "classpath:db.migrations.h2" : "classpath:db.migrations.postgres";
    }
}
