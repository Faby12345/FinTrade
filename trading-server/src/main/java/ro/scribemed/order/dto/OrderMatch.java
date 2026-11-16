package ro.scribemed.order.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record OrderMatch(
        UUID stockId,
        UUID buyOrderId,
        UUID sellOrderId,
        UUID buyerAccountId,
        UUID sellerAccountId,
        long quantity,
        BigDecimal executionPrice
) {
    public OrderMatch {
        Objects.requireNonNull(stockId);
        Objects.requireNonNull(buyOrderId);
        Objects.requireNonNull(sellOrderId);
        Objects.requireNonNull(buyerAccountId);
        Objects.requireNonNull(sellerAccountId);
        Objects.requireNonNull(executionPrice);

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Matched quantity must be greater than zero"
            );
        }

        if (executionPrice.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Execution price must be greater than zero"
            );
        }
    }
}
