package ro.scribemed.tradingAccount.service;

import ro.scribemed.shared.persistence.JpaTransactionManager;
import ro.scribemed.tradingAccount.domain.TradingAccount;
import ro.scribemed.tradingAccount.repository.JpaTradingAccountRepository;
import ro.scribemed.tradingAccount.repository.TradingAccountRepository;
import ro.scribemed.user.domain.User;

import java.math.BigDecimal;

public class TradingAccountService {

    private final JpaTransactionManager transactionManager;
    private final BigDecimal DEFAULT_START_BALANCE = BigDecimal.valueOf(1000);

    public TradingAccountService(JpaTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }


}
