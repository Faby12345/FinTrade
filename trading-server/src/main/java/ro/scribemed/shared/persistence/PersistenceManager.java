package ro.scribemed.shared.persistence;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import ro.scribemed.shared.persistence.config.DatabaseConfig;

import java.util.HashMap;
import java.util.Map;

public final class PersistenceManager implements AutoCloseable {

    private final EntityManagerFactory entityManagerFactory;

    public PersistenceManager(
            String persistenceUnitName,
            DatabaseConfig databaseConfig
    ) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(
                "jakarta.persistence.jdbc.url",
                databaseConfig.jdbcUrl()
        );

        properties.put(
                "jakarta.persistence.jdbc.user",
                databaseConfig.username()
        );

        properties.put(
                "jakarta.persistence.jdbc.password",
                databaseConfig.password()
        );

        this.entityManagerFactory =
                Persistence.createEntityManagerFactory(
                        persistenceUnitName,
                        properties
                );
    }

    public EntityManagerFactory entityManagerFactory() {
        return entityManagerFactory;
    }

    @Override
    public void close() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}