package ro.scribemed.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import ro.scribemed.shared.persistence.BaseEntity;
import ro.scribemed.stock.domain.Stock;
import ro.scribemed.tradingAccount.domain.TradingAccount;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "trade_orders")
public class TradeOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trading_account_id", nullable = false)
    private TradingAccount tradingAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private OrderSide side;

    @Column(name = "original_quantity", nullable = false)
    private long originalQuantity;

    @Column(name = "remaining_quantity", nullable = false)
    private long remainingQuantity;

    @Column(
            name = "limit_price",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal limitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private OrderStatus status;

    @Column(
            name = "engine_sequence",
            nullable = false,
            unique = true,
            updatable = false
    )
    private long sequenceNumber;

    @Version
    private long version;

    protected TradeOrder() {
    }

    public TradeOrder(
            TradingAccount tradingAccount,
            Stock stock,
            OrderSide side,
            long quantity,
            BigDecimal limitPrice,
            long sequenceNumber
    ) {
        requirePositiveQuantity(quantity);
        requirePositivePrice(limitPrice);
        requirePositiveSequence(sequenceNumber);

        this.tradingAccount = Objects.requireNonNull(tradingAccount);
        this.stock = Objects.requireNonNull(stock);
        this.side = Objects.requireNonNull(side);
        this.originalQuantity = quantity;
        this.remainingQuantity = quantity;
        this.limitPrice = limitPrice;
        this.status = OrderStatus.OPEN;
        this.sequenceNumber = sequenceNumber;
    }

    public void recordFill(long filledQuantity) {
        requirePositiveQuantity(filledQuantity);

        if (!isActive()) {
            throw new IllegalStateException(
                    "Only an active order can be filled"
            );
        }

        if (filledQuantity > remainingQuantity) {
            throw new IllegalArgumentException(
                    "Filled quantity exceeds remaining quantity"
            );
        }

        remainingQuantity -= filledQuantity;
        status = remainingQuantity == 0
                ? OrderStatus.FILLED
                : OrderStatus.PARTIALLY_FILLED;
    }

    public void cancel() {
        if (!isActive()) {
            throw new IllegalStateException(
                    "Only an active order can be cancelled"
            );
        }

        status = OrderStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == OrderStatus.OPEN
                || status == OrderStatus.PARTIALLY_FILLED;
    }

    public TradingAccount getTradingAccount() {
        return tradingAccount;
    }

    public Stock getStock() {
        return stock;
    }

    public OrderSide getSide() {
        return side;
    }

    public long getOriginalQuantity() {
        return originalQuantity;
    }

    public long getRemainingQuantity() {
        return remainingQuantity;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    private static void requirePositiveQuantity(long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Order quantity must be greater than zero"
            );
        }
    }

    private static void requirePositivePrice(BigDecimal price) {
        Objects.requireNonNull(price);

        if (price.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Order price must be greater than zero"
            );
        }
    }

    private static void requirePositiveSequence(long sequenceNumber) {
        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException(
                    "Order sequence number must be greater than zero"
            );
        }
    }
}
