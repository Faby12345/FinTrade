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
        BigDecimal cashBalance = tradingAccount.getCashBalance();
        BigDecimal reservedCash = tradingAccount.getReservedCash();

        return new TradingAccountResponse(
                tradingAccount.getId(),
                cashBalance,
                reservedCash,
                cashBalance.subtract(reservedCash)
        );
    }
}
