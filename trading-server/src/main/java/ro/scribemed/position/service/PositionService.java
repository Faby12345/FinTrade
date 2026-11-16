package ro.scribemed.position.service;

import jakarta.persistence.EntityNotFoundException;
import ro.scribemed.position.dto.PositionResponse;
import ro.scribemed.position.repository.JpaPositionRepository;
import ro.scribemed.position.repository.PositionRepository;
import ro.scribemed.shared.persistence.JpaTransactionManager;
import ro.scribemed.tradingAccount.domain.TradingAccount;
import ro.scribemed.tradingAccount.repository.JpaTradingAccountRepository;
import ro.scribemed.tradingAccount.repository.TradingAccountRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PositionService {
    private final JpaTransactionManager transactionManager;

    public PositionService(JpaTransactionManager transactionManager) {
        this.transactionManager = Objects.requireNonNull(transactionManager);
    }

    public List<PositionResponse> getPositionsForUser(UUID userId) {
        Objects.requireNonNull(userId);

        return transactionManager.execute(entityManager -> {
            TradingAccountRepository accountRepository =
                    new JpaTradingAccountRepository(entityManager);
            PositionRepository positionRepository =
                    new JpaPositionRepository(entityManager);

            TradingAccount account = accountRepository
                    .findByUserId(userId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Trading account was not found"
                    ));

            return positionRepository
                    .findAllByTradingAccountId(account.getId())
                    .stream()
                    .map(PositionResponse::from)
                    .toList();
        });
    }
}
