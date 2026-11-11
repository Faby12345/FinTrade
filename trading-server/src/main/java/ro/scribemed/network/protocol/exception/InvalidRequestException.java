package ro.scribemed.network.protocol.exception;

public final class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
