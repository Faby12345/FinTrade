package ro.scribemed.position.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import ro.scribemed.position.domain.Position;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class JpaPositionRepository implements PositionRepository {

    private final EntityManager entityManager;

    public JpaPositionRepository(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager);
    }

    @Override
    public void add(Position position) {
        entityManager.persist(Objects.requireNonNull(position));
    }

    @Override
    public Optional<Position> findById(UUID id) {
        Objects.requireNonNull(id);

        return Optional.ofNullable(
                entityManager.find(Position.class, id)
        );
    }

    @Override
    public Optional<Position> findByTradingAccountIdAndStockId(
            UUID tradingAccountId,
            UUID stockId
    ) {
        return findByTradingAccountIdAndStockIdQuery(
                tradingAccountId,
                stockId,
                null
        );
    }

    @Override
    public Optional<Position> findByTradingAccountIdAndStockIdForUpdate(
            UUID tradingAccountId,
            UUID stockId
    ) {
        return findByTradingAccountIdAndStockIdQuery(
                tradingAccountId,
                stockId,
                LockModeType.PESSIMISTIC_WRITE
        );
    }

    @Override
    public List<Position> findAllByTradingAccountId(
            UUID tradingAccountId
    ) {
        Objects.requireNonNull(tradingAccountId);

        return entityManager.createQuery("""
                select position
                from Position position
                join fetch position.stock stock
                where position.tradingAccount.id = :tradingAccountId
                order by stock.symbol
                """, Position.class)
                .setParameter(
                        "tradingAccountId",
                        tradingAccountId
                )
                .getResultList();
    }

    private Optional<Position> findByTradingAccountIdAndStockIdQuery(
            UUID tradingAccountId,
            UUID stockId,
            LockModeType lockMode
    ) {
        Objects.requireNonNull(tradingAccountId);
        Objects.requireNonNull(stockId);

        var query = entityManager.createQuery("""
                select position
                from Position position
                where position.tradingAccount.id = :tradingAccountId
                  and position.stock.id = :stockId
                """, Position.class)
                .setParameter(
                        "tradingAccountId",
                        tradingAccountId
                )
                .setParameter("stockId", stockId)
                .setMaxResults(1);

        if (lockMode != null) {
            query.setLockMode(lockMode);
        }

        return query.getResultStream().findFirst();
    }
}
