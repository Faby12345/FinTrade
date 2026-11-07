package ro.scribemed.shared.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.function.Consumer;
import java.util.function.Function;

public final class JpaTransactionManager {

    private final EntityManagerFactory entityManagerFactory;

    public JpaTransactionManager(
            EntityManagerFactory entityManagerFactory
    ) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public <T> T execute(
            Function<EntityManager, T> operation
    ) {
        try (EntityManager entityManager =
                     entityManagerFactory.createEntityManager()) {

            EntityTransaction transaction =
                    entityManager.getTransaction();

            try {
                transaction.begin();

                T result = operation.apply(entityManager);

                transaction.commit();

                return result;
            } catch (RuntimeException exception) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                throw exception;
            }
        }
    }

    public void execute(
            Consumer<EntityManager> operation
    ) {
        execute(entityManager -> {
            operation.accept(entityManager);
            return null;
        });
    }
}