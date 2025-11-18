package br.com.erp.desafioII.config;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class EmbeddedPostgresConfig {
    private EmbeddedPostgres pg;

    @Bean
    @Primary
    public DataSource dataSource() {
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        if (envUrl != null && !envUrl.isBlank()) {
            ds.setUrl(envUrl);
            ds.setUsername(envUser);
            ds.setPassword(envPass);
            return ds;
        }
        try {
            pg = EmbeddedPostgres.builder().start();
            ds.setUrl(pg.getJdbcUrl("postgres", "postgres"));
            ds.setUsername("postgres");
            ds.setPassword("postgres");
            return ds;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void shutdown() throws Exception {
        if (pg != null) {
            pg.close();
        }
    }
}