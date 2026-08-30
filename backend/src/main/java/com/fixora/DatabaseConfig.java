package com.fixora;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl == null || databaseUrl.isBlank()) {
            databaseUrl = System.getProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/fixora_db");
        }

        if (databaseUrl.startsWith("jdbc:postgresql://") && !databaseUrl.contains("@")) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(databaseUrl);
            config.setDriverClassName("org.postgresql.Driver");
            return new HikariDataSource(config);
        }

        String cleanUrl = databaseUrl;
        if (cleanUrl.startsWith("jdbc:")) {
            cleanUrl = cleanUrl.substring(5);
        }

        URI dbUri = new URI(cleanUrl);

        String username = null;
        String password = null;
        if (dbUri.getUserInfo() != null) {
            String[] userInfo = dbUri.getUserInfo().split(":");
            username = userInfo[0];
            password = userInfo.length > 1 ? userInfo[1] : "";
        }

        int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
        String host = dbUri.getHost();
        String path = dbUri.getPath();

        String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s?sslmode=require", host, port, path);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        if (username != null) config.setUsername(username);
        if (password != null) config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(config);
    }
}