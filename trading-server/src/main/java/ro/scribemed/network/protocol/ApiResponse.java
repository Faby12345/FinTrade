package ro.scribemed.network.protocol;


public record ApiResponse(
        String requestId,
        String status,
        Object data,
        ApiError error
) {

    public static ApiResponse success(
            String requestId,
            Object data
    ) {
        return new ApiResponse(
                requestId,
                "OK",
                data,
                null
        );
    }

    public static ApiResponse error(
            String requestId,
            String code,
            String message
    ) {
        return new ApiResponse(
                requestId,
                "ERROR",
                null,
                new ApiError(code, message)
        );
    }
}