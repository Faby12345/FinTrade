package ro.scribemed.position.dto;

import ro.scribemed.position.domain.Position;
import ro.scribemed.stock.domain.Stock;
import ro.scribemed.tradingAccount.domain.TradingAccount;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record BuyExecutionResponse(
        UUID positionId,
        UUID stockId,
        String symbol,
        long purchasedQuantity,
        BigDecimal executionPrice,
        BigDecimal totalCost,
        long positionQuantity,
        BigDecimal averagePurchasePrice,
        BigDecimal cashBalance,
        BigDecimal availableCash
) {

    public static BuyExecutionResponse from(
            Position position,
            long purchasedQuantity
    ) {
        Objects.requireNonNull(position);

        if (purchasedQuantity <= 0) {
            throw new IllegalArgumentException(
                    "Purchased quantity must be greater than zero"
            );
        }

        Stock stock = position.getStock();
        TradingAccount tradingAccount = position.getTradingAccount();
        BigDecimal executionPrice = stock.getPrice();
        BigDecimal cashBalance = tradingAccount.getCashBalance();
        BigDecimal reservedCash = tradingAccount.getReservedCash();

        return new BuyExecutionResponse(
                position.getId(),
                stock.getId(),
                stock.getSymbol(),
                purchasedQuantity,
                executionPrice,
                executionPrice.multiply(
                        BigDecimal.valueOf(purchasedQuantity)
                ),
                position.getQuantity(),
                position.getAveragePurchasePrice(),
                cashBalance,
                cashBalance.subtract(reservedCash)
        );
    }
}
