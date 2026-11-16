package ro.scribemed.trade.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import ro.scribemed.order.domain.TradeOrder;
import ro.scribemed.shared.persistence.BaseEntity;
import ro.scribemed.stock.domain.Stock;
import ro.scribemed.tradingAccount.domain.TradingAccount;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(
        name = "trades",
        indexes = {
                @Index(name = "idx_trades_buy_order", columnList = "buy_order_id"),
                @Index(name = "idx_trades_sell_order", columnList = "sell_order_id"),
                @Index(name = "idx_trades_stock", columnList = "stock_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_trades_order_pair",
                columnNames = {"buy_order_id", "sell_order_id"}
        )
)
public class Trade extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buy_order_id", nullable = false, updatable = false)
    private TradeOrder buyOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sell_order_id", nullable = false, updatable = false)
    private TradeOrder sellOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_account_id", nullable = false, updatable = false)
    private TradingAccount buyerAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_account_id", nullable = false, updatable = false)
    private TradingAccount sellerAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false, updatable = false)
    private Stock stock;

    @Column(nullable = false, updatable = false)
    private long quantity;

    @Column(
            name = "execution_price",
            nullable = false,
            updatable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal executionPrice;

    @Column(
            name = "total_amount",
            nullable = false,
            updatable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal totalAmount;

    protected Trade() {
    }

    public Trade(
            TradeOrder buyOrder,
            TradeOrder sellOrder,
            TradingAccount buyerAccount,
            TradingAccount sellerAccount,
            Stock stock,
            long quantity,
            BigDecimal executionPrice
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Trade quantity must be greater than zero"
            );
        }

        Objects.requireNonNull(executionPrice);
        if (executionPrice.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Trade execution price must be greater than zero"
            );
        }

        this.buyOrder = Objects.requireNonNull(buyOrder);
        this.sellOrder = Objects.requireNonNull(sellOrder);
        this.buyerAccount = Objects.requireNonNull(buyerAccount);
        this.sellerAccount = Objects.requireNonNull(sellerAccount);
        this.stock = Objects.requireNonNull(stock);
        this.quantity = quantity;
        this.executionPrice = executionPrice;
        this.totalAmount = executionPrice.multiply(
                BigDecimal.valueOf(quantity)
        );
    }

    public TradeOrder getBuyOrder() {
        return buyOrder;
    }

    public TradeOrder getSellOrder() {
        return sellOrder;
    }

    public TradingAccount getBuyerAccount() {
        return buyerAccount;
    }

    public TradingAccount getSellerAccount() {
        return sellerAccount;
    }

    public Stock getStock() {
        return stock;
    }

    public long getQuantity() {
        return quantity;
    }

    public BigDecimal getExecutionPrice() {
        return executionPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
