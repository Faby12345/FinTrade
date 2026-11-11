package ro.scribemed.network.protocol;


public record ApiError(
        String code,
        String message
) {
}