package ro.scribemed.network.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.scribemed.authentification.handler.AuthenticationHandler;
import ro.scribemed.network.protocol.ApiResponse;
import ro.scribemed.network.protocol.exception.InvalidRequestException;
import ro.scribemed.order.handler.OrderHandler;
import ro.scribemed.position.handler.PositionHandler;
import ro.scribemed.stock.handler.StockHandler;
import ro.scribemed.tradingAccount.handler.TradingAccountHandler;
import ro.scribemed.tradingAccount.service.TradingAccountService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

public final class RequestRouter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RequestRouter.class);

    private final JsonMapper jsonMapper =
            JsonMapper.builder().build();

    private final AuthenticationHandler authenticationHandler;
    private final StockHandler stockHandler;
    private final TradingAccountHandler tradingAccountHandler;
    private final OrderHandler orderHandler;
    private final PositionHandler positionHandler;

    public RequestRouter(
            AuthenticationHandler authenticationHandler,
            StockHandler stockHandler,
            TradingAccountHandler tradingAccountHandler,
            OrderHandler orderHandler,
            PositionHandler positionHandler
    ) {
        this.authenticationHandler = authenticationHandler;
        this.stockHandler = stockHandler;
        this.tradingAccountHandler = tradingAccountHandler;
        this.orderHandler = orderHandler;
        this.positionHandler = positionHandler;
    }

    public String handle(
            String requestJson,
            ClientSession session
    ) {
        String requestId = null;

        try {
            JsonNode request = jsonMapper.readTree(requestJson);

            if (request == null || !request.isObject()) {
                throw new InvalidRequestException(
                        "Request must be a JSON object"
                );
            }

            requestId = optionalText(request, "requestId");
            String type = requiredText(request, "type");
            JsonNode payload = request.path("payload");


            /// refactor thhis to  that descin
            ApiResponse response = switch (type) {
                case "PING" -> ApiResponse.success(
                        requestId,
                        Map.of("message", "PONG")
                );

                case "REGISTER" ->
                        authenticationHandler.register(
                                requestId,
                                payload
                        );

                case "LOGIN" ->
                        authenticationHandler.login(
                                requestId,
                                payload,
                                session
                        );
                case "LIST_STOCKS" ->
                        stockHandler.findAll(
                                requestId
                        );
                case "GET_ACCOUNT" ->
                        tradingAccountHandler.getAccount(
                                requestId,
                                session
                        );
                case "PLACE_ORDER" ->
                        orderHandler.placeOrder(
                                requestId,
                                payload,
                                session
                        );
                case "LIST_MY_ORDERS" ->
                        orderHandler.getMyOrders(
                                requestId,
                                session
                        );
                case "GET_MY_POSITIONS" ->
                        positionHandler.getMyPositions(
                                requestId,
                                session
                        );

                default -> ApiResponse.error(
                        requestId,
                        "UNKNOWN_REQUEST",
                        "Unknown request type: " + type
                );
            };

            return serialize(response);
        } catch (InvalidRequestException exception) {
            return serialize(
                    ApiResponse.error(
                            requestId,
                            "INVALID_REQUEST",
                            exception.getMessage()
                    )
            );
        } catch (Exception exception) {
            LOGGER.error(
                    "Unexpected request processing failure",
                    exception
            );

            return serialize(
                    ApiResponse.error(
                            requestId,
                            "INTERNAL_SERVER_ERROR",
                            "The server could not process the request"
                    )
            );
        }
    }

    private String requiredText(
            JsonNode request,
            String fieldName
    ) {
        String value = optionalText(request, fieldName);

        if (value == null || value.isBlank()) {
            throw new InvalidRequestException(
                    "Missing required field: " + fieldName
            );
        }

        return value;
    }

    private String optionalText(
            JsonNode request,
            String fieldName
    ) {
        JsonNode field = request.get(fieldName);

        if (field == null || field.isNull()) {
            return null;
        }

        if (!field.isTextual()) {
            throw new InvalidRequestException(
                    "Field must be text: " + fieldName
            );
        }

        return field.asText().trim();
    }

    private String serialize(ApiResponse response) {
        try {
            return jsonMapper.writeValueAsString(response);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not serialize API response",
                    exception
            );
        }
    }
}
