package ro.scribemed.order.dto;

import ro.scribemed.network.protocol.exception.InvalidRequestException;
import ro.scribemed.order.domain.OrderSide;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record PlaceOrderRequest(
        UUID stockId,
        OrderSide side,
        long quantity,
        BigDecimal limitPrice
) {

    public PlaceOrderRequest {
        Objects.requireNonNull(stockId);
        Objects.requireNonNull(side);
        Objects.requireNonNull(limitPrice);

        if (quantity <= 0) {
            throw new InvalidRequestException(
                    "Quantity must be greater than zero"
            );
        }

        if (limitPrice.signum() <= 0) {
            throw new InvalidRequestException(
                    "Limit price must be greater than zero"
            );
        }
    }

    public static PlaceOrderRequest from(JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            throw new InvalidRequestException(
                    "Order payload must be a JSON object"
            );
        }

        return new PlaceOrderRequest(
                requiredUuid(payload, "stockId"),
                requiredSide(payload, "side"),
                requiredPositiveLong(payload, "quantity"),
                requiredPositiveDecimal(payload, "limitPrice")
        );
    }

    private static UUID requiredUuid(
            JsonNode payload,
            String fieldName
    ) {
        String value = requiredText(payload, fieldName);

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException(
                    "Field must be a valid UUID: " + fieldName
            );
        }
    }

    private static OrderSide requiredSide(
            JsonNode payload,
            String fieldName
    ) {
        String value = requiredText(payload, fieldName);

        try {
            return OrderSide.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException(
                    "Order side must be BUY or SELL"
            );
        }
    }

    private static long requiredPositiveLong(
            JsonNode payload,
            String fieldName
    ) {
        JsonNode field = payload.get(fieldName);

        if (field == null || !field.isIntegralNumber()) {
            throw new InvalidRequestException(
                    "Field must be a whole number: " + fieldName
            );
        }

        long value = field.asLong();

        if (value <= 0) {
            throw new InvalidRequestException(
                    "Field must be greater than zero: " + fieldName
            );
        }

        return value;
    }

    private static BigDecimal requiredPositiveDecimal(
            JsonNode payload,
            String fieldName
    ) {
        JsonNode field = payload.get(fieldName);

        if (field == null || !field.isNumber()) {
            throw new InvalidRequestException(
                    "Field must be a number: " + fieldName
            );
        }

        BigDecimal value = field.decimalValue();

        if (value.signum() <= 0) {
            throw new InvalidRequestException(
                    "Field must be greater than zero: " + fieldName
            );
        }

        return value;
    }

    private static String requiredText(
            JsonNode payload,
            String fieldName
    ) {
        JsonNode field = payload.get(fieldName);

        if (
                field == null
                        || !field.isTextual()
                        || field.asText().isBlank()
        ) {
            throw new InvalidRequestException(
                    "Missing required field: " + fieldName
            );
        }

        return field.asText().trim();
    }
}
