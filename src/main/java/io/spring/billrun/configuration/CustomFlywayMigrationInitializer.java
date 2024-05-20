package io.spring.billrun.configuration;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;

public class CustomFlywayMigrationInitializer  extends FlywayMigrationInitializer {

    public CustomFlywayMigrationInitializer(Flyway flyway, FlywayMigrationStrategy migrationStrategy) {
        super(flyway, migrationStrategy);
    }
}
