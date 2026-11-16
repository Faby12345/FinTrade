package ro.scribemed.trade.service;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.scribemed.order.domain.OrderSide;
import ro.scribemed.order.domain.TradeOrder;
import ro.scribemed.order.dto.OrderMatch;
import ro.scribemed.order.repository.JpaOrderRepository;
import ro.scribemed.order.repository.OrderRepository;
import ro.scribemed.position.domain.Position;
import ro.scribemed.position.repository.JpaPositionRepository;
import ro.scribemed.position.repository.PositionRepository;
import ro.scribemed.shared.persistence.JpaTransactionManager;
import ro.scribemed.stock.domain.Stock;
import ro.scribemed.trade.domain.Trade;
import ro.scribemed.trade.repository.JpaTradeRepository;
import ro.scribemed.trade.repository.TradeRepository;
import ro.scribemed.tradingAccount.domain.TradingAccount;
import ro.scribemed.tradingAccount.repository.JpaTradingAccountRepository;
import ro.scribemed.tradingAccount.repository.TradingAccountRepository;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class TradeSettlementService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TradeSettlementService.class);

    private final JpaTransactionManager transactionManager;

    public TradeSettlementService(
            JpaTransactionManager transactionManager
    ) {
        this.transactionManager = Objects.requireNonNull(transactionManager);
    }

    public Trade settle(OrderMatch match) {
        Objects.requireNonNull(match);

        Trade trade = transactionManager.execute(entityManager -> {
            OrderRepository orderRepository =
                    new JpaOrderRepository(entityManager);
            TradingAccountRepository accountRepository =
                    new JpaTradingAccountRepository(entityManager);
            PositionRepository positionRepository =
                    new JpaPositionRepository(entityManager);
            TradeRepository tradeRepository =
                    new JpaTradeRepository(entityManager);

            LockedOrders orders = lockOrders(orderRepository, match);
            validateMatch(match, orders.buyOrder(), orders.sellOrder());

            LockedAccounts accounts = lockAccounts(
                    accountRepository,
                    match.buyerAccountId(),
                    match.sellerAccountId()
            );

            BigDecimal matchedQuantity =
                    BigDecimal.valueOf(match.quantity());
            BigDecimal reservedAmount = orders.buyOrder()
                    .getLimitPrice()
                    .multiply(matchedQuantity);
            BigDecimal executionAmount = match.executionPrice()
                    .multiply(matchedQuantity);

            Position sellerPosition = positionRepository
                    .findByTradingAccountIdAndStockIdForUpdate(
                            accounts.seller().getId(),
                            match.stockId()
                    )
                    .orElseThrow(() -> new IllegalStateException(
                            "Seller position was not found"
                    ));

            sellerPosition.completeSale(match.quantity());

            if (accounts.buyer().getId().equals(accounts.seller().getId())) {
                sellerPosition.addPurchase(
                        match.quantity(),
                        match.executionPrice()
                );
            } else {
                positionRepository
                        .findByTradingAccountIdAndStockIdForUpdate(
                                accounts.buyer().getId(),
                                match.stockId()
                        )
                        .ifPresentOrElse(
                                position -> position.addPurchase(
                                        match.quantity(),
                                        match.executionPrice()
                                ),
                                () -> positionRepository.add(
                                        new Position(
                                                accounts.buyer(),
                                                orders.buyOrder().getStock(),
                                                match.quantity(),
                                                match.executionPrice()
                                        )
                                )
                        );
            }

            accounts.buyer().settlePurchase(
                    reservedAmount,
                    executionAmount
            );
            accounts.seller().creditSale(executionAmount);

            orders.buyOrder().recordFill(match.quantity());
            orders.sellOrder().recordFill(match.quantity());

            return tradeRepository.add(new Trade(
                    orders.buyOrder(),
                    orders.sellOrder(),
                    accounts.buyer(),
                    accounts.seller(),
                    orders.buyOrder().getStock(),
                    match.quantity(),
                    match.executionPrice()
            ));
        });

        LOGGER.info(
                "Settled trade {}: {} shares of stock {} at {}",
                trade.getId(),
                trade.getQuantity(),
                match.stockId(),
                trade.getExecutionPrice()
        );

        return trade;
    }

    private LockedOrders lockOrders(
            OrderRepository repository,
            OrderMatch match
    ) {
        if (match.buyOrderId().equals(match.sellOrderId())) {
            throw new IllegalArgumentException(
                    "Buy and sell order must be different"
            );
        }

        UUID firstId = smaller(match.buyOrderId(), match.sellOrderId());
        UUID secondId = firstId.equals(match.buyOrderId())
                ? match.sellOrderId()
                : match.buyOrderId();

        TradeOrder first = findOrderForUpdate(repository, firstId);
        TradeOrder second = findOrderForUpdate(repository, secondId);

        return firstId.equals(match.buyOrderId())
                ? new LockedOrders(first, second)
                : new LockedOrders(second, first);
    }

    private LockedAccounts lockAccounts(
            TradingAccountRepository repository,
            UUID buyerId,
            UUID sellerId
    ) {
        UUID firstId = smaller(buyerId, sellerId);
        TradingAccount first = findAccountForUpdate(repository, firstId);

        if (buyerId.equals(sellerId)) {
            return new LockedAccounts(first, first);
        }

        UUID secondId = firstId.equals(buyerId) ? sellerId : buyerId;
        TradingAccount second = findAccountForUpdate(repository, secondId);

        return firstId.equals(buyerId)
                ? new LockedAccounts(first, second)
                : new LockedAccounts(second, first);
    }

    private void validateMatch(
            OrderMatch match,
            TradeOrder buyOrder,
            TradeOrder sellOrder
    ) {
        if (buyOrder.getSide() != OrderSide.BUY) {
            throw new IllegalStateException("Matched buy order is not BUY");
        }
        if (sellOrder.getSide() != OrderSide.SELL) {
            throw new IllegalStateException("Matched sell order is not SELL");
        }
        if (!buyOrder.isActive() || !sellOrder.isActive()) {
            throw new IllegalStateException("Both matched orders must be active");
        }
        if (!buyOrder.getStock().getId().equals(match.stockId())
                || !sellOrder.getStock().getId().equals(match.stockId())) {
            throw new IllegalStateException("Matched orders have the wrong stock");
        }
        if (!buyOrder.getTradingAccount().getId()
                .equals(match.buyerAccountId())) {
            throw new IllegalStateException("Matched buyer account is invalid");
        }
        if (!sellOrder.getTradingAccount().getId()
                .equals(match.sellerAccountId())) {
            throw new IllegalStateException("Matched seller account is invalid");
        }
        if (match.quantity() > buyOrder.getRemainingQuantity()
                || match.quantity() > sellOrder.getRemainingQuantity()) {
            throw new IllegalStateException(
                    "Matched quantity exceeds an order's remaining quantity"
            );
        }
        if (match.executionPrice().compareTo(buyOrder.getLimitPrice()) > 0
                || match.executionPrice()
                .compareTo(sellOrder.getLimitPrice()) < 0) {
            throw new IllegalStateException(
                    "Execution price is outside the matched order limits"
            );
        }
    }

    private TradeOrder findOrderForUpdate(
            OrderRepository repository,
            UUID orderId
    ) {
        return repository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trade order was not found: " + orderId
                ));
    }

    private TradingAccount findAccountForUpdate(
            TradingAccountRepository repository,
            UUID accountId
    ) {
        return repository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trading account was not found: " + accountId
                ));
    }

    private UUID smaller(UUID first, UUID second) {
        return first.compareTo(second) <= 0 ? first : second;
    }

    private record LockedOrders(
            TradeOrder buyOrder,
            TradeOrder sellOrder
    ) {
    }

    private record LockedAccounts(
            TradingAccount buyer,
            TradingAccount seller
    ) {
    }
}
