package ro.scribemed.authentification.handler;

import ro.scribemed.authentification.dto.UserResponse;
import ro.scribemed.authentification.exception.EmailAlreadyRegisteredException;
import ro.scribemed.authentification.exception.InvalidCredentialsException;
import ro.scribemed.authentification.exception.UsernameAlreadyRegisteredException;
import ro.scribemed.network.protocol.ApiResponse;
import ro.scribemed.network.protocol.exception.InvalidRequestException;
import ro.scribemed.network.tcp.ClientSession;
import ro.scribemed.user.domain.User;
import ro.scribemed.user.service.UserService;
import tools.jackson.databind.JsonNode;

public final class AuthenticationHandler {

    private final UserService userService;

    public AuthenticationHandler(
            UserService userService
    ) {
        this.userService = userService;
    }

    public ApiResponse register(
            String requestId,
            JsonNode payload
    ) {
        String username = requiredText(
                payload,
                "username",
                true
        );

        String email = requiredText(
                payload,
                "email",
                true
        );

        String password = requiredText(
                payload,
                "password",
                false
        );

        try {
            User user = userService.register(
                    username,
                    email,
                    password
            );

            return ApiResponse.success(
                    requestId,
                    UserResponse.from(user)
            );

        } catch (EmailAlreadyRegisteredException exception) {
            return ApiResponse.error(
                    requestId,
                    "EMAIL_ALREADY_REGISTERED",
                    "That email is already registered"
            );
        } catch (UsernameAlreadyRegisteredException exception) {
            return ApiResponse.error(
                    requestId,
                    "USERNAME_ALREADY_REGISTERED",
                    "That username is already registered"
            );
        }
    }

    public ApiResponse login(
            String requestId,
            JsonNode payload,
            ClientSession session
    ) {
        String email = requiredText(
                payload,
                "email",
                true
        );

        String password = requiredText(
                payload,
                "password",
                false
        );

        try {
            User user = userService.authenticate(
                    email,
                    password
            );

            session.authenticate(user.getId());

            return ApiResponse.success(
                    requestId,
                    UserResponse.from(user)
            );

        } catch (InvalidCredentialsException exception) {
            return ApiResponse.error(
                    requestId,
                    "INVALID_CREDENTIALS",
                    "Invalid email or password"
            );
        }
    }

    private String requiredText(
            JsonNode payload,
            String fieldName,
            boolean trim
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

        String value = field.asText();

        return trim ? value.trim() : value;
    }
}
