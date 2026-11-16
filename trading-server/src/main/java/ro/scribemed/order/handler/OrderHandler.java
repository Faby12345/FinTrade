package ro.scribemed.order.handler;

import ro.scribemed.network.protocol.ApiResponse;
import ro.scribemed.network.tcp.ClientSession;
import ro.scribemed.order.domain.TradeOrder;
import ro.scribemed.order.dto.OrderResponse;
import ro.scribemed.order.dto.PlaceOrderRequest;
import ro.scribemed.order.service.OrderService;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public class OrderHandler {
    private final OrderService orderService;

    public OrderHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    public ApiResponse getAllOrders(String requestId){
        try {
            List<TradeOrder> orders = orderService.getAllOrders();

            List<OrderResponse> response = orders
                    .stream()
                    .map(OrderResponse::from)
                    .toList();

            return ApiResponse.success(
                    requestId,
                    response
            );
        } catch (RuntimeException e){
            return ApiResponse.error(
                    requestId,
                    "GET_ALL_ORDERS_FAILD",
                    "An error occurred trying to fetch all orders"
            );

        }
    }

    public ApiResponse getOrdersForAccountId(
            String requestId,
            UUID accountId
    ){
        try {
            List<TradeOrder> orders = orderService.getUserOrders(accountId);

            List<OrderResponse> response = orders
                    .stream()
                    .map(OrderResponse::from)
                    .toList();

            return ApiResponse.success(
                    requestId,
                    response
            );
        } catch (RuntimeException e){
            return ApiResponse.error(
                    requestId,
                    "GET_USER_ORDERS_FAILD",
                    "An error occurred trying to fetch user's orders"
            );

        }
    }

    public ApiResponse placeOrder(
            String requestId,
            JsonNode payload,
            ClientSession session
    ) {
        UUID userId = session.authenticatedUserId()
                .orElse(null);

        if (userId == null) {
            return ApiResponse.error(
                    requestId,
                    "UNAUTHENTICATED",
                    "You must log in before placing an order"
            );
        }

        PlaceOrderRequest request =
                PlaceOrderRequest.from(payload);

        try {
            OrderResponse response = orderService.placeOrder(
                    userId,
                    request
            );

            return ApiResponse.success(requestId, response);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            return ApiResponse.error(
                    requestId,
                    "ORDER_SUBMISSION_INTERRUPTED",
                    "The order was saved but could not be queued"
            );
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return ApiResponse.error(
                    requestId,
                    "ORDER_REJECTED",
                    exception.getMessage()
            );
        } catch (RuntimeException exception) {
            return ApiResponse.error(
                    requestId,
                    "PLACE_ORDER_FAILED",
                    "The order could not be placed"
            );
        }
    }

    public ApiResponse getMyOrders(
            String requestId,
            ClientSession session
    ) {
        UUID userId = session.authenticatedUserId()
                .orElse(null);

        if (userId == null) {
            return ApiResponse.error(
                    requestId,
                    "UNAUTHENTICATED",
                    "You must log in before viewing your orders"
            );
        }

        try {
            return ApiResponse.success(
                    requestId,
                    orderService.getOrdersForUser(userId)
            );
        } catch (RuntimeException exception) {
            return ApiResponse.error(
                    requestId,
                    "GET_USER_ORDERS_FAILED",
                    "The orders could not be loaded"
            );
        }
    }


}
