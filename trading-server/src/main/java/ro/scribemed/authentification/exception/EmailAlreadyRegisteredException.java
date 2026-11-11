package ro.scribemed.authentification.exception;

public final class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException(String email) {
        super("Email is already registered: " + email);
    }
}
