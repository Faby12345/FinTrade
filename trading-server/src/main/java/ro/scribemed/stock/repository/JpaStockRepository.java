package ro.scribemed.stock.repository;

import jakarta.persistence.EntityManager;
import ro.scribemed.stock.domain.Stock;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class JpaStockRepository implements StockRepository {

    private final EntityManager entityManager;

    public JpaStockRepository(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager);
    }

    @Override
    public void add(Stock stock) {
        entityManager.persist(Objects.requireNonNull(stock));
    }

    @Override
    public Optional<Stock> findById(UUID id) {
        Objects.requireNonNull(id);

        return Optional.ofNullable(
                entityManager.find(Stock.class, id)
        );
    }

    @Override
    public Optional<Stock> findBySymbol(String symbol) {
        Objects.requireNonNull(symbol);

        return entityManager.createQuery("""
                select stock
                from Stock stock
                where stock.symbol = :symbol
                """, Stock.class)
                .setParameter("symbol", symbol)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Stock> findAll() {
        return entityManager.createQuery("""
                select stock
                from Stock stock
                order by stock.symbol
                """, Stock.class)
                .getResultList();
    }

    @Override
    public boolean existsBySymbol(String symbol) {
        Objects.requireNonNull(symbol);

        Long count = entityManager.createQuery("""
                select count(stock)
                from Stock stock
                where stock.symbol = :symbol
                """, Long.class)
                .setParameter("symbol", symbol)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public long count() {
        return entityManager.createQuery("""
                select count(stock)
                from Stock stock
                """, Long.class)
                .getSingleResult();
    }
}
