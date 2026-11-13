package ro.scribemed.stock.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import ro.scribemed.shared.persistence.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "stocks")
public class Stock extends BaseEntity {

    @Column(nullable = false, unique = true, length = 12)
    private String symbol;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active;

    @Version
    private long version;

    protected Stock() {
    }

    public Stock(
            String symbol,
            String name,
            BigDecimal price
    ) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.active = true;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }
}
