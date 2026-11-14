package ro.scribemed.position.service;

import ro.scribemed.position.domain.Position;
import ro.scribemed.position.dto.BuyExecutionResponse;
import ro.scribemed.position.repository.JpaPositionRepository;
import ro.scribemed.position.repository.PositionRepository;
import ro.scribemed.shared.persistence.JpaTransactionManager;

public class PositionService {
    private final JpaTransactionManager transactionManager;

    public PositionService(JpaTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

//    public BuyExecutionResponse buyPosition(Position position, int quantity) {
//        return transactionManager.execute(entityManager -> {
//            PositionRepository positionRepository =
//                    new JpaPositionRepository(entityManager);
//
//
//
//        })
//    }
}
