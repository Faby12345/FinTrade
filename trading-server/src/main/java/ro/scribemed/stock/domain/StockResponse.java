package ro.scribemed.stock.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record StockResponse(
                UUID id,
                String symbol,
                String name,
                BigDecimal price
        )
{
    public static StockResponse from(Stock stock){
        return new StockResponse(
                stock.getId(),
                stock.getSymbol(),
                stock.getName(),
                stock.getPrice()
        );
    }
}
