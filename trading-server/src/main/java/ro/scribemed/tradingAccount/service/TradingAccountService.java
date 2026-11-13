package ro.scribemed.tradingAccount.service;

import jakarta.persistence.EntityNotFoundException;
import ro.scribemed.shared.persistence.JpaTransactionManager;
import ro.scribemed.tradingAccount.domain.TradingAccount;
import ro.scribemed.tradingAccount.repository.JpaTradingAccountRepository;
import ro.scribemed.tradingAccount.repository.TradingAccountRepository;
import ro.scribemed.user.domain.User;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class TradingAccountService {

    private final JpaTransactionManager transactionManager;
    private final BigDecimal DEFAULT_START_BALANCE = BigDecimal.valueOf(1000);

    public TradingAccountService(JpaTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public TradingAccount getAccount(UUID userId){
        return transactionManager.execute(entityManager -> {
            TradingAccountRepository tradingAccountRepository =
                    new JpaTradingAccountRepository(entityManager);

            Optional<TradingAccount> tradingAccount =
                    tradingAccountRepository.findByUserId(userId);

            if(tradingAccount.isEmpty()){
                throw new EntityNotFoundException
                        ("Trading account for user id: " + userId + " ,not exists");
            }

            return tradingAccount.get();
        });
    }


}
