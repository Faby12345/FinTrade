package ro.scribemed.stock.repository;

import ro.scribemed.stock.domain.Stock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository {

    void add(Stock stock);

    Optional<Stock> findById(UUID id);

    Optional<Stock> findBySymbol(String symbol);

    List<Stock> findAll();

    boolean existsBySymbol(String symbol);

    long count();
}
