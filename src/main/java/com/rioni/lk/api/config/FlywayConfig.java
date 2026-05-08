package com.rioni.lk.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    @Value("${spring.flyway.default-schema}")
    private String schema;

    @Bean
    public CommandLineRunner flywayMigrate(DataSource dataSource) {
        return args -> {
            logger.info("=== Flyway migration starting ===");
            logger.info("DataSource: {}", dataSource.getConnection().getMetaData().getURL());
            logger.info("Schema: {}", schema);

            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .schemas(schema)
                    .defaultSchema(schema)
                    .load();

            logger.info("Pending migrations: {}", flyway.info().pending().length);
            logger.info("Applied migrations: {}", flyway.info().applied().length);

            logger.info("Executing migrate()...");
            var result = flyway.migrate();
            logger.info("Migrate() result - migrationsExecuted: {}", result.migrationsExecuted);
            logger.info("=== Flyway migration completed ===");
        };
    }
}