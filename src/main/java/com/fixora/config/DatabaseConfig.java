package com.fixora.config;

import com.zaxxeon.hikari.HikariConfig;
import com.zaxxeon.hikari.HikariDataSource;
import org.slf.Logger;
import org.slf.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Value("${spring.datasource.url:}")
    private String fallbackUrl;

    @Value("${spring.datasource.username:}")
    private String fallbackUsername;

    @Value("${spring.datasource.password:}")
    private String fallbackPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        if (databaseUrl != null && !databaseUrl.trim().isEmpty()) {
            log.info("Parsing DATABASE_URL environment variable for Render deployment.");
            try {
                String cleanUrl = databaseUrl.trim();
                if (cleanUrl.startsWith("postgres://")) {
                    cleanUrl = "postgresql://" + cleanUrl.substring("postgres://".length());
                }
                
                URI uri = new URI(cleanUrl);
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                String dbName = (path != null && path.length() > 1) ? path.substring(1) : "";

                String username = "";
                String password = "";
                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":");
                    username = userInfo[0];
                    if (userInfo.length > 1) {
                        password = userInfo[1];
                    }
                }

                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
                log.info("Successfully resolved JDBC URL for Host: {}, Port: {}, DB: {}", host, port, dbName);

                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("org.postgresql.Driver");
                config.setMaximumPoolSize(5);
                config.setMinimumIdle(1);
                config.setIdleTimeout(300000);
                config.setMaxLifetime(600000);
                config.setConnectionTimeout(20000);
                config.setInitializationFailTimeout(0);
                config.setPoolName("FixoraHikariCP-Render");

                return new HikariDataSource(config);
            } catch (URISyntaxException e) {
                log.error("Failed to parse DATABASE_URL URI. Fallback to default properties.", e);
            }
        }

        log.info("Using standard Spring Datasource properties configuration.");
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(fallbackUrl)
                .username(fallbackUsername)
                .password(fallbackPassword)
                .build();
    }
}
