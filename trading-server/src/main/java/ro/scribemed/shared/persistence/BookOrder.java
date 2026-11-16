package ro.scribemed.shared.persistence;

import ro.scribemed.order.command.OrderCommand;
import ro.scribemed.order.domain.OrderSide;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class BookOrder {
    private final UUID orderId;
    private final UUID accountId;
    private final UUID stockId;
    private final OrderSide side;
    private final BigDecimal limitPrice;
    private final long sequenceNumber;

    private long remainingQuantity;

    public BookOrder(
            UUID orderId,
            UUID accountId,
            UUID stockId,
            OrderSide side,
            long remainingQuantity,
            BigDecimal limitPrice,
            long sequenceNumber
    ) {
        if (remainingQuantity <= 0) {
            throw new IllegalArgumentException(
                    "Remaining quantity must be greater than zero"
            );
        }

        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException(
                    "Sequence number must be greater than zero"
            );
        }

        this.orderId = Objects.requireNonNull(orderId);
        this.accountId = Objects.requireNonNull(accountId);
        this.stockId = Objects.requireNonNull(stockId);
        this.side = Objects.requireNonNull(side);
        this.remainingQuantity = remainingQuantity;
        this.limitPrice = Objects.requireNonNull(limitPrice);
        this.sequenceNumber = sequenceNumber;
    }
    public static BookOrder from(OrderCommand command) {
        return new BookOrder(
                command.orderId(),
                command.tradingAccountId(),
                command.stockId(),
                command.side(),
                command.remainingQuantity(),
                command.limitPrice(),
                command.sequenceNumber()
        );
    }

    public void fill(long quantity) {
        if (quantity <= 0 || quantity > remainingQuantity) {
            throw new IllegalArgumentException(
                    "Invalid fill quantity"
            );
        }

        remainingQuantity -= quantity;
    }
    public boolean isFilled() {
        return remainingQuantity == 0;
    }
}
