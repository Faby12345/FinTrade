package ro.scribemed.user.repository;

import jakarta.persistence.EntityManager;
import ro.scribemed.user.domain.User;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class JpaUserRepository
        implements UserRepository {

    private final EntityManager entityManager;

    public JpaUserRepository(EntityManager entityManager) {
        this.entityManager =
                Objects.requireNonNull(entityManager);
    }

    @Override
    public void add(User user) {
        entityManager.persist(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        User user = entityManager.find(User.class, id);

        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return entityManager.createQuery("""
                select u
                from User u
                where u.username = :username
                """, User.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return entityManager.createQuery("""
                select u
                from User u
                where u.email = :email
                """, User.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = entityManager.createQuery("""
                select count(u)
                from User u
                where u.username = :username
                """, Long.class)
                .setParameter("username", username)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Long count = entityManager.createQuery("""
                select count(u)
                from User u
                where u.email = :email
                """, Long.class)
                .setParameter("email", email)
                .getSingleResult();

        return count > 0;
    }
}