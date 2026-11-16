package ro.scribemed.tradingAccount.domain;

import jakarta.persistence.*;
import ro.scribemed.shared.persistence.BaseEntity;
import ro.scribemed.user.domain.User;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "trading_accounts")
public class TradingAccount extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @Column(
            name = "cash_balance",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal cashBalance;

    @Column(
            name = "reserved_cash",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal reservedCash;

    @Version
    private long version;

    protected TradingAccount() {
    }

    public TradingAccount(User user, BigDecimal defaultBalance){
        this.user = user;
        this.cashBalance = defaultBalance;
        this.reservedCash = BigDecimal.ZERO;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public BigDecimal getReservedCash() {
        return reservedCash;
    }

    public BigDecimal getAvailableCash() {
        return cashBalance.subtract(reservedCash);
    }

    public void reserveCash(BigDecimal amount) {
        requirePositiveAmount(amount);

        if (amount.compareTo(getAvailableCash()) > 0) {
            throw new IllegalStateException(
                    "Not enough available cash for this order"
            );
        }

        reservedCash = reservedCash.add(amount);
    }

    public void releaseReservedCash(BigDecimal amount) {
        requirePositiveAmount(amount);

        if (amount.compareTo(reservedCash) > 0) {
            throw new IllegalStateException(
                    "Cannot release more than the reserved cash"
            );
        }

        reservedCash = reservedCash.subtract(amount);
    }

    public void settlePurchase(
            BigDecimal reservedAmount,
            BigDecimal executionAmount
    ) {
        requirePositiveAmount(reservedAmount);
        requirePositiveAmount(executionAmount);

        if (executionAmount.compareTo(reservedAmount) > 0) {
            throw new IllegalStateException(
                    "Execution amount exceeds the reserved order amount"
            );
        }

        if (reservedAmount.compareTo(reservedCash) > 0) {
            throw new IllegalStateException(
                    "Buyer does not have enough reserved cash"
            );
        }

        if (executionAmount.compareTo(cashBalance) > 0) {
            throw new IllegalStateException(
                    "Buyer does not have enough cash"
            );
        }

        reservedCash = reservedCash.subtract(reservedAmount);
        cashBalance = cashBalance.subtract(executionAmount);
    }

    public void creditSale(BigDecimal executionAmount) {
        requirePositiveAmount(executionAmount);
        cashBalance = cashBalance.add(executionAmount);
    }

    private static void requirePositiveAmount(BigDecimal amount) {
        Objects.requireNonNull(amount);

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Cash amount must be greater than zero"
            );
        }
    }
}
