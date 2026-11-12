package ro.scribemed.stock.service;

import ro.scribemed.shared.persistence.JpaTransactionManager;
import ro.scribemed.stock.domain.Stock;
import ro.scribemed.stock.repository.JpaStockRepository;
import ro.scribemed.stock.repository.StockRepository;

import java.util.List;

public class StockService {
    private final JpaTransactionManager transactionManager;

    public StockService(JpaTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public List<Stock> fetchAll(){
        return transactionManager.execute(entityManager -> {
            StockRepository stockRepository =
                    new JpaStockRepository(entityManager);

            return stockRepository.findAll();
        });
    }

}
