package ro.scribemed.order.repository;

import ro.scribemed.order.domain.TradeOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    TradeOrder add(TradeOrder order);

    Optional<TradeOrder> findById(UUID id);

    Optional<TradeOrder> findByIdForUpdate(UUID id);

    List<TradeOrder> findAllActive();

    List<TradeOrder> findAllActiveByStockId(UUID stockId);

    List<TradeOrder> findAllByTradingAccountId(UUID tradingAccountId);

    long findMaxSequenceNumber();
}
