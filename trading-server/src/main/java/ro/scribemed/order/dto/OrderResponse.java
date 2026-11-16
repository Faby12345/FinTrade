package ro.scribemed.order.dto;

import ro.scribemed.order.domain.OrderSide;
import ro.scribemed.order.domain.OrderStatus;
import ro.scribemed.order.domain.TradeOrder;
import ro.scribemed.stock.domain.Stock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID stockId,
        String symbol,
        OrderSide side,
        long originalQuantity,
        long filledQuantity,
        long remainingQuantity,
        BigDecimal limitPrice,
        OrderStatus status,
        Instant createdAt
) {

    public static OrderResponse from(TradeOrder order) {
        Objects.requireNonNull(order);

        Stock stock = order.getStock();
        long originalQuantity = order.getOriginalQuantity();
        long remainingQuantity = order.getRemainingQuantity();

        return new OrderResponse(
                order.getId(),
                stock.getId(),
                stock.getSymbol(),
                order.getSide(),
                originalQuantity,
                originalQuantity - remainingQuantity,
                remainingQuantity,
                order.getLimitPrice(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
