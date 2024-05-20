package io.spring.billrun.configuration;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlyWayConfiguration {



    @Value("${spring.flyway.url}")
    private String url;

    @Value("${spring.flyway.user}")
    private String user;

    @Value("${spring.flyway.password}")
    private String password;

    @Bean
    public FlywayMigrationInitializer flywayMigrationInitializer(
            Flyway flyway
            , ObjectProvider<FlywayMigrationStrategy> migrationStrategies) {
        return new FlywayMigrationInitializer(flyway, migrationStrategies.getIfAvailable());
    }

    @Bean
    public Flyway flyway(){
        return Flyway.configure().dataSource(url, user,password).load();
    }

}
