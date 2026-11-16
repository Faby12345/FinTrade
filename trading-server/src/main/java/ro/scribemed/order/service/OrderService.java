package ro.scribemed.order.service;

import ro.scribemed.order.command.OrderCommand;
import ro.scribemed.order.domain.OrderSide;
import ro.scribemed.order.domain.TradeOrder;
import ro.scribemed.order.dto.OrderResponse;
import ro.scribemed.order.dto.PlaceOrderRequest;
import ro.scribemed.order.repository.JpaOrderRepository;
import ro.scribemed.order.repository.OrderRepository;
import ro.scribemed.position.domain.Position;
import ro.scribemed.position.repository.JpaPositionRepository;
import ro.scribemed.position.repository.PositionRepository;
import ro.scribemed.shared.persistence.IncomingOrderQueue;
import ro.scribemed.shared.persistence.JpaTransactionManager;
import ro.scribemed.stock.domain.Stock;
import ro.scribemed.stock.repository.JpaStockRepository;
import ro.scribemed.stock.repository.StockRepository;
import ro.scribemed.tradingAccount.domain.TradingAccount;
import ro.scribemed.tradingAccount.repository.JpaTradingAccountRepository;
import ro.scribemed.tradingAccount.repository.TradingAccountRepository;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class OrderService {
    private final JpaTransactionManager transactionManager;
    private final IncomingOrderQueue incomingOrderQueue;
    private final AtomicLong sequenceGenerator;

    public OrderService(
            JpaTransactionManager transactionManager,
            IncomingOrderQueue incomingOrderQueue
    ) {
        this.transactionManager = Objects.requireNonNull(transactionManager);
        this.incomingOrderQueue = Objects.requireNonNull(incomingOrderQueue);
        this.sequenceGenerator = new AtomicLong(
                loadMaximumSequenceNumber()
        );
    }

    public OrderResponse placeOrder(
            UUID userId,
            PlaceOrderRequest request
    ) throws InterruptedException {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(request);

        long sequenceNumber = sequenceGenerator.incrementAndGet();

        OrderCreationResult result =
                transactionManager.execute(entityManager -> {
                    TradingAccountRepository accountRepository =
                            new JpaTradingAccountRepository(entityManager);
                    StockRepository stockRepository =
                            new JpaStockRepository(entityManager);
                    OrderRepository orderRepository =
                            new JpaOrderRepository(entityManager);

                    PositionRepository positionRepository =
                            new JpaPositionRepository(entityManager);

                    TradingAccount account = accountRepository
                            .findByUserId(userId)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Trading account was not found"
                            ));

                    Stock stock = stockRepository
                            .findById(request.stockId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Stock was not found"
                            ));

                    if (!stock.isActive()) {
                        throw new IllegalStateException(
                                "Stock is not available for trading"
                        );
                    }

                    TradeOrder order = new TradeOrder(
                            account,
                            stock,
                            request.side(),
                            request.quantity(),
                            request.limitPrice(),
                            sequenceNumber
                    );

                    TradeOrder persistedOrder =
                            orderRepository.add(order);

                    if (request.side() == OrderSide.SELL) {
                        Position position = positionRepository
                                .findByTradingAccountIdAndStockIdForUpdate(
                                        account.getId(),
                                        stock.getId()
                                )
                                .orElseThrow(() -> new IllegalStateException(
                                        "You do not own this stock"
                                ));

                        position.reserveForSale(request.quantity());
                    }


                    return new OrderCreationResult(
                            OrderResponse.from(persistedOrder),
                            OrderCommand.from(persistedOrder)
                    );
                });


        incomingOrderQueue.submit(result.command());

        return result.response();
    }


    public List<TradeOrder> getUserOrders(UUID accountId) {
        return transactionManager.execute(entityManager -> {
            OrderRepository orderRepository =
                    new JpaOrderRepository(entityManager);
            return orderRepository.findAllByTradingAccountId(accountId);
        });
    }

    public List<OrderResponse> getOrdersForUser(UUID userId) {
        Objects.requireNonNull(userId);

        return transactionManager.execute(entityManager -> {
            TradingAccountRepository accountRepository =
                    new JpaTradingAccountRepository(entityManager);
            OrderRepository orderRepository =
                    new JpaOrderRepository(entityManager);

            TradingAccount account = accountRepository
                    .findByUserId(userId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Trading account was not found"
                    ));

            return orderRepository
                    .findAllByTradingAccountId(account.getId())
                    .stream()
                    .map(OrderResponse::from)
                    .toList();
        });
    }

    public List<TradeOrder> getAllOrders(){
        return transactionManager.execute(entityManager -> {
            OrderRepository orderRepository =
                    new JpaOrderRepository(entityManager);
            return orderRepository.findAllActive();
        });
    }

    public int enqueueActiveOrders() throws InterruptedException {
        List<OrderCommand> commands =
                transactionManager.execute(entityManager -> {
                    OrderRepository orderRepository =
                            new JpaOrderRepository(entityManager);

                    return orderRepository.findAllActive()
                            .stream()
                            .map(OrderCommand::from)
                            .toList();
                });

        for (OrderCommand command : commands) {
            incomingOrderQueue.submit(command);
        }

        return commands.size();
    }

    private long loadMaximumSequenceNumber() {
        return transactionManager.execute(entityManager -> {
            OrderRepository orderRepository =
                    new JpaOrderRepository(entityManager);

            return orderRepository.findMaxSequenceNumber();
        });
    }

    private record OrderCreationResult(
            OrderResponse response,
            OrderCommand command
    ) {
    }

    private record PersistedOrderCommand(
            TradeOrder order,
            OrderCommand command
    ) {
    }
}
