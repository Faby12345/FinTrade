package ro.scribemed.position.dto;

import ro.scribemed.position.domain.Position;
import ro.scribemed.stock.domain.Stock;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record PositionResponse(
        UUID positionId,
        UUID stockId,
        String symbol,
        String stockName,
        long quantity,
        BigDecimal averagePurchasePrice,
        BigDecimal currentPrice,
        BigDecimal costBasis,
        BigDecimal marketValue,
        BigDecimal unrealizedProfitLoss
) {

    public static PositionResponse from(Position position) {
        Objects.requireNonNull(position);

        Stock stock = position.getStock();
        long quantity = position.getQuantity();
        BigDecimal quantityValue = BigDecimal.valueOf(quantity);
        BigDecimal averagePurchasePrice =
                position.getAveragePurchasePrice();
        BigDecimal currentPrice = stock.getPrice();
        BigDecimal costBasis = averagePurchasePrice.multiply(quantityValue);
        BigDecimal marketValue = currentPrice.multiply(quantityValue);

        return new PositionResponse(
                position.getId(),
                stock.getId(),
                stock.getSymbol(),
                stock.getName(),
                quantity,
                averagePurchasePrice,
                currentPrice,
                costBasis,
                marketValue,
                marketValue.subtract(costBasis)
        );
    }
}
