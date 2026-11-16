package ro.scribemed.trade.repository;

import jakarta.persistence.EntityManager;
import ro.scribemed.trade.domain.Trade;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class JpaTradeRepository implements TradeRepository {

    private final EntityManager entityManager;

    public JpaTradeRepository(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager);
    }

    @Override
    public Trade add(Trade trade) {
        Trade persistedTrade = Objects.requireNonNull(trade);
        entityManager.persist(persistedTrade);
        return persistedTrade;
    }

    @Override
    public Optional<Trade> findById(UUID id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(entityManager.find(Trade.class, id));
    }

    @Override
    public List<Trade> findAllByTradingAccountId(
            UUID tradingAccountId
    ) {
        Objects.requireNonNull(tradingAccountId);

        return entityManager.createQuery("""
                select trade
                from Trade trade
                join fetch trade.stock
                where trade.buyerAccount.id = :tradingAccountId
                   or trade.sellerAccount.id = :tradingAccountId
                order by trade.createdAt desc
                """, Trade.class)
                .setParameter("tradingAccountId", tradingAccountId)
                .getResultList();
    }
}
