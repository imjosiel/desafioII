package br.com.erp.desafioII;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;

@Testcontainers
public abstract class TestContainersConfig {
    static PostgreSQLContainer<?> postgres;
    static EmbeddedPostgres embedded;

    @BeforeAll
    static void start() {
        try {
            postgres = new PostgreSQLContainer<>("postgres:16-alpine");
            postgres.start();
        } catch (Throwable t) {
            try {
                embedded = EmbeddedPostgres.builder().start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        if (postgres != null) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
        } else {
            registry.add("spring.datasource.url", () -> embedded.getJdbcUrl("postgres", "postgres"));
            registry.add("spring.datasource.username", () -> "postgres");
            registry.add("spring.datasource.password", () -> "postgres");
        }
    }
}