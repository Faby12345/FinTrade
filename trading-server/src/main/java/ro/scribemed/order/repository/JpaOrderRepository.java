package ro.scribemed.order.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import ro.scribemed.order.domain.OrderStatus;
import ro.scribemed.order.domain.TradeOrder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class JpaOrderRepository implements OrderRepository {

    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.OPEN,
            OrderStatus.PARTIALLY_FILLED
    );

    private final EntityManager entityManager;

    public JpaOrderRepository(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager);
    }

    @Override
    public void add(TradeOrder order) {
        entityManager.persist(Objects.requireNonNull(order));
    }

    @Override
    public Optional<TradeOrder> findById(UUID id) {
        Objects.requireNonNull(id);

        return Optional.ofNullable(
                entityManager.find(TradeOrder.class, id)
        );
    }

    @Override
    public Optional<TradeOrder> findByIdForUpdate(UUID id) {
        Objects.requireNonNull(id);

        return Optional.ofNullable(
                entityManager.find(
                        TradeOrder.class,
                        id,
                        LockModeType.PESSIMISTIC_WRITE
                )
        );
    }

    @Override
    public List<TradeOrder> findAllActive() {
        return entityManager.createQuery("""
                select tradeOrder
                from TradeOrder tradeOrder
                join fetch tradeOrder.tradingAccount
                join fetch tradeOrder.stock
                where tradeOrder.status in :activeStatuses
                order by tradeOrder.sequenceNumber
                """, TradeOrder.class)
                .setParameter("activeStatuses", ACTIVE_STATUSES)
                .getResultList();
    }

    @Override
    public List<TradeOrder> findAllActiveByStockId(UUID stockId) {
        Objects.requireNonNull(stockId);

        return entityManager.createQuery("""
                select tradeOrder
                from TradeOrder tradeOrder
                join fetch tradeOrder.tradingAccount
                join fetch tradeOrder.stock stock
                where stock.id = :stockId
                  and tradeOrder.status in :activeStatuses
                order by tradeOrder.sequenceNumber
                """, TradeOrder.class)
                .setParameter("stockId", stockId)
                .setParameter("activeStatuses", ACTIVE_STATUSES)
                .getResultList();
    }

    @Override
    public List<TradeOrder> findAllByTradingAccountId(
            UUID tradingAccountId
    ) {
        Objects.requireNonNull(tradingAccountId);

        return entityManager.createQuery("""
                select tradeOrder
                from TradeOrder tradeOrder
                join fetch tradeOrder.stock
                where tradeOrder.tradingAccount.id = :tradingAccountId
                order by tradeOrder.sequenceNumber desc
                """, TradeOrder.class)
                .setParameter(
                        "tradingAccountId",
                        tradingAccountId
                )
                .getResultList();
    }
}
