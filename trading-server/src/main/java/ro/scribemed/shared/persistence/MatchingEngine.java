package ro.scribemed.shared.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.scribemed.order.command.OrderCommand;
import ro.scribemed.order.dto.OrderMatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public final class MatchingEngine implements Runnable {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MatchingEngine.class);

    private final IncomingOrderQueue incomingOrderQueue;
    private final Consumer<OrderMatch> matchConsumer;
    private final Map<UUID, OrderBook> orderBooks = new HashMap<>();

    public MatchingEngine(IncomingOrderQueue incomingOrderQueue) {
        this(
                incomingOrderQueue,
                match -> LOGGER.info(
                        "Matched {} shares of stock {} at {} "
                                + "(buy order {}, sell order {})",
                        match.quantity(),
                        match.stockId(),
                        match.executionPrice(),
                        match.buyOrderId(),
                        match.sellOrderId()
                )
        );
    }

    public MatchingEngine(
            IncomingOrderQueue incomingOrderQueue,
            Consumer<OrderMatch> matchConsumer
    ) {
        this.incomingOrderQueue =
                Objects.requireNonNull(incomingOrderQueue);
        this.matchConsumer = Objects.requireNonNull(matchConsumer);
    }

    @Override
    public void run() {
        LOGGER.info("FinTrade matching engine started");

        try {
            while (!Thread.currentThread().isInterrupted()) {

                /// take method dose nothing on current thread if it is empty and it will be triggered when a message comes into the queue
                OrderCommand command = incomingOrderQueue.take();

                try {
                    process(command);
                } catch (RuntimeException exception) {
                    LOGGER.error(
                            "Could not process order {}",
                            command.orderId(),
                            exception
                    );
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } finally {
            LOGGER.info("FinTrade matching engine stopped");
        }
    }

    private void process(OrderCommand command) {
        BookOrder incomingOrder = BookOrder.from(command);

        OrderBook orderBook = orderBooks.computeIfAbsent(
                command.stockId(),
                OrderBook::new
        );

        List<OrderMatch> matches = orderBook.process(incomingOrder);

        for (OrderMatch match : matches) {
            matchConsumer.accept(match);
        }

        LOGGER.debug(
                "Order {} processed: {} match(es), {} buy order(s), "
                        + "{} sell order(s) resting for stock {}",
                command.orderId(),
                matches.size(),
                orderBook.buyOrderCount(),
                orderBook.sellOrderCount(),
                command.stockId()
        );
    }
}
