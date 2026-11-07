package ro.scribemed.tradingAccount.repository;



import ro.scribemed.tradingAccount.domain.TradingAccount;

import java.util.Optional;
import java.util.UUID;

public interface TradingAccountRepository {

    void add(TradingAccount account);

    Optional<TradingAccount> findById(UUID id);

    Optional<TradingAccount> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    Optional<TradingAccount> findByIdForUpdate(UUID id);
}