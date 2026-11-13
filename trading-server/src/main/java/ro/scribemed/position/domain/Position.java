package ro.scribemed.position.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import ro.scribemed.shared.persistence.BaseEntity;
import ro.scribemed.stock.domain.Stock;
import ro.scribemed.tradingAccount.domain.TradingAccount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(
        name = "positions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_positions_account_stock",
                columnNames = {
                        "trading_account_id",
                        "stock_id"
                }
        )
)
public class Position extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trading_account_id", nullable = false)
    private TradingAccount tradingAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private long quantity;

    @Column(
            name = "average_purchase_price",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal averagePurchasePrice;

    @Version
    private long version;

    protected Position() {
    }

    public Position(
            TradingAccount tradingAccount,
            Stock stock,
            long quantity,
            BigDecimal averagePurchasePrice
    ) {
        requirePositiveQuantity(quantity);
        requirePositivePrice(averagePurchasePrice);

        this.tradingAccount = Objects.requireNonNull(tradingAccount);
        this.stock = Objects.requireNonNull(stock);
        this.quantity = quantity;
        this.averagePurchasePrice = averagePurchasePrice;
    }

    public void addPurchase(
            long purchasedQuantity,
            BigDecimal executionPrice
    ) {
        requirePositiveQuantity(purchasedQuantity);
        requirePositivePrice(executionPrice);

        long updatedQuantity = Math.addExact(
                quantity,
                purchasedQuantity
        );

        BigDecimal existingCost = averagePurchasePrice.multiply(
                BigDecimal.valueOf(quantity)
        );
        BigDecimal purchaseCost = executionPrice.multiply(
                BigDecimal.valueOf(purchasedQuantity)
        );

        averagePurchasePrice = existingCost
                .add(purchaseCost)
                .divide(
                        BigDecimal.valueOf(updatedQuantity),
                        4,
                        RoundingMode.HALF_UP
                );
        quantity = updatedQuantity;
    }

    public TradingAccount getTradingAccount() {
        return tradingAccount;
    }

    public Stock getStock() {
        return stock;
    }

    public long getQuantity() {
        return quantity;
    }

    public BigDecimal getAveragePurchasePrice() {
        return averagePurchasePrice;
    }

    private static void requirePositiveQuantity(long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Position quantity must be greater than zero"
            );
        }
    }

    private static void requirePositivePrice(BigDecimal price) {
        Objects.requireNonNull(price);

        if (price.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Position price must be greater than zero"
            );
        }
    }
}
