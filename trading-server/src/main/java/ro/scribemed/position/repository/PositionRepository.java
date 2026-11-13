package ro.scribemed.position.repository;

import ro.scribemed.position.domain.Position;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionRepository {

    void add(Position position);

    Optional<Position> findById(UUID id);

    Optional<Position> findByTradingAccountIdAndStockId(
            UUID tradingAccountId,
            UUID stockId
    );

    Optional<Position> findByTradingAccountIdAndStockIdForUpdate(
            UUID tradingAccountId,
            UUID stockId
    );

    List<Position> findAllByTradingAccountId(UUID tradingAccountId);
}
