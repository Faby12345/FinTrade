package ro.scribemed.trade.repository;

import ro.scribemed.trade.domain.Trade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeRepository {

    Trade add(Trade trade);

    Optional<Trade> findById(UUID id);

    List<Trade> findAllByTradingAccountId(UUID tradingAccountId);
}
