package ro.scribemed.authentification.exception;

public final class UsernameAlreadyRegisteredException extends RuntimeException {

    public UsernameAlreadyRegisteredException(String username) {
        super("Username is already registered: " + username);
    }
}
