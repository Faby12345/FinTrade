package ro.scribemed.tradingAccount.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import ro.scribemed.tradingAccount.domain.TradingAccount;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class JpaTradingAccountRepository
        implements TradingAccountRepository {

    private final EntityManager entityManager;

    public JpaTradingAccountRepository(
            EntityManager entityManager
    ) {
        this.entityManager =
                Objects.requireNonNull(entityManager);
    }

    @Override
    public TradingAccount add(TradingAccount account) {
        Objects.requireNonNull(account);

        entityManager.persist(account);
        return account;
    }

    @Override
    public Optional<TradingAccount> findById(UUID id) {
        Objects.requireNonNull(id);

        TradingAccount account =
                entityManager.find(TradingAccount.class, id);

        return Optional.ofNullable(account);
    }

    @Override
    public Optional<TradingAccount> findByUserId(
            UUID userId
    ) {
        Objects.requireNonNull(userId);

        return entityManager.createQuery("""
                select account
                from TradingAccount account
                where account.user.id = :userId
                """, TradingAccount.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        Objects.requireNonNull(userId);

        Long count = entityManager.createQuery("""
                select count(account)
                from TradingAccount account
                where account.user.id = :userId
                """, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public Optional<TradingAccount> findByIdForUpdate(
            UUID id
    ) {
        Objects.requireNonNull(id);

        TradingAccount account = entityManager.find(
                TradingAccount.class,
                id,
                LockModeType.PESSIMISTIC_WRITE
        );

        return Optional.ofNullable(account);
    }
}