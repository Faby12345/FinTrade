package ro.scribemed.shared.persistence;

import ro.scribemed.order.domain.OrderSide;
import ro.scribemed.order.dto.OrderMatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.function.Consumer;

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
        return process(incomingOrder, ignored -> {
        });
    }

    public List<OrderMatch> process(
            BookOrder incomingOrder,
            Consumer<OrderMatch> matchConsumer
    ) {
        Objects.requireNonNull(incomingOrder);
        Objects.requireNonNull(matchConsumer);

        if (!stockId.equals(incomingOrder.stockId())) {
            throw new IllegalArgumentException(
                    "Order belongs to a different stock"
            );
        }

        return incomingOrder.side() == OrderSide.BUY
                ? processBuy(incomingOrder, matchConsumer)
                : processSell(incomingOrder, matchConsumer);
    }

    public int buyOrderCount() {
        return buyOrders.size();
    }

    public int sellOrderCount() {
        return sellOrders.size();
    }

    private List<OrderMatch> processBuy(
            BookOrder incomingBuy,
            Consumer<OrderMatch> matchConsumer
    ) {
        List<OrderMatch> matches = new ArrayList<>();

        try {
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

                OrderMatch match = new OrderMatch(
                        stockId,
                        incomingBuy.orderId(),
                        bestSell.orderId(),
                        incomingBuy.accountId(),
                        bestSell.accountId(),
                        matchedQuantity,
                        bestSell.limitPrice()
                );

                matchConsumer.accept(match);

                incomingBuy.fill(matchedQuantity);
                bestSell.fill(matchedQuantity);
                matches.add(match);

                if (bestSell.isFilled()) {
                    sellOrders.poll();
                }
            }
        } catch (RuntimeException exception) {
            restBuy(incomingBuy);
            throw exception;
        }

        restBuy(incomingBuy);

        return List.copyOf(matches);
    }

    private List<OrderMatch> processSell(
            BookOrder incomingSell,
            Consumer<OrderMatch> matchConsumer
    ) {
        List<OrderMatch> matches = new ArrayList<>();

        try {
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

                OrderMatch match = new OrderMatch(
                        stockId,
                        bestBuy.orderId(),
                        incomingSell.orderId(),
                        bestBuy.accountId(),
                        incomingSell.accountId(),
                        matchedQuantity,
                        bestBuy.limitPrice()
                );

                matchConsumer.accept(match);

                incomingSell.fill(matchedQuantity);
                bestBuy.fill(matchedQuantity);
                matches.add(match);

                if (bestBuy.isFilled()) {
                    buyOrders.poll();
                }
            }
        } catch (RuntimeException exception) {
            restSell(incomingSell);
            throw exception;
        }

        restSell(incomingSell);

        return List.copyOf(matches);
    }

    private void restBuy(BookOrder order) {
        if (!order.isFilled()) {
            buyOrders.add(order);
        }
    }

    private void restSell(BookOrder order) {
        if (!order.isFilled()) {
            sellOrders.add(order);
        }
    }
}
