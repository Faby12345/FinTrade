package ro.scribemed.tradingAccount.domain;

import jakarta.persistence.*;
import ro.scribemed.shared.persistence.BaseEntity;
import ro.scribemed.user.domain.User;

import java.math.BigDecimal;

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
}