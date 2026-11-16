package ro.scribemed.tradingAccount.dto;

import ro.scribemed.tradingAccount.domain.TradingAccount;

import java.math.BigDecimal;
import java.util.UUID;

public record TradingAccountResponse(
        UUID accountId,
        BigDecimal cashBalance,
        BigDecimal reservedCash,
        BigDecimal availableCash
) {

    public static TradingAccountResponse from(
            TradingAccount tradingAccount
    ) {
        return new TradingAccountResponse(
                tradingAccount.getId(),
                tradingAccount.getCashBalance(),
                tradingAccount.getReservedCash(),
                tradingAccount.getAvailableCash()
        );
    }
}
