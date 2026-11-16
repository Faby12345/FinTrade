package ro.scribemed.shared.persistence;

import ro.scribemed.order.domain.OrderSide;
import ro.scribemed.order.dto.OrderMatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.UUID;

public final class OrderBook {

    private static final Comparator<BookOrder> BUY_PRIORITY =
            Comparator.comparing(
                            BookOrder::limitPrice,
                            Comparator.reverseOrder()
                    )
                    .thenComparingLong(BookOrder::sequenceNumber);

    private static final Comparator<BookOrder> SELL_PRIORITY =
            Comparator.comparing(BookOrder::limitPrice)
                    .thenComparingLong(BookOrder::sequenceNumber);

    private final UUID stockId;
    private final PriorityQueue<BookOrder> buyOrders =
            new PriorityQueue<>(BUY_PRIORITY);
    private final PriorityQueue<BookOrder> sellOrders =
            new PriorityQueue<>(SELL_PRIORITY);

    public OrderBook(UUID stockId) {
        this.stockId = Objects.requireNonNull(stockId);
    }

    public List<OrderMatch> process(BookOrder incomingOrder) {
        Objects.requireNonNull(incomingOrder);

        if (!stockId.equals(incomingOrder.stockId())) {
            throw new IllegalArgumentException(
                    "Order belongs to a different stock"
            );
        }

        return incomingOrder.side() == OrderSide.BUY
                ? processBuy(incomingOrder)
                : processSell(incomingOrder);
    }

    public int buyOrderCount() {
        return buyOrders.size();
    }

    public int sellOrderCount() {
        return sellOrders.size();
    }

    private List<OrderMatch> processBuy(BookOrder incomingBuy) {
        List<OrderMatch> matches = new ArrayList<>();

        while (!incomingBuy.isFilled() && !sellOrders.isEmpty()) {
            BookOrder bestSell = sellOrders.peek();

            if (incomingBuy.limitPrice()
                    .compareTo(bestSell.limitPrice()) < 0) {
                break;
            }

            long matchedQuantity = Math.min(
                    incomingBuy.remainingQuantity(),
                    bestSell.remainingQuantity()
            );

            incomingBuy.fill(matchedQuantity);
            bestSell.fill(matchedQuantity);

            matches.add(new OrderMatch(
                    stockId,
                    incomingBuy.orderId(),
                    bestSell.orderId(),
                    incomingBuy.accountId(),
                    bestSell.accountId(),
                    matchedQuantity,
                    bestSell.limitPrice()
            ));

            if (bestSell.isFilled()) {
                sellOrders.poll();
            }
        }

        if (!incomingBuy.isFilled()) {
            buyOrders.add(incomingBuy);
        }

        return List.copyOf(matches);
    }

    private List<OrderMatch> processSell(BookOrder incomingSell) {
        List<OrderMatch> matches = new ArrayList<>();

        while (!incomingSell.isFilled() && !buyOrders.isEmpty()) {
            BookOrder bestBuy = buyOrders.peek();

            if (bestBuy.limitPrice()
                    .compareTo(incomingSell.limitPrice()) < 0) {
                break;
            }

            long matchedQuantity = Math.min(
                    incomingSell.remainingQuantity(),
                    bestBuy.remainingQuantity()
            );

            incomingSell.fill(matchedQuantity);
            bestBuy.fill(matchedQuantity);

            matches.add(new OrderMatch(
                    stockId,
                    bestBuy.orderId(),
                    incomingSell.orderId(),
                    bestBuy.accountId(),
                    incomingSell.accountId(),
                    matchedQuantity,
                    bestBuy.limitPrice()
            ));

            if (bestBuy.isFilled()) {
                buyOrders.poll();
            }
        }

        if (!incomingSell.isFilled()) {
            sellOrders.add(incomingSell);
        }

        return List.copyOf(matches);
    }
}
