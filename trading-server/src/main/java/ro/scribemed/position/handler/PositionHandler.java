package ro.scribemed.position.handler;

import ro.scribemed.network.protocol.ApiResponse;
import ro.scribemed.network.tcp.ClientSession;
import ro.scribemed.position.service.PositionService;

import java.util.UUID;

public final class PositionHandler {

    private final PositionService positionService;

    public PositionHandler(PositionService positionService) {
        this.positionService = positionService;
    }

    public ApiResponse getMyPositions(
            String requestId,
            ClientSession session
    ) {
        UUID userId = session.authenticatedUserId()
                .orElse(null);

        if (userId == null) {
            return ApiResponse.error(
                    requestId,
                    "UNAUTHENTICATED",
                    "You must log in before viewing your positions"
            );
        }

        try {
            return ApiResponse.success(
                    requestId,
                    positionService.getPositionsForUser(userId)
            );
        } catch (RuntimeException exception) {
            return ApiResponse.error(
                    requestId,
                    "GET_POSITIONS_FAILED",
                    "The positions could not be loaded"
            );
        }
    }
}
