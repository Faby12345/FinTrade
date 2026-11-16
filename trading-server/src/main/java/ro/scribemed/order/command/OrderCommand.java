package ro.scribemed.order.command;

import ro.scribemed.order.domain.OrderSide;
import ro.scribemed.order.domain.TradeOrder;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record OrderCommand(
        UUID orderId,
        UUID tradingAccountId,
        UUID stockId,
        OrderSide side,
        long remainingQuantity,
        BigDecimal limitPrice,
        long sequenceNumber
) {

    public OrderCommand {
        Objects.requireNonNull(orderId);
        Objects.requireNonNull(tradingAccountId);
        Objects.requireNonNull(stockId);
        Objects.requireNonNull(side);
        Objects.requireNonNull(limitPrice);

        if (remainingQuantity <= 0) {
            throw new IllegalArgumentException(
                    "Remaining quantity must be greater than zero"
            );
        }

        if (limitPrice.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Limit price must be greater than zero"
            );
        }

        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException(
                    "Sequence number must be greater than zero"
            );
        }
    }

    public static OrderCommand from(TradeOrder order) {
        Objects.requireNonNull(order);

        if (!order.isActive()) {
            throw new IllegalArgumentException(
                    "Only an active order can enter the matching engine"
            );
        }

        return new OrderCommand(
                order.getId(),
                order.getTradingAccount().getId(),
                order.getStock().getId(),
                order.getSide(),
                order.getRemainingQuantity(),
                order.getLimitPrice(),
                order.getSequenceNumber()
        );
    }
}
