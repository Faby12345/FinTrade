package ro.scribemed.order.dto;

import java.math.BigDecimal;
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
}